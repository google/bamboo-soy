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
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.google.bamboo.soy.scope.Variable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ParamUtils {
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
}
