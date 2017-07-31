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

import com.google.bamboo.soy.parser.SoyTypes;
import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public interface TagElement extends PsiElement {
  ImmutableSet<IElementType> SLASH_R_BRACES =
      ImmutableSet.of(SoyTypes.SLASH_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE);
  ImmutableSet<IElementType> LEFT_BRACES =
      ImmutableSet.of(SoyTypes.LBRACE, SoyTypes.LBRACE_LBRACE, SoyTypes.LBRACE_SLASH,
          SoyTypes.LBRACE_LBRACE_SLASH);
  ImmutableSet<IElementType> RIGHT_BRACES =
      ImmutableSet.of(SoyTypes.RBRACE, SoyTypes.RBRACE_RBRACE, SoyTypes.SLASH_RBRACE,
          SoyTypes.SLASH_RBRACE_RBRACE);
  ImmutableSet<IElementType> BRACES =
      ImmutableSet
          .of(SoyTypes.LBRACE, SoyTypes.LBRACE_LBRACE, SoyTypes.RBRACE, SoyTypes.RBRACE_RBRACE,
              SoyTypes.SLASH_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE, SoyTypes.LBRACE_SLASH,
              SoyTypes.LBRACE_LBRACE_SLASH);

  @NotNull
  default TagName getTagName() {
    try {
      // The first child is a brace, the next non-whitespace token is the name.
      return TagName
          .valueOf(PsiTreeUtil.skipSiblingsForward(getFirstChild(), PsiWhiteSpace.class).getText()
              .toUpperCase());
    } catch (NullPointerException | IllegalArgumentException e) {
      return TagName._UNKNOWN_;
    }
  }

  default boolean isDoubleBraced() {
    return getFirstChild().getNode().getElementType() == SoyTypes.LBRACE_LBRACE
        || getFirstChild().getNode().getElementType() == SoyTypes.LBRACE_LBRACE_SLASH;
  }

  default boolean isSelfClosed() {
    return SLASH_R_BRACES.contains(getLastChild().getNode().getElementType());
  }

  default boolean isIncomplete() {
    return !RIGHT_BRACES.contains(getLastChild().getNode().getElementType());
  }

  enum TagName {
    _UNKNOWN_, CALL, DELCALL, TEMPLATE, DELTEMPLATE, FOR, FOREACH, IF, LET, MSG, PARAM, PLURAL, SELECT, SWITCH
  }
}
