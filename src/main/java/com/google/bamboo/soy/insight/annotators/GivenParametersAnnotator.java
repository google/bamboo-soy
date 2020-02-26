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

import com.google.bamboo.soy.elements.CallStatementElement;
import com.google.bamboo.soy.lang.ParamUtils;
import com.google.bamboo.soy.lang.Parameter;
import com.google.bamboo.soy.lang.ParameterSpecification;
import com.google.bamboo.soy.lang.TemplateNameUtils;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.google.bamboo.soy.parser.SoyTemplateReferenceIdentifier;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class GivenParametersAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (psiElement instanceof CallStatementElement) {
      CallStatementElement statement = (CallStatementElement) psiElement;

      if (statement.getBeginCall().getAttributeKeyValuePairList().stream()
          .map(pair -> pair.getAttributeNameIdentifier().getText())
          .anyMatch("data"::equals)) {
        return;
      }

      PsiElement identifier =
          PsiTreeUtil.findChildOfType(statement, SoyTemplateReferenceIdentifier.class);

      if (identifier == null) return;

      Collection<ParameterSpecification> givenParameters = ParamUtils.getGivenParameters(statement);
      SoyTemplateBlock templateBlock =
          TemplateNameUtils.findTemplateDeclaration(statement, identifier.getText());
      if (templateBlock == null) {
        // Do not check parameters for an unknown template invocation.
        return;
      }

      List<Parameter> declaredParameters = templateBlock.getParameters();
      checkMissingRequiredParameters(
          annotationHolder, identifier, givenParameters, declaredParameters);
      checkUnknownParameters(annotationHolder, givenParameters, declaredParameters);
      checkDuplicateParameters(annotationHolder, givenParameters);
    }
  }

  private static void checkMissingRequiredParameters(
      @NotNull AnnotationHolder annotationHolder,
      PsiElement identifier,
      Collection<ParameterSpecification> givenParameters,
      List<Parameter> declaredParameters) {

    List<String> requiredParameterNames =
        declaredParameters.stream()
            .filter(var -> !var.isOptional)
            .map(var -> var.name)
            .collect(Collectors.toList());
    List<String> givenParameterNames =
        givenParameters.stream().map(ParameterSpecification::name).collect(Collectors.toList());

    if (!givenParameterNames.containsAll(requiredParameterNames)) {
      requiredParameterNames.removeAll(givenParameterNames);
      annotationHolder.createErrorAnnotation(
          identifier, "Missing required parameters: " + String.join(",", requiredParameterNames));
    }
  }

  private static void checkUnknownParameters(
      @NotNull AnnotationHolder annotationHolder,
      Collection<ParameterSpecification> givenParameters,
      List<Parameter> declaredParameters) {
    Set<String> declaredParameterNames =
        declaredParameters.stream().map(var -> var.name).collect(Collectors.toSet());
    for (ParameterSpecification givenParameter : givenParameters) {
      if (!declaredParameterNames.contains(givenParameter.name())) {
        annotationHolder.createErrorAnnotation(
            givenParameter.identifier(), "Unknown parameter specified");
      }
    }
  }

  private static void checkDuplicateParameters(
      @NotNull AnnotationHolder annotationHolder,
      Collection<ParameterSpecification> givenParameters) {
    Set<String> seenNames = new HashSet<>();
    for (ParameterSpecification givenParameter : givenParameters) {
      if (!seenNames.add(givenParameter.name())) {
        annotationHolder.createErrorAnnotation(
            givenParameter.identifier(), "Duplicate parameter specified");
      }
    }
  }
}
