package com.google.bamboo.soy.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.psi.TokenType;

/** Lexer for closure template files. */
%%

%class SoyFlexLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{ return;
%eof}

/* Identifier */
Alpha=[a-zA-Z]

IdentifierChar={Alpha}|"_"
Digit=[0-9]

IdentifierWord={IdentifierChar}({IdentifierChar}|{Digit})*
QualifiedIdentifier="."?{IdentifierWord}("."{IdentifierWord})*

/* Line terminators, white space, and comments */
LineTerminator=\r|\n|\r\n
InputCharacter=[^\r\n]
HorizontalSpace=[ \t\f]
WhiteSpace={LineTerminator}|{HorizontalSpace}

OptionalEndOfBlockComment="*"*("*/")?
CommentContent=([^*]|("*"+[^/*]))*
DocCommentBlock="/**"{CommentContent}{OptionalEndOfBlockComment}? // May be unterminated
CommentBlock="/*"{CommentContent}{OptionalEndOfBlockComment}? // May be unterminated
LineComment="//"{InputCharacter}*
HtmlComment="<!--([^-]|-[^-]|--+[^->])*-*-->"
BlockComment={CommentBlock}|{HtmlComment}

/* Integer literal */
DecimalDigit=[0-9]
DecimalDigits={DecimalDigit}+
HexDigit=[0-9a-fA-F]

DecimalIntegerLiteral={DecimalDigits}
SignedDecimalIntegerLiteral=("+"|"-")?{DecimalIntegerLiteral}
HexNumeral=0x{HexDigit}+

IntegerLiteral={HexNumeral}|{DecimalIntegerLiteral}

ExponentPart="e"{SignedDecimalIntegerLiteral}

FloatingPointLiteral=({DecimalDigit}+"."{DecimalDigit}+{ExponentPart}?)|({DecimalDigit}+{ExponentPart}?)

/* String literal */
DoubleQuotedStringLiteral=\"([^\r\n\"\\]|\\.)*\"
SingleQuotedStringLiteral='([^\r\n'\\]|\\.)*'

MultiLineDoubleQuotedStringLiteral=\"([^\"\\]|\\([^]))*\"
MultiLineSingleQuotedStringLiteral='([^'\\]|\\([^]))*'

NonSemantical=({WhiteSpace}|{LineComment}|{DocCommentBlock}|{BlockComment})+

/* Lexer states */
%state OPEN_TAG
%state CLOSE_TAG
%state YYINITIAL_IN_TAG
%state LITERAL_SINGLE
%state LITERAL_DOUBLE
%state TAG_CONTINUATION
%state TAG_CONTINUATION_NAMESPACE
%state TAG_IDENTIFIER_WORD
%state TAG_QUALIFIED_IDENTIFIER
%state TAG_QUALIFIED_IDENTIFIER_NAMESPACE
%state WHITESPACE_BEFORE_LINE_COMMENT
%state IMPORT
%state IMPORT_CONTINUATION

%%

// -- Literal tags: ensure we parse literal tags and eat their whole content first without matching
// anything within the literal body.
<LITERAL_SINGLE> {
  "{/literal}" { yypop(); return SoyTypes.END_LITERAL; }
  "{{literal}}" { return SoyTypes.OTHER; }
  "{{/literal}}" { return SoyTypes.OTHER; }
}

<LITERAL_DOUBLE> {
  "{{/literal}}" { yypop(); return SoyTypes.END_LITERAL_DOUBLE; }
  "{literal}" { return SoyTypes.OTHER; }
  "{/literal}" { return SoyTypes.OTHER; }
}

<LITERAL_SINGLE, LITERAL_DOUBLE> {
  .|{WhiteSpace}|"{{"|"{/"|"{{/" { return SoyTypes.OTHER; }
}

"{literal}" { yypush(); yybegin(LITERAL_SINGLE); return SoyTypes.LITERAL; }
"{{literal}}" { yypush(); yybegin(LITERAL_DOUBLE); return SoyTypes.LITERAL_DOUBLE; }

/* Comments */

