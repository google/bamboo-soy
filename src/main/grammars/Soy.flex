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
CssIdentifierChar={IdentifierChar}|"-"

IdentifierWord={IdentifierChar}({IdentifierChar}|{Digit})*
QualifiedIdentifier="."?{IdentifierWord}("."{IdentifierWord})*
CssIdentifierLiteral="%"?{CssIdentifierChar}({CssIdentifierChar}|{Digit})*

/* Line terminators, white space, and comments */
LineTerminator=\r|\n|\r\n
InputCharacter=[^\r\n]
HorizontalSpace=[ \t\f]
WhiteSpace={LineTerminator}|{HorizontalSpace}

CommentContent=([^*]|("*"+[^/*]))*
DocComment="/**"{CommentContent}"*"+"/"
TraditionalComment="/*"{CommentContent}"*"+"/"
DoubleSlashComment="//"{InputCharacter}*{LineTerminator}
HtmlComment="<!--"({InputCharacter}|{LineTerminator})*"-->"
Comment=({WhiteSpace}{DoubleSlashComment})|{TraditionalComment}|{HtmlComment}

/* Integer literal */
DecimalDigit=[0-9]
DecimalDigits={DecimalDigit}+
HexDigit=[0-9A-F]

DecimalIntegerLiteral={DecimalDigits}
SignedDecimalIntegerLiteral=("+"|"-")?{DecimalIntegerLiteral}
HexNumeral=0x{HexDigit}+

IntegerLiteral={HexNumeral}|{SignedDecimalIntegerLiteral}

ExponentPart="e"{SignedDecimalIntegerLiteral}

FloatingPointLiteral=({DecimalDigit}+"."{DecimalDigit}+{ExponentPart}?)|({DecimalDigit}+{ExponentPart}?)

/* String literal */
DoubleQuotedStringLiteral=\"([^\r\n\"\\]|\\.)*\"
SingleQuotedStringLiteral='([^\r\n'\\]|\\.)*'

MultiLineDoubleQuotedStringLiteral=\"([^\"\\]|\\([^]))*\"
MultiLineSingleQuotedStringLiteral='([^'\\]|\\([^]))*'

/* Lexer states */
%state TAG
%state LITERAL
%state TAG_NO_KEYWORD
%state TAG_QUALIFIED_IDENTIFIER

%%

<YYINITIAL,TAG,TAG_QUALIFIED_IDENTIFIER,LITERAL> {
  {WhiteSpace}  { return TokenType.WHITE_SPACE; }
}

<TAG_NO_KEYWORD> {
  {WhiteSpace}  { yybegin(TAG); return TokenType.WHITE_SPACE; }
}


<YYINITIAL,TAG,TAG_QUALIFIED_IDENTIFIER,TAG_NO_KEYWORD,LITERAL> {
  /* Comments */
  ^{DoubleSlashComment} { return SoyTypes.COMMENT_BLOCK; }
  {DocComment} { return SoyTypes.DOC_COMMENT_BLOCK; }
  {Comment} { return SoyTypes.COMMENT_BLOCK; }

  "{literal}" { yybegin(LITERAL); return SoyTypes.LITERAL; }
  "{{literal}}" { yybegin(LITERAL); return SoyTypes.LITERAL_DOUBLE; }
  "{" { yybegin(TAG); return SoyTypes.LBRACE; }
  "{{" { yybegin(TAG); return SoyTypes.LBRACE_LBRACE; }
  "{/" { yybegin(TAG); return SoyTypes.LBRACE_SLASH; }
  "{{/" { yybegin(TAG); return SoyTypes.LBRACE_LBRACE_SLASH; }
}

// Inside a tag, not after "." or "$". Keywords can only be here.
<TAG> {
  /* Tag names only followed by a qualified identifier */
  "alias" { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.ALIAS; }
  "call" { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.CALL; }
  "delcall" { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELCALL; }
  "delpackage" { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELPACKAGE; }
  "deltemplate" { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELTEMPLATE; }
  "namespace" { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.NAMESPACE; }
  "template" { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.TEMPLATE; }

  true { return SoyTypes.BOOL_LITERAL; }
  false { return SoyTypes.BOOL_LITERAL; }

  null { return SoyTypes.NULL_LITERAL; }

  /* Tag names */
  "case" { return SoyTypes.CASE; }
  "css" { return SoyTypes.CSS; }
  "default" { return SoyTypes.DEFAULT; }

  "else" { return SoyTypes.ELSE; }
  "elseif" { return SoyTypes.ELSEIF; }
  "fallbackmsg" { return SoyTypes.FALLBACKMSG; }
  "for" { return SoyTypes.FOR; }
  "foreach" { return SoyTypes.FOREACH; }
  "if" { return SoyTypes.IF; }
  "ifempty" { return SoyTypes.IFEMPTY; }
  "lb" { return SoyTypes.LB; }
  "let" { return SoyTypes.LET; }
  "msg" { return SoyTypes.MSG; }

  "nil" { return SoyTypes.NIL; }
  "param" { return SoyTypes.PARAM; }
  "plural" { return SoyTypes.PLURAL; }
  "print" { return SoyTypes.PRINT; }
  "rb" { return SoyTypes.RB; }
  "select" { return SoyTypes.SELECT; }
  "sp" { return SoyTypes.SP; }
  "switch" { return SoyTypes.SWITCH; }
  "xid" { return SoyTypes.XID; }
  "msg" { return SoyTypes.MSG; }

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
  "css" { return SoyTypes.CSS; }
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
}

