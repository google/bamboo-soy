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

package com.google.bamboo.soy.refactoring;

import com.google.bamboo.soy.elements.impl.IdentifierMixin;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyIdentifierManipulator extends AbstractElementManipulator<PsiElement> {

  @Nullable
  @Override
  public PsiElement handleContentChange(
      @NotNull PsiElement element, @NotNull TextRange range, String newContent)
      throws IncorrectOperationException {
    if (element instanceof IdentifierMixin) {
      IdentifierMixin identifierMixin = (IdentifierMixin) element;
      PsiReference reference = identifierMixin.getReference();
      if (reference != null && reference.getElement() instanceof PsiNamedElement) {
        ((PsiNamedElement) reference.getElement()).setName(newContent);
      }
    }
    return ((PsiNamedElement) element).setName(newContent);
  }
}