// Require a whitespace before a line comment but do not coalesce the two.
{WhiteSpace}/{LineComment} {
    yypush();
    yybegin(WHITESPACE_BEFORE_LINE_COMMENT);
    return TokenType.WHITE_SPACE;
}
<WHITESPACE_BEFORE_LINE_COMMENT> {
  {LineComment} { yypop(); return SoyTypes.LINE_COMMENT; }
  . { yypop(); return SoyTypes.OTHER; } // Should not really happen.
}

// Line comment can also begin at line start.
^{LineComment} { return SoyTypes.LINE_COMMENT; }
{DocCommentBlock} { return SoyTypes.DOC_COMMENT_BLOCK; }
{BlockComment} { return SoyTypes.COMMENT_BLOCK; }

{WhiteSpace}  { return TokenType.WHITE_SPACE; }

/**
  The idea behind tracking open-close tags is to allow Python-style imports only at the top level, not inside tag
  contents, which breaks HTML parsing, like <div style="color: white !important">. Here, the leading part of "important"
  would get parsed as "import" because the parser is in the YYINITIAL state. We work around this by having tag contents
  parsed in the YYINITIAL_IN_TAG state.

  We also need to parse {namespace ...} in a special way, since it doesn't have a closing counterpart and everything
  following it should start in the YYINITIAL (not YYINITIAL_IN_TAG) state.
 */

<CLOSE_TAG> {
  /* Tag closing */
  "/}" { return SoyTypes.OTHER; } // ERROR: {/..../} or {{/.../}
  "}" { yypop(); return SoyTypes.RBRACE; }
  "}}" { yypop(); return SoyTypes.RBRACE_RBRACE; }
  "/}}" { return SoyTypes.OTHER; } // ERROR: {/..../}} or {{/..../}}
}

<OPEN_TAG, TAG_CONTINUATION> {
  /* Tag closing */
  "/}" { yypop(); return SoyTypes.SLASH_RBRACE; } // self-closing
  "}" { yybegin(YYINITIAL_IN_TAG); return SoyTypes.RBRACE; }
  "}}" { yybegin(YYINITIAL_IN_TAG); return SoyTypes.RBRACE_RBRACE; }
  "/}}" { yypop(); return SoyTypes.SLASH_RBRACE_RBRACE; } // self-closing
}

<TAG_CONTINUATION_NAMESPACE> {
  /* Tag closing */
  "}" { yypop(); return SoyTypes.RBRACE; }
}

<OPEN_TAG> {
  "namespace"/{NonSemantical}{QualifiedIdentifier} {
    yybegin(YYINITIAL); yypush(); yybegin(TAG_QUALIFIED_IDENTIFIER_NAMESPACE); return SoyTypes.NAMESPACE;
  }
}

<OPEN_TAG, CLOSE_TAG> {
  /* Tag names that may be followed by identifiers */
  "@inject"/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.AT_INJECT; }
  "@inject?"/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.AT_INJECT_OPT; }
  "@param"/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.AT_PARAM; }
  "@param?"/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.AT_PARAM_OPT; }
  "@state"/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.AT_STATE; }
  "alias"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.ALIAS; }
  "call"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.CALL; }
  "delcall"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELCALL; }
  "delpackage"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELPACKAGE; }
  "deltemplate"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELTEMPLATE; }
  "element"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.ELEMENT; }
  "param"/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.PARAM; }
  "template"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.TEMPLATE; }

  "let" { yybegin(TAG_CONTINUATION); return SoyTypes.LET; }
  "element" { yybegin(TAG_CONTINUATION); return SoyTypes.ELEMENT; }
}