// Inside a declaration or call tag. Only "as" and identifiers expected.
<TAG_QUALIFIED_IDENTIFIER> {
  {QualifiedIdentifier} { yybegin(TAG); return SoyTypes.QUALIFIED_IDENTIFIER; }
}

// Anywhere inside normal (not declaration/call) tag.
<TAG,TAG_NO_KEYWORD> {
  /* Cannot be followed by a tag name. */
  "." { yybegin(TAG_NO_KEYWORD); return SoyTypes.DOT; }
  "?." { yybegin(TAG_NO_KEYWORD); return SoyTypes.DOT_NULL_CHECK; }
  "$" { yybegin(TAG_NO_KEYWORD); return SoyTypes.DOLLAR; }

  /* Literals */
  {DoubleQuotedStringLiteral} { return SoyTypes.STRING_LITERAL; }
  {SingleQuotedStringLiteral} { return SoyTypes.STRING_LITERAL; }
  {MultiLineDoubleQuotedStringLiteral} { return SoyTypes.MULTI_LINE_STRING_LITERAL; }
  {MultiLineSingleQuotedStringLiteral} { return SoyTypes.MULTI_LINE_STRING_LITERAL; }
  {IntegerLiteral} { return SoyTypes.INTEGER_LITERAL; }
  {FloatingPointLiteral} { return SoyTypes.FLOAT_LITERAL; }

  /* Tag names */
  "@inject" { return SoyTypes.AT_INJECT; }
  "@inject?" { return SoyTypes.AT_INJECT_OPT; }
  "@param" { return SoyTypes.AT_PARAM; }
  "@param?" { return SoyTypes.AT_PARAM_OPT; }
  "\\r" { return SoyTypes.CARRIAGE_RETURN; }
  "\\n" { return SoyTypes.NEWLINE_LITERAL; }
  "\\t" { return SoyTypes.TAB; }
/*
  "/call" { return SoyTypes.END_CALL; }
  "/delcall" { return SoyTypes.END_DELCALL; }
  "/deltemplate" { return SoyTypes.END_DELTEMPLATE; }
  "/foreach" { return SoyTypes.END_FOREACH; }
  "/for" { return SoyTypes.END_FOR; }
  "/if" { return SoyTypes.END_IF; }
  "/let" { return SoyTypes.END_LET; }
  "/msg" { return SoyTypes.END_MSG; }
  "/param" { return SoyTypes.END_PARAM; }
  "/plural" { return SoyTypes.END_PLURAL; }
  "/select" { return SoyTypes.END_SELECT; }
  "/switch" { return SoyTypes.END_SWITCH; }
  "/template" { return SoyTypes.END_TEMPLATE; }
*/
  "=" { return SoyTypes.EQUAL; }
  ":" { return SoyTypes.COLON; }
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
  {CssIdentifierLiteral} { return SoyTypes.CSS_IDENTIFIER_LITERAL; }
}

// Anywhere inside a tag.
<TAG,TAG_NO_KEYWORD,TAG_QUALIFIED_IDENTIFIER> {
  /* Tag closing */
  "/}" { yybegin(YYINITIAL); return SoyTypes.SLASH_RBRACE; }
  "}" { yybegin(YYINITIAL); return SoyTypes.RBRACE; }
  "}}" { yybegin(YYINITIAL); return SoyTypes.RBRACE_RBRACE; }
  "/}}" { yybegin(YYINITIAL); return SoyTypes.SLASH_RBRACE_RBRACE; }
}

<LITERAL> {
  "{/literal}" { yybegin(YYINITIAL); return SoyTypes.END_LITERAL; }
  "{{/literal}}" { yybegin(YYINITIAL); return SoyTypes.END_LITERAL_DOUBLE; }
}

. { return SoyTypes.OTHER; }
