// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.elements.IdentifierElement;
import com.google.bamboo.soy.insight.quickfix.RemoveUnusedParameterFix;
import com.google.bamboo.soy.lang.ParamUtils;
import com.google.bamboo.soy.lang.Parameter;
import com.google.bamboo.soy.lang.Variable;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class UnusedParameterAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
    if (element instanceof SoyTemplateBlock) {
      // Abort if values are passed with data="...", parameter are sometimes defined for the sake
      // of added documentation even when not technically used directly in the template body.
      if (element.getText().contains("data=")) {
        return;
      }

      Collection<? extends Variable> variables = Streams
          .concat(ParamUtils.getParamDefinitions(element).stream(),
              ParamUtils.getStateDefinitions(element).stream()).collect(
              ImmutableList.toImmutableList());

      Collection<String> usedVariableIdentifiers =
          PsiTreeUtil.findChildrenOfType(element, IdentifierElement.class)
              .stream()
              .map(IdentifierElement::getReferences)
              .flatMap(Arrays::stream)
              .map(PsiReference::getCanonicalText)
              .distinct()
              .collect(Collectors.toList());

      for (Variable variable : variables) {
        boolean isMatched = false;
        for (String usedIdentifier : usedVariableIdentifiers) {
          if (usedIdentifier.equals(variable.name)) {
            isMatched = true;
            break;
          }
        }

        if (!isMatched) {
          Annotation annotation = annotationHolder.createErrorAnnotation(
              variable.element,
              variableType(variable) + " " + variable.name + " is unused.");
          annotation.registerFix(new RemoveUnusedParameterFix(variable.name));
        }
      }
    }
  }

  private static String variableType(Variable variable) {
    return variable instanceof Parameter ? "Parameter" : "State variable";
  }
}
