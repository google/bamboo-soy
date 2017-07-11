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

package com.google.bamboo.soy.lexer;

import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergeFunction;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public class SoyLexer extends MergingLexerAdapterBase {
  private static final TokenSet WHITE_SPACES =
      TokenSet.create(TokenType.WHITE_SPACE, SoyTypes.HORIZONTAL_SPACE, SoyTypes.LINE_TERMINATOR);

  public SoyLexer() {
    super(new SoyRawLexer());
  }

  @Override
  public MergeFunction getMergeFunction() {
    return ((final IElementType type, final Lexer originalLexer) -> {
      if (type == SoyTypes.OTHER || WHITE_SPACES.contains(type)) {
        IElementType returnType = TokenType.WHITE_SPACE;
        while (originalLexer.getTokenType() == SoyTypes.OTHER
            || WHITE_SPACES.contains(originalLexer.getTokenType())) {
          if (originalLexer.getTokenType() == SoyTypes.OTHER) {
            returnType = SoyTypes.OTHER;
          }
          originalLexer.advance();
        }
        return returnType;
      }

      return type;
    });
  }
}