<OPEN_TAG, TAG_CONTINUATION, TAG_CONTINUATION_NAMESPACE, CLOSE_TAG> {
  "alias" { return SoyTypes.ALIAS; }
  "call" { return SoyTypes.CALL; }
  "delcall" { return SoyTypes.DELCALL; }
  "delpackage" { return SoyTypes.DELPACKAGE; }
  "deltemplate" { return SoyTypes.DELTEMPLATE; }
  "namespace" { return SoyTypes.NAMESPACE; }
  "template" { return SoyTypes.TEMPLATE; }

  "css" { return SoyTypes.CSS; }
  "xid" { return SoyTypes.XID; }

  /* Other tag names */
  "@inject" { return SoyTypes.AT_INJECT; }
  "@inject?" { return SoyTypes.AT_INJECT_OPT; }
  "@param" { return SoyTypes.AT_PARAM; }
  "@param?" { return SoyTypes.AT_PARAM_OPT; }
  "@state" { return SoyTypes.AT_STATE; }
  "case" { return SoyTypes.CASE; }
  "default" { return SoyTypes.DEFAULT; }

  "else" { return SoyTypes.ELSE; }
  "elseif" { return SoyTypes.ELSEIF; }
  "fallbackmsg" { return SoyTypes.FALLBACKMSG; }
  "for" { return SoyTypes.FOR; }
  "foreach" { return SoyTypes.FOREACH; }
  "if" { return SoyTypes.IF; }
  "ifempty" { return SoyTypes.IFEMPTY; }
  "lb" { return SoyTypes.LB; }
  "msg" { return SoyTypes.MSG; }
  "velog" { return SoyTypes.VELOG; }

  "key" { return SoyTypes.KEY; }
  "nbsp" { return SoyTypes.NBSP; }
  "nil" { return SoyTypes.NIL; }
  "param" { return SoyTypes.PARAM; }
  "plural" { return SoyTypes.PLURAL; }
  "print" { return SoyTypes.PRINT; }
  "rb" { return SoyTypes.RB; }
  "select" { return SoyTypes.SELECT; }
  "skip" { return SoyTypes.SKIP; }
  "sp" { return SoyTypes.SP; }
  "switch" { return SoyTypes.SWITCH; }

  /* Types */
  "any" { return SoyTypes.ANY; }
  "string" { return SoyTypes.STRING; }
  "bool" { return SoyTypes.BOOL; }
  "int" { return SoyTypes.INT; }
  "float" { return SoyTypes.FLOAT; }
  "number" { return SoyTypes.NUMBER; }
  "html" { return SoyTypes.HTML; }
  "uri" { return SoyTypes.URI; }
  "js" { return SoyTypes.JS; }
  "attributes" { return SoyTypes.ATTRIBUTES; }
  "list" { return SoyTypes.LIST; }
  "map" { return SoyTypes.MAP; }

  /* Verbal logical operators */
  "and" { return SoyTypes.AND; }
  "not" { return SoyTypes.NOT; }
  "or" { return SoyTypes.OR; }
  "in" { return SoyTypes.IN; }

  /* Other verbal tokens */
  "as" { return SoyTypes.AS; }
  record { return SoyTypes.RECORD; }
  true { return SoyTypes.BOOL_LITERAL; }
  false { return SoyTypes.BOOL_LITERAL; }
  null { return SoyTypes.NULL_LITERAL; }

  /* Maybe followed by IdentifierWord, a special state to not trigger keyword rules. */
  "."/{NonSemantical}?{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.DOT; }
  "?."/{NonSemantical}?{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.DOT_NULL_CHECK; }
  "$"/{NonSemantical}?{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.DOLLAR; }

  "." { return SoyTypes.DOT; }
  "?." { return SoyTypes.DOT_NULL_CHECK; }
  "$" { return SoyTypes.DOLLAR; }

  /* Literals */
  {DoubleQuotedStringLiteral} { return SoyTypes.STRING_LITERAL; }
  {SingleQuotedStringLiteral} { return SoyTypes.STRING_LITERAL; }
  {MultiLineDoubleQuotedStringLiteral} { return SoyTypes.MULTI_LINE_STRING_LITERAL; }
  {MultiLineSingleQuotedStringLiteral} { return SoyTypes.MULTI_LINE_STRING_LITERAL; }
  {IntegerLiteral} { return SoyTypes.INTEGER_LITERAL; }
  {FloatingPointLiteral} { return SoyTypes.FLOAT_LITERAL; }

  "\\r" { return SoyTypes.CARRIAGE_RETURN; }
  "\\n" { return SoyTypes.NEWLINE_LITERAL; }
  "\\t" { return SoyTypes.TAB; }

  "=" { return SoyTypes.EQUAL; }
  ";" { return SoyTypes.SEMICOLON; }
  ":" { return SoyTypes.COLON; }
  ":=" { return SoyTypes.COLON_EQUAL; }
  "?" { return SoyTypes.QUESTIONMARK; }
  "?:" { return SoyTypes.TERNARY_COALESCER; }

  "?[" { return SoyTypes.INDEX_NULL_CHECK; }
  "|" { return SoyTypes.PIPE; }

  "&&" { return SoyTypes.AMP_AMP; }
  "||" { return SoyTypes.PIPE_PIPE; }

  "(" { return SoyTypes.PARENS_OPEN; }
  ")" { return SoyTypes.PARENS_CLOSE; }
  "[" { return SoyTypes.SQUARE_OPEN; }
  "]" { return SoyTypes.SQUARE_CLOSE; }
  "," { return SoyTypes.COMMA; }

  "*" { return SoyTypes.STAR; }
  "/" { return SoyTypes.SLASH; }
  "%" { return SoyTypes.PERCENT; }
  "+" { return SoyTypes.PLUS; }
  "-" { return SoyTypes.MINUS; }
  "!" { return SoyTypes.EXCLAMATION; }

  "<" { return SoyTypes.LESS; }
  ">" { return SoyTypes.GREATER; }
  ">=" { return SoyTypes.GREATER_EQUAL; }
  "==" { return SoyTypes.EQUAL_EQUAL; }
  "<=" { return SoyTypes.LESS_EQUAL; }
  "!=" { return SoyTypes.NOT_EQUAL; }

  {IdentifierWord} { return SoyTypes.IDENTIFIER_WORD; }
}

