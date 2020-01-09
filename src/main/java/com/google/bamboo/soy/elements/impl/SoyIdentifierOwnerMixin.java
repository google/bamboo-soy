// Copyright 2020 Google Inc.
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
package com.google.bamboo.soy.elements.impl;

import com.google.bamboo.soy.refactoring.SoyPsiElementFactory;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A common interface for Soy identifier owner elements that implements get/setName(). */
public abstract class SoyIdentifierOwnerMixin extends ASTWrapperPsiElement
    implements PsiNameIdentifierOwner {

  SoyIdentifierOwnerMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  public abstract PsiElement getIdentifierWord();

  @Override
  public String getName() {
    return getNameIdentifier() != null ? getNameIdentifier().getText() : "";
  }

  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    if (getNameIdentifier() == null) {
      throw new IncorrectOperationException("IdentifierWord missing");
    }
    getNameIdentifier().replace(SoyPsiElementFactory.createIdentifierFromText(getProject(), name));
    return this;
  }

  @Override
  public int getTextOffset() {
    if (getIdentifierWord() != null) {
      return getIdentifierWord().getTextRange().getStartOffset();
    }

    return getNode().getStartOffset();
  }
}
