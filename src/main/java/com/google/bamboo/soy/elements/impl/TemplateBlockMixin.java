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

import com.google.bamboo.soy.elements.TemplateBlockElement;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.bamboo.soy.stubs.TemplateBlockStub;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

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

  public boolean isElementBlock() {
    return getTagNameTokenType() == SoyTypes.ELEMENT;
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
  public ItemPresentation getPresentation() {
    return SoyPsiElementPresentationFactory.getItemPresentation(this);
  }
}
