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
import com.google.bamboo.soy.parser.SoyAtInjectSingle;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ParamUtils {
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

  public static List<Variable> getParamDefinitions(PsiElement element) {
    SoyTemplateBlock templateBlock = getParentTemplateBlock(element);
    return templateBlock != null ? templateBlock.getParameters() : new ArrayList<>();
  }

  /* Only uses stub tree. */
  public static List<Variable> getParametersForInvocation(PsiElement position, String identifier) {
    SoyTemplateBlock templateBlock =
        TemplateNameUtils.findTemplateDeclaration(position, identifier);

    if (templateBlock == null) {
      return new ArrayList<>();
    }

    return templateBlock.getParameters();
  }

  public static Collection<Variable> getInjectDefinitions(PsiElement element) {
    PsiElement templateBlock = getParentTemplateBlock(element);
    return PsiTreeUtil.findChildrenOfType(templateBlock, SoyAtInjectSingle.class)
        .stream()
        .map(id -> new Variable(id.getName(), "", false, id))
        .collect(Collectors.toList());
  }

  public static Collection<Variable> getLetDefinitions(PsiElement element) {
    PsiElement templateBlock = getParentTemplateBlock(element);
    return PsiTreeUtil.findChildrenOfType(
            templateBlock, com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier.class)
        .stream()
        .map(id -> new Variable(id.getName(), "", false, id))
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

  private static SoyTemplateBlock getParentTemplateBlock(PsiElement element) {
    return (SoyTemplateBlock)
        PsiTreeUtil.findFirstParent(
            element,
            psiElement -> psiElement instanceof com.google.bamboo.soy.parser.SoyTemplateBlock);
  }

  public static class Variable {
    public final String name;
    public final String type;
    public final boolean isOptional;
    public final PsiNamedElement element;

    public Variable(String name, String type, boolean isOptional, PsiNamedElement element) {
      this.name = name;
      this.type = type;
      this.isOptional = isOptional;
      this.element = element;
    }
  }
}
