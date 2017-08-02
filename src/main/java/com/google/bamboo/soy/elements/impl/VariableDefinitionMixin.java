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

package com.google.bamboo.soy.elements.impl;

import com.google.bamboo.soy.elements.VariableDefinitionElement;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public abstract class VariableDefinitionMixin extends ASTWrapperPsiElement
    implements VariableDefinitionElement {

  public VariableDefinitionMixin(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public String getName() {
    return getIdentifierWord() == null ? "" : getIdentifierWord().getText();
  }

  @Override
  public int getTextOffset() {
    return getIdentifierWord() == null
        ? super.getTextOffset()
        : getIdentifierWord().getTextOffset();
  }
}
