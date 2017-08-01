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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TagElement extends PsiElement {

  ImmutableMap<IElementType, String> BRACE_TYPE_TO_STRING = ImmutableMap.<IElementType, String>builder()
      .put(SoyTypes.LBRACE, "{")
      .put(SoyTypes.LBRACE_LBRACE, "{{")
      .put(SoyTypes.LBRACE_SLASH, "{/")
      .put(SoyTypes.LBRACE_LBRACE_SLASH, "{{/")
      .put(SoyTypes.RBRACE, "}")
      .put(SoyTypes.RBRACE_RBRACE, "}}")
      .put(SoyTypes.SLASH_RBRACE, "/}")
      .put(SoyTypes.SLASH_RBRACE_RBRACE, "/}}")
      .build();

  ImmutableSet<IElementType> SLASH_R_BRACES =
      ImmutableSet.of(SoyTypes.SLASH_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE);
  ImmutableSet<IElementType> DOUBLE_BRACES =
      ImmutableSet.of(SoyTypes.LBRACE_LBRACE, SoyTypes.LBRACE_LBRACE_SLASH,
          SoyTypes.RBRACE_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE);
  ImmutableSet<IElementType> LEFT_SLASH_BRACES = ImmutableSet.of(
      SoyTypes.LBRACE_SLASH, SoyTypes.LBRACE_LBRACE_SLASH);
  ImmutableSet<IElementType> RIGHT_BRACES =
      ImmutableSet.of(SoyTypes.RBRACE, SoyTypes.RBRACE_RBRACE, SoyTypes.SLASH_RBRACE,
          SoyTypes.SLASH_RBRACE_RBRACE);

  static boolean isDoubleBrace(IElementType type) {
    return DOUBLE_BRACES.contains(type);
  }

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

  @NotNull
  default IElementType getOpeningBraceType() {
    return getFirstChild().getNode().getElementType();
  }

  @Nullable
  default IElementType getClosingBraceType() {
    IElementType type = getLastChild().getNode().getElementType();
    return RIGHT_BRACES.contains(type) ? type : null;
  }

  default boolean isDoubleBraced() {
    return isDoubleBrace(getOpeningBraceType());
  }

  default boolean isClosingTag() {
    return LEFT_SLASH_BRACES.contains(getOpeningBraceType());
  }

  default boolean isSelfClosed() {
    return SLASH_R_BRACES.contains(getClosingBraceType());
  }

  default boolean isIncomplete() {
    return getClosingBraceType() == null;
  }

  default String generateClosingTag() {
    String closingTag = "{/" + getTagName().name().toLowerCase() + "}";
    if (isDoubleBraced()) {
      closingTag = "{" + closingTag + "}";
    }
    return closingTag;
  }

  enum TagName {
    _UNKNOWN_,
    ALIAS,
    CALL,
    DELCALL,
    DELPACKAGE,
    NAMESPACE,
    TEMPLATE,
    DELTEMPLATE,
    CASE,
    CSS,
    DEFAULT,
    ELSE,
    ELSIF,
    FALLBACKMSG,
    FOR,
    FOREACH,
    IF,
    IFEMPTY,
    LB,
    LET,
    MSG,
    NIL,
    PARAM,
    PLURAL,
    PRINT,
    RB,
    SELECT,
    SP,
    SWITCH,
    XID
  }
}
