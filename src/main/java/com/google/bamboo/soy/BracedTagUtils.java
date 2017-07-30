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

package com.google.bamboo.soy;

import com.google.bamboo.soy.parser.SoyTypes;
import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class BracedTagUtils {

  private static final ImmutableSet<IElementType> SLASH_R_BRACES =
      ImmutableSet.of(SoyTypes.SLASH_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE);
  private static final ImmutableSet<IElementType> LEFT_BRACES =
      ImmutableSet.of(SoyTypes.LBRACE, SoyTypes.LBRACE_LBRACE, SoyTypes.LBRACE_SLASH,
          SoyTypes.LBRACE_LBRACE_SLASH);
  private static final ImmutableSet<IElementType> BRACES =
      ImmutableSet
          .of(SoyTypes.LBRACE, SoyTypes.LBRACE_LBRACE, SoyTypes.RBRACE, SoyTypes.RBRACE_RBRACE,
              SoyTypes.SLASH_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE, SoyTypes.LBRACE_SLASH,
              SoyTypes.LBRACE_LBRACE_SLASH);

  public static boolean isBrace(@NotNull PsiElement element) {
    return BRACES.contains(element.getNode().getElementType());
  }

  private static boolean isLeftBrace(@NotNull PsiElement element) {
    return LEFT_BRACES.contains(element.getNode().getElementType());
  }

  public static boolean isBracedTag(@NotNull PsiElement tag) {
    return tag.getFirstChild() != null && isLeftBrace(tag.getFirstChild());
  }

  public static boolean isDoubleBraced(@NotNull PsiElement tag) {
    return tag.getFirstChild().getNode().getElementType() == SoyTypes.LBRACE_LBRACE
        || tag.getFirstChild().getNode().getElementType() == SoyTypes.LBRACE_LBRACE_SLASH;
  }

  public static boolean isSelfClosed(@NotNull PsiElement tag) {
    return SLASH_R_BRACES.contains(tag.getLastChild().getNode().getElementType());
  }
}
