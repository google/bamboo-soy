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
import com.google.common.collect.ImmutableMap;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public class SoyTokenTypes {

  public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);

  public static final TokenSet COMMENTS =
      TokenSet.create(SoyTypes.COMMENT_BLOCK, SoyTypes.DOC_COMMENT_BLOCK);

  public static final TokenSet STRINGS = TokenSet.create(SoyTypes.OTHER);

  public static final TokenSet KEYWORDS = TokenSet.create(
      /* Tag names */
      SoyTypes.AT_PARAM,
      SoyTypes.AT_PARAM_OPT,
      SoyTypes.AT_STATE,
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
      SoyTypes.ELEMENT,
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
      SoyTypes.END_LITERAL,
      SoyTypes.END_LITERAL_DOUBLE,

      /* Other verbal tokens */
      SoyTypes.AS,
      SoyTypes.RECORD);

  public static final TokenSet OPERATOR_LITERALS = TokenSet
      .create(SoyTypes.AND, SoyTypes.OR, SoyTypes.NOT);

  public static final TokenSet BUILTIN_TYPES = TokenSet
      .create(SoyTypes.ANY,
          SoyTypes.ATTRIBUTES,
          SoyTypes.BOOL,
          // To remove ambiguity CSS is only marked as keyword.
          // SoyTypes.CSS,
          SoyTypes.FLOAT,
          SoyTypes.HTML,
          SoyTypes.INT,
          SoyTypes.JS,
          SoyTypes.LIST,
          SoyTypes.MAP,
          SoyTypes.NULL_LITERAL,
          SoyTypes.NUMBER,
          SoyTypes.STRING,
          SoyTypes.URI);

  public static final TokenSet NUMBER_LITERALS = TokenSet
      .create(SoyTypes.FLOAT_LITERAL, SoyTypes.INTEGER_LITERAL, SoyTypes.BOOL_LITERAL);

  public static final TokenSet STRING_LITERALS = TokenSet
      .create(SoyTypes.STRING_LITERAL, SoyTypes.MULTI_LINE_STRING_LITERAL);

  public static final TokenSet BINARY_OPERATORS = TokenSet.create(
      SoyTypes.QUESTIONMARK,
      SoyTypes.COLON,
      SoyTypes.TERNARY_COALESCER,
      SoyTypes.OR,
      SoyTypes.AND,
      SoyTypes.PIPE_PIPE,
      SoyTypes.AMP_AMP,
      SoyTypes.EQUAL_EQUAL,
      SoyTypes.NOT_EQUAL,
      SoyTypes.GREATER,
      SoyTypes.GREATER_EQUAL,
      SoyTypes.LESS,
      SoyTypes.LESS_EQUAL,
      SoyTypes.PLUS,
      SoyTypes.MINUS,
      SoyTypes.STAR,
      SoyTypes.SLASH,
      SoyTypes.PERCENT);

  public static final TokenSet UNARY_OPERATORS = TokenSet.create(
      SoyTypes.PLUS,
      SoyTypes.MINUS,
      SoyTypes.EXCLAMATION);

  public static final ImmutableMap<IElementType, String> BRACE_TYPE_TO_STRING =
      ImmutableMap.<IElementType, String>builder()
          .put(SoyTypes.LBRACE, "{")
          .put(SoyTypes.LBRACE_LBRACE, "{{")
          .put(SoyTypes.LBRACE_SLASH, "{/")
          .put(SoyTypes.LBRACE_LBRACE_SLASH, "{{/")
          .put(SoyTypes.RBRACE, "}")
          .put(SoyTypes.RBRACE_RBRACE, "}}")
          .put(SoyTypes.SLASH_RBRACE, "/}")
          .put(SoyTypes.SLASH_RBRACE_RBRACE, "/}}")
          .build();

  public static final TokenSet DOUBLE_BRACES =
      TokenSet.create(SoyTypes.LBRACE_LBRACE, SoyTypes.LBRACE_LBRACE_SLASH,
          SoyTypes.RBRACE_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE);

  public static final TokenSet SLASH_R_BRACES =
      TokenSet.create(SoyTypes.SLASH_RBRACE, SoyTypes.SLASH_RBRACE_RBRACE);

  public static final TokenSet LEFT_BRACES = TokenSet.create(
      SoyTypes.LBRACE, SoyTypes.LBRACE_LBRACE, SoyTypes.LBRACE_SLASH, SoyTypes.LBRACE_LBRACE_SLASH);

  public static final TokenSet RIGHT_BRACES =
      TokenSet.create(SoyTypes.RBRACE, SoyTypes.RBRACE_RBRACE, SoyTypes.SLASH_RBRACE,
          SoyTypes.SLASH_RBRACE_RBRACE);
}
