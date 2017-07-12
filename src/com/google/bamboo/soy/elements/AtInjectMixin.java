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

package com.google.bamboo.soy.elements;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public abstract class AtInjectMixin extends ASTWrapperPsiElement implements AtInjectElement {
  public AtInjectMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getName() {
    if (getParamDefinitionIdentifier() != null) {
      return getParamDefinitionIdentifier().getName();
    }
    return "";
  }

  @Override
  public PsiElement setName(@NotNull String s) throws IncorrectOperationException {
    return null;
  }

  @NotNull
  @Override
  public String getType() {
    if (getTypeExpression() != null) {
      return getTypeExpression().getText();
    }
    return "";
  }
}
