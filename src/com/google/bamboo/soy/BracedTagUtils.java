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

public class BracedTagUtils {
  private static final ImmutableSet<IElementType> slashRBraces =
      ImmutableSet.of(SoyTypes.SLASH_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE);

  public static boolean isDoubleBraced(PsiElement tag) {
    return tag.getFirstChild().getNode().getElementType() == SoyTypes.LBRACE_LBRACE;
  }

  public static boolean isSelfClosed(PsiElement tag) {
    return slashRBraces.contains(tag.getLastChild().getNode().getElementType());
  }
}
