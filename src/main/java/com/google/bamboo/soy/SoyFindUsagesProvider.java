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

import com.google.bamboo.soy.lexer.SoyLexer;
import com.google.bamboo.soy.parser.SoyAtInjectSingle;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyAtStateSingle;
import com.google.bamboo.soy.parser.SoyBeginTemplate;
import com.google.bamboo.soy.parser.SoyNamespaceDeclarationIdentifier;
import com.google.bamboo.soy.parser.SoyParamSpecificationIdentifier;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.google.common.base.Strings;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyFindUsagesProvider implements FindUsagesProvider {

  @Nullable
  @Override
  public WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(
        new SoyLexer(),
        TokenSet.create(SoyTypes.IDENTIFIER_WORD),
        TokenSet.create(SoyTypes.COMMENT_BLOCK, SoyTypes.LINE_COMMENT),
        TokenSet.EMPTY);
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof PsiNamedElement;
  }

  @Nullable
  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @NotNull
  @Override
  public String getType(@NotNull PsiElement psiElement) {
    if (psiElement instanceof SoyVariableDefinitionIdentifier) {
      return "Variable"; // for/foreach/let
    }
    if (psiElement instanceof SoyParamSpecificationIdentifier) {
      return "Parameter specification";
    }
    PsiElement parent = psiElement.getParent();
    if (parent instanceof SoyAtParamSingle) {
      return "Parameter";
    }
    if (parent instanceof SoyAtStateSingle) {
      return "State param";
    }
    if (parent instanceof SoyAtInjectSingle) {
      return "Injected param";
    }
    if (parent instanceof SoyNamespaceDeclarationIdentifier) {
      return "Namespace";
    }
    if (parent instanceof SoyBeginTemplate) {
      return ((SoyBeginTemplate) parent).getTagNameTokenType() == SoyTypes.ELEMENT
          ? "Element"
          : "Template";
    }
    return "";
  }

  @NotNull
  @Override
  public String getDescriptiveName(@NotNull PsiElement psiElement) {
    if (!(psiElement instanceof PsiNamedElement)) {
      return "";
    }
    return Strings.nullToEmpty(((PsiNamedElement) psiElement).getName());
  }

  @NotNull
  @Override
  public String getNodeText(@NotNull PsiElement psiElement, boolean useFullName) {
    return psiElement.getText();
  }
}