// Only QualifiedIdentifier expected (ensured by look-ahead).
<TAG_QUALIFIED_IDENTIFIER> {
  {QualifiedIdentifier} { yybegin(TAG_CONTINUATION); return SoyTypes.QUALIFIED_IDENTIFIER; }
}

<TAG_QUALIFIED_IDENTIFIER_NAMESPACE> {
  {QualifiedIdentifier} { yybegin(TAG_CONTINUATION_NAMESPACE); return SoyTypes.QUALIFIED_IDENTIFIER; }
}

// Only IdentifierWord expected (ensured by look-ahead).
<TAG_IDENTIFIER_WORD> {
  {IdentifierWord} { yybegin(TAG_CONTINUATION); return SoyTypes.IDENTIFIER_WORD; }
}

// Import clause.
<YYINITIAL, IMPORT, IMPORT_CONTINUATION> {
    // All except YYINITIAL for error recovery.
    "import" { yypush(); yybegin(IMPORT); return SoyTypes.IMPORT_OPEN; }
}

<IMPORT> {
  "{" { yybegin(IMPORT_CONTINUATION); return SoyTypes.LBRACE; }
  . { yypop(); return SoyTypes.OTHER; }
}

// LBRACEs always start a TAG, except when in the IMPORT state.
"{{/" { yybegin(CLOSE_TAG); return SoyTypes.LBRACE_LBRACE_SLASH; }
"{/" { yybegin(CLOSE_TAG); return SoyTypes.LBRACE_SLASH; }
"{{" { yypush(); yybegin(OPEN_TAG); return SoyTypes.LBRACE_LBRACE; }
"{" { yypush(); yybegin(OPEN_TAG); return SoyTypes.LBRACE; }

<IMPORT_CONTINUATION> {
  "as" { return SoyTypes.AS; }
  "}" { return SoyTypes.RBRACE; }
  "," { return SoyTypes.COMMA; }
  "from" { return SoyTypes.FROM; }
  ";" { yypop(); return SoyTypes.SEMICOLON; }
  {DoubleQuotedStringLiteral} { return SoyTypes.STRING_LITERAL; }
  {SingleQuotedStringLiteral} { return SoyTypes.STRING_LITERAL; }
  {IdentifierWord} { return SoyTypes.IDENTIFIER_WORD; }
  . { yypop(); return SoyTypes.OTHER; }
}

. { return SoyTypes.OTHER; }
