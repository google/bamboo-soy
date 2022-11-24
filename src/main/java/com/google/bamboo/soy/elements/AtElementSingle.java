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

package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.parser.SoyExpr;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Nullable;

public interface AtElementSingle extends PsiNamedElement, TagElement {
  @Nullable
  SoyParamDefinitionIdentifier getParamDefinitionIdentifier();

  @Nullable
  default SoyExpr getDefaultInitializerExpr() {
    return null;
  }

  @Nullable
  default PsiComment getDocComment() {
    PsiElement firstChild = getFirstChild();
    return firstChild instanceof PsiComment
            && firstChild.getNode().getElementType() == SoyTypes.DOC_COMMENT_BLOCK
        ? (PsiComment) firstChild
        : null;
  }
}
