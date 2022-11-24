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
import com.google.bamboo.soy.elements.impl.TemplateBlockMixin;
import com.google.bamboo.soy.insight.quickfix.RemoveUnusedParameterFix;
import com.google.bamboo.soy.insight.quickfix.RemoveUnusedStateVarFix;
import com.google.bamboo.soy.lang.ParamUtils;
import com.google.bamboo.soy.lang.Parameter;
import com.google.bamboo.soy.lang.Variable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class UnusedParameterOrStateAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
    if (!(element instanceof TemplateBlockMixin)) {
      return;
    }

    // Abort if values are passed with data="...", parameters are sometimes defined for the sake
    // of added documentation even when not technically used directly in the template body.
    if (element.getText().contains("data=")) {
      return;
    }

    Collection<? extends Variable> variables = Streams
        .concat(ParamUtils.getParamDefinitions(element).stream(),
            ParamUtils.getStateDefinitions(element).stream()).collect(
            ImmutableList.toImmutableList());

    Collection<IdentifierElement> childrenOfType = PsiTreeUtil.findChildrenOfType(element,
        IdentifierElement.class);
    Set<String> usedVariableIdentifiers =
        childrenOfType
            .stream()
            .map(IdentifierElement::getReferences)
            .flatMap(Arrays::stream)
            .map(PsiReference::getCanonicalText)
            .collect(ImmutableSet.toImmutableSet());

    for (Variable variable : variables) {
      if (!usedVariableIdentifiers.contains(variable.name)) {
        annotationHolder
            .newAnnotation(((TemplateBlockMixin) element).isElementBlock() ?
                    HighlightSeverity.WEAK_WARNING : HighlightSeverity.ERROR,
                variableType(variable) + " " + variable.name + " is unused.")
            .range(variable.element.getTextRange())
            .withFix(isParameter(variable)
                ? new RemoveUnusedParameterFix(variable.name)
                : new RemoveUnusedStateVarFix(variable.name))
            .create();
      }
    }
  }

  private static String variableType(Variable variable) {
    return isParameter(variable) ? "Parameter" : "State variable";
  }

  private static boolean isParameter(Variable variable) {
    return variable instanceof Parameter;
  }
}
