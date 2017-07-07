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

package com.google.bamboo.soy;

import com.google.bamboo.soy.elements.CallStatementBase;
import com.google.bamboo.soy.parser.SoyAtParamBody;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ParamUtils {
  public static class Variable {
    public final String name;
    public final String type;
    public final PsiNamedElement element;

    public Variable(String name, String type, PsiNamedElement element) {
      this.name = name;
      this.type = type;
      this.element = element;
    }
  }

  public static Collection<Variable> getIdentifiersInScope(PsiElement element) {
    Collection<Variable> identifiers = getLetDefinitions(element);
    identifiers.addAll(getParametersAndInjectDefinitions(element));
    return identifiers;
  }

  public static Collection<Variable> getParametersAndInjectDefinitions(PsiElement element) {
    Collection<Variable> identifiers = getParamDefinitions(element);
    identifiers.addAll(getInjectDefinitions(element));
    return identifiers;
  }

  public static Collection<Variable> getParamDefinitions(PsiElement element) {
    return getParamDefinitions(element, false);
  }

  public static Collection<Variable> getParamDefinitions(
      PsiElement element, boolean excludeOptionalParameters) {
    PsiElement templateBlock = getParentTemplateBlock(element);

    if (templateBlock == null) {
      return new ArrayList<>();
    } else {
      List<Variable> params = new ArrayList<>();

      Collection<SoyAtParamBody> paramDefinitions =
          PsiTreeUtil.findChildrenOfType(templateBlock, SoyAtParamBody.class);

      if (excludeOptionalParameters) {
        paramDefinitions =
            paramDefinitions
                .stream()
                .filter(definition -> !definition.getText().contains("@param?"))
                .collect(Collectors.toList());
      }

      for (SoyAtParamBody paramDefinition : paramDefinitions) {
        if (paramDefinition.getParamDefinitionIdentifier() != null) {
          PsiNamedElement identifier = paramDefinition.getParamDefinitionIdentifier();
          PsiElement typeExpression = paramDefinition.getTypeExpression();
          params.add(
              new Variable(
                  identifier.getText(),
                  typeExpression == null ? "" : typeExpression.getText(),
                  identifier));
        }
      }
      return params;
    }
  }

  public static Collection<Variable> getInjectDefinitions(PsiElement element) {
    PsiElement templateBlock = getParentTemplateBlock(element);
    return PsiTreeUtil.findChildrenOfType(templateBlock, SoyParamDefinitionIdentifier.class)
        .stream()
        .map(id -> new Variable(id.getText(), "", id))
        .collect(Collectors.toList());
  }

  public static Collection<Variable> getLetDefinitions(PsiElement element) {
    PsiElement templateBlock = getParentTemplateBlock(element);
    return PsiTreeUtil.findChildrenOfType(
            templateBlock, com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier.class)
        .stream()
        .map(id -> new Variable(id.getText(), "", id))
        .collect(Collectors.toList());
  }

  public static Collection<String> getGivenParameters(CallStatementBase statement) {
    return statement
        .getParamListElementList()
        .stream()
        .map(param -> param.getBeginParamTag().getParamSpecificationIdentifier())
        .filter(param -> param != null)
        .map(PsiElement::getText)
        .collect(Collectors.toList());
  }

  private static PsiElement getParentTemplateBlock(PsiElement element) {
    return PsiTreeUtil.findFirstParent(
        element,
        psiElement ->
            psiElement instanceof com.google.bamboo.soy.parser.SoyTemplateBlock
                || psiElement instanceof com.google.bamboo.soy.parser.SoyDelegateTemplateBlock);
  }
}
