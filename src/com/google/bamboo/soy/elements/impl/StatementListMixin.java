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

import com.google.bamboo.soy.elements.StatementListElement;
import com.google.bamboo.soy.parser.SoyLetSingleStatement;
import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.lang.Variable;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class StatementListMixin extends ASTWrapperPsiElement
    implements StatementListElement {
  public StatementListMixin(@NotNull ASTNode node) {
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
    return Stream.concat(
            getLetSingleStatementList()
                .stream()
                .map(SoyLetSingleStatement::getVariableDefinitionIdentifier),
            getLetCompoundStatementList()
                .stream()
                .map((let) -> let.getBeginLet().getVariableDefinitionIdentifier()))
        .map(SoyVariableDefinitionIdentifier::toVariable)
        .collect(Collectors.toList());
  }
}
