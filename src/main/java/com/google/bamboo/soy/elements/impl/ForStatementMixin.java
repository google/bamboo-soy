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

import com.google.bamboo.soy.elements.ForStatementElement;
import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.lang.Variable;
import com.google.common.collect.ImmutableList;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ForStatementMixin extends ASTWrapperPsiElement
    implements ForStatementElement {
  public ForStatementMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public Scope getParentScope() {
    return Scope.getScope(this);
  }

  @NotNull
  @Override
  public List<Variable> getLocalVariables() {
    return getBeginFor().getVariableDefinitionIdentifier() != null
        ? ImmutableList.of(getBeginFor().getVariableDefinitionIdentifier().toVariable())
        : ImmutableList.of();
  }
}
