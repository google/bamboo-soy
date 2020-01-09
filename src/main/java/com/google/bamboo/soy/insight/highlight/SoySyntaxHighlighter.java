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

package com.google.bamboo.soy.insight.highlight;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import com.google.bamboo.soy.lexer.SoyLexer;
import com.google.bamboo.soy.lexer.SoyTokenTypes;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class SoySyntaxHighlighter extends SyntaxHighlighterBase {

  public static final TextAttributesKey COMMENT =
      createTextAttributesKey("COMMENT_BLOCK", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
  public static final TextAttributesKey KEYWORD =
      createTextAttributesKey("KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey BUILTIN_TYPE =
      createTextAttributesKey("BUILTIN_TYPE", DefaultLanguageHighlighterColors.INTERFACE_NAME);
  public static final TextAttributesKey OPERATOR_LITERAL =
      createTextAttributesKey("OPERATOR_LITERAL", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey NUMBER =
      createTextAttributesKey("NUMBER", DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey STRING =
      createTextAttributesKey("STRING", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey VARIABLE =
      createTextAttributesKey("VARIABLE", DefaultLanguageHighlighterColors.IDENTIFIER);

  private static final ImmutableSet<TextAttributesKey> EMPTY_KEYS = ImmutableSet.of();

  private static final
  ImmutableMultimap<TextAttributesKey, IElementType> attributesToTokenMap =
      ImmutableMultimap.<TextAttributesKey, IElementType>builder()
          .putAll(KEYWORD, SoyTokenTypes.KEYWORDS.getTypes())
          .putAll(OPERATOR_LITERAL, SoyTokenTypes.OPERATOR_LITERALS.getTypes())
          .putAll(BUILTIN_TYPE, SoyTokenTypes.BUILTIN_TYPES.getTypes())
          .putAll(NUMBER, SoyTokenTypes.NUMBER_LITERALS.getTypes())
          .putAll(STRING, SoyTokenTypes.STRING_LITERALS.getTypes())
          .putAll(COMMENT, SoyTokenTypes.COMMENTS.getTypes())
          .build();


  private static final ImmutableMap<IElementType, Collection<TextAttributesKey>>
      tokenToAttributesMap = attributesToTokenMap.inverse().asMap();

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new SoyLexer();
  }

  @NotNull
  @Override
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return tokenToAttributesMap.getOrDefault(tokenType, EMPTY_KEYS)
        .toArray(new TextAttributesKey[0]);
  }
}
