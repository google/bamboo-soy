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

import com.google.bamboo.soy.parser.SoyAtInjectSingle;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.scope.Scope;
import com.google.bamboo.soy.scope.Variable;
import com.google.bamboo.soy.stubs.TemplateBlockStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TemplateBlockMixin extends SoyStubBasedPsiElementBase<TemplateBlockStub>
    implements TemplateBlockElement {
  public TemplateBlockMixin(TemplateBlockStub stub, IStubElementType type) {
    super(stub, type);
  }

  public TemplateBlockMixin(ASTNode node) {
    super(node);
  }

  public TemplateBlockMixin(TemplateBlockStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  @NotNull
  @Override
  public String getName() {
    if (getStub() != null) {
      return getStub().getName();
    }
    SoyTemplateDefinitionIdentifier identifier = getDefinitionIdentifier();
    return identifier == null ? "" : identifier.getName();
  }

  @Override
  public PsiElement setName(@NotNull String s) throws IncorrectOperationException {
    return null;
  }

  @Nullable
  public SoyTemplateDefinitionIdentifier getDefinitionIdentifier() {
    return PsiTreeUtil.findChildOfType(this, SoyTemplateDefinitionIdentifier.class);
  }

  @Override
  public boolean isDelegate() {
    return getStub() != null ? getStub().isDelegate : getBeginDelegateTemplate() != null;
  }

  @NotNull
  @Override
  public List<Variable> getParameters() {
    if (getStub() != null) {
      return getStub().getParameters();
    }
    return getAtParamSingleList()
        .stream()
        .map(SoyAtParamSingle::toVariable)
        .collect(Collectors.toList());
  }

  @Nullable
  @Override
  public Scope getParentScope() {
    return null;
  }

  @NotNull
  @Override
  public List<Variable> getLocalVariables() {
    List<Variable> variables = new ArrayList<>();
    variables.addAll(getParameters());
    variables.addAll(getInjectedVariables());
    return variables;
  }

  private List<Variable> getInjectedVariables() {
    return getAtInjectSingleList()
        .stream()
        .map(SoyAtInjectSingle::toVariable)
        .collect(Collectors.toList());
  }
}
