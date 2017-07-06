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

import com.google.bamboo.soy.ParamUtils;
import com.google.bamboo.soy.TemplateNameUtils;
import com.google.bamboo.soy.elements.CallStatementBase;
import com.google.bamboo.soy.parser.SoyTemplateReferenceIdentifier;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class MissingParametersAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (psiElement instanceof CallStatementBase) {
      CallStatementBase statement = (CallStatementBase) psiElement;

      Collection<String> givenParameters = ParamUtils.getGivenParameters(statement);

      // TODO(thso): Detect data="" in more robust way.
      if (statement.getText().contains("data=")) return;

      PsiElement identifier =
          PsiTreeUtil.findChildOfType(statement, SoyTemplateReferenceIdentifier.class);

      if (identifier == null) return;

      PsiElement templateDefinition =
          TemplateNameUtils.findTemplateDefinition(statement, identifier.getText());
      Collection<String> requiredParameter =
          ParamUtils.getParamDefinitions(templateDefinition, true)
              .stream()
              .map(PsiNamedElement::getName)
              .collect(Collectors.toList());

      if (!givenParameters.containsAll(requiredParameter)) {
        requiredParameter.removeAll(givenParameters);
        annotationHolder.createErrorAnnotation(
            identifier, "Missing required parameters: " + String.join(",", requiredParameter));
      }
    }
  }
}
