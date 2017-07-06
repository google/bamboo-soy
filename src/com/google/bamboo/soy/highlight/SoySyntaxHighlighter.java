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

package com.google.bamboo.soy.highlight;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import com.google.bamboo.soy.lexer.SoyLexer;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import java.util.HashMap;
import java.util.Map;
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
  public static final TextAttributesKey VARIABLE_REFERENCE =
      createTextAttributesKey("VARIABLE_REFERENCE", DefaultLanguageHighlighterColors.IDENTIFIER);

  private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

  private static final ImmutableMap<IElementType, TextAttributesKey[]> tokenToAttributesMap;

  static {
    Map<TextAttributesKey, ImmutableSet<IElementType>> attributesToTokenMap = new HashMap<>();

    attributesToTokenMap.put(
        KEYWORD,
        ImmutableSet.copyOf(
            new IElementType[] {
              /* Tag openers & single-word tag contents */
              SoyTypes.AT_PARAM,
              SoyTypes.AT_PARAM_OPT,
              SoyTypes.AT_INJECT,
              SoyTypes.AT_INJECT_OPT,
              SoyTypes.ALIAS,
              SoyTypes.CALL,
              SoyTypes.CASE,
              SoyTypes.CSS,
              SoyTypes.DEFAULT,
              SoyTypes.DELCALL,
              SoyTypes.DELPACKAGE,
              SoyTypes.DELTEMPLATE,
              SoyTypes.ELSE,
              SoyTypes.ELSEIF,
              SoyTypes.FALLBACKMSG,
              SoyTypes.FOR,
              SoyTypes.FOREACH,
              SoyTypes.IF,
              SoyTypes.IFEMPTY,
              SoyTypes.LB,
              SoyTypes.LET,
              SoyTypes.LITERAL,
              SoyTypes.LITERAL_DOUBLE,
              SoyTypes.MSG,
              SoyTypes.NAMESPACE,
              SoyTypes.NIL,
              SoyTypes.PARAM,
              SoyTypes.PLURAL,
              SoyTypes.PRINT,
              SoyTypes.RB,
              SoyTypes.SELECT,
              SoyTypes.SP,
              SoyTypes.SWITCH,
              SoyTypes.TEMPLATE,
              SoyTypes.XID,
              SoyTypes.MSG,
              SoyTypes.CARRIAGE_RETURN,
              SoyTypes.NEWLINE_LITERAL,
              SoyTypes.TAB,

              /* Tag closing keywords */
              SoyTypes.END_CALL,
              SoyTypes.END_DELCALL,
              SoyTypes.END_DELTEMPLATE,
              SoyTypes.END_FOREACH,
              SoyTypes.END_FOR,
              SoyTypes.END_IF,
              SoyTypes.END_LET,
              SoyTypes.END_LITERAL,
              SoyTypes.END_LITERAL_DOUBLE,
              SoyTypes.END_MSG,
              SoyTypes.END_PARAM,
              SoyTypes.END_PLURAL,
              SoyTypes.END_SELECT,
              SoyTypes.END_SWITCH,
              SoyTypes.END_TEMPLATE,

              /* Other verbal tokens */
              SoyTypes.AS,
            }));

    attributesToTokenMap.put(
        OPERATOR_LITERAL,
        ImmutableSet.copyOf(new IElementType[] {SoyTypes.AND, SoyTypes.OR, SoyTypes.NOT}));

    attributesToTokenMap.put(
        BUILTIN_TYPE,
        ImmutableSet.copyOf(
            new IElementType[] {
              SoyTypes.ANY,
              SoyTypes.ATTRIBUTES,
              SoyTypes.BOOL,
              SoyTypes.CSS,
              SoyTypes.FLOAT,
              SoyTypes.HTML,
              SoyTypes.INT,
              SoyTypes.JS,
              SoyTypes.LIST,
              SoyTypes.MAP,
              SoyTypes.NULL,
              SoyTypes.NUMBER,
              SoyTypes.STRING,
              SoyTypes.URI,
            }));

    attributesToTokenMap.put(
        NUMBER,
        ImmutableSet.of(SoyTypes.FLOAT_LITERAL, SoyTypes.INTEGER_LITERAL, SoyTypes.BOOL_LITERAL));

    attributesToTokenMap.put(
        STRING, ImmutableSet.of(SoyTypes.STRING_LITERAL, SoyTypes.MULTI_LINE_STRING_LITERAL));

    attributesToTokenMap.put(
        COMMENT, ImmutableSet.of(SoyTypes.COMMENT_BLOCK, SoyTypes.DOC_COMMENT_BLOCK));

    attributesToTokenMap.put(
        VARIABLE_REFERENCE,
        ImmutableSet.of(
            SoyTypes.DOLLAR_SINGLE_IDENTIFIER_LITERAL,
            SoyTypes.CSS_DOLLAR_MULTI_IDENTIFIER_LITERAL));

    tokenToAttributesMap = ImmutableMap.copyOf(createTokenToAttributesMap(attributesToTokenMap));
  }

  private static Map<IElementType, TextAttributesKey[]> createTokenToAttributesMap(
      Map<TextAttributesKey, ImmutableSet<IElementType>> attributesToTokenMap) {
    Map<IElementType, TextAttributesKey[]> tokenToAttributesMap = new HashMap<>();
    Map<TextAttributesKey, TextAttributesKey[]> attributesKeyArrayCache = new HashMap<>();

    attributesToTokenMap.forEach(
        (TextAttributesKey attributes, ImmutableSet<IElementType> tokens) -> {
          tokens.forEach(
              (IElementType token) -> {
                if (!attributesKeyArrayCache.containsKey(attributes)) {
                  TextAttributesKey[] attributesArray = new TextAttributesKey[] {attributes};
                  attributesKeyArrayCache.put(attributes, attributesArray);
                }
                tokenToAttributesMap.put(token, attributesKeyArrayCache.get(attributes));
              });
        });
    return tokenToAttributesMap;
  }

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new SoyLexer();
  }

  @NotNull
  @Override
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return tokenToAttributesMap.getOrDefault(tokenType, EMPTY_KEYS);
  }
}
