// Copyright 2019 Google Inc.
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

import com.google.bamboo.soy.lang.Parameter;
import com.google.bamboo.soy.lang.StateVariable;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyTypeExpression;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.bamboo.soy.stubs.AtParamStub;
import com.google.bamboo.soy.stubs.AtStateStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AtStateElement extends StubBasedPsiElement<AtStateStub>, PsiNamedElement,
    TagElement {

  @Nullable
  SoyParamDefinitionIdentifier getParamDefinitionIdentifier();

  @Nullable
  SoyTypeExpression getTypeExpression();

  default PsiElement setName(@NotNull String s) throws IncorrectOperationException {
    return null;
  }

  @NotNull
  default String getType() {
    if (getStub() != null) {
      return getStub().type;
    }
    if (getTypeExpression() != null) {
      return getTypeExpression().getText();
    }
    return "";
  }

  default StateVariable toStateVariable() {
    return this.getParamDefinitionIdentifier() == null
        ? null
        : new StateVariable(getName(), getType(), this.getParamDefinitionIdentifier());
  }
}
