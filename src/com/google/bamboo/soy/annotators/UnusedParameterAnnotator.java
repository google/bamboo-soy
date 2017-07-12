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

package com.google.bamboo.soy.annotators;

import com.google.bamboo.soy.lang.ParamUtils;
import com.google.bamboo.soy.elements.IdentifierElement;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.google.bamboo.soy.lang.Parameter;
import com.google.bamboo.soy.lang.Variable;
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
      if (element.getText().contains("data=")) return;

      Collection<Parameter> parameters = ParamUtils.getParamDefinitions(element);

      Collection<String> usedVariableIdentifiers =
          PsiTreeUtil.findChildrenOfType(element, IdentifierElement.class)
              .stream()
              .map(IdentifierElement::getReferences)
              .flatMap(references -> Arrays.stream(references))
              .map(PsiReference::getCanonicalText)
              .filter(id -> id.startsWith("$"))
              .map(id -> id.substring(1))
              .collect(Collectors.toList());

      for (Variable parameter : parameters) {
        boolean isMatched = false;
        for (String usedIdentifier : usedVariableIdentifiers) {
          if (usedIdentifier.startsWith(parameter.name)) {
            isMatched = true;
            break;
          }
        }

        if (!isMatched) {
          annotationHolder.createErrorAnnotation(parameter.element, "Unused parameter.");
        }
      }
    }
  }
}
