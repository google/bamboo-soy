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
CssXidIdentifierChar={IdentifierChar}|"-"|"."

IdentifierWord={IdentifierChar}({IdentifierChar}|{Digit})*
QualifiedIdentifier="."?{IdentifierWord}("."{IdentifierWord})*
CssXidIdentifier="%"?{CssXidIdentifierChar}({CssXidIdentifierChar}|{Digit})*

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

IntegerLiteral={HexNumeral}|{DecimalIntegerLiteral}

ExponentPart="e"{SignedDecimalIntegerLiteral}

FloatingPointLiteral=({DecimalDigit}+"."{DecimalDigit}+{ExponentPart}?)|({DecimalDigit}+{ExponentPart}?)

/* String literal */
DoubleQuotedStringLiteral=\"([^\r\n\"\\]|\\.)*\"
SingleQuotedStringLiteral='([^\r\n'\\]|\\.)*'

MultiLineDoubleQuotedStringLiteral=\"([^\"\\]|\\([^]))*\"
MultiLineSingleQuotedStringLiteral='([^'\\]|\\([^]))*'

NonSemantical=({WhiteSpace}|{DoubleSlashComment}|{DocComment}|{Comment})*

/* Lexer states */
%state TAG
%state LITERAL
%state TAG_CSS_XID
%state TAG_IDENTIFIER_WORD
%state TAG_QUALIFIED_IDENTIFIER

%%

{WhiteSpace}  { return TokenType.WHITE_SPACE; }

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


// Anywhere inside a tag.
<TAG> {
  /* Tag closing */
  "/}" { yybegin(YYINITIAL); return SoyTypes.SLASH_RBRACE; }
  "}" { yybegin(YYINITIAL); return SoyTypes.RBRACE; }
  "}}" { yybegin(YYINITIAL); return SoyTypes.RBRACE_RBRACE; }
  "/}}" { yybegin(YYINITIAL); return SoyTypes.SLASH_RBRACE_RBRACE; }

  /* Tag names that may be followed by a qualified identifier */
  "alias"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.ALIAS; }
  "call"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.CALL; }
  "delcall"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELCALL; }
  "delpackage"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELPACKAGE; }
  "deltemplate"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.DELTEMPLATE; }
  "namespace"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.NAMESPACE; }
  "template"/{NonSemantical}{QualifiedIdentifier} { yybegin(TAG_QUALIFIED_IDENTIFIER); return SoyTypes.TEMPLATE; }

  "alias" { return SoyTypes.ALIAS; }
  "call" { return SoyTypes.CALL; }
  "delcall" { return SoyTypes.DELCALL; }
  "delpackage" { return SoyTypes.DELPACKAGE; }
  "deltemplate" { return SoyTypes.DELTEMPLATE; }
  "namespace" { return SoyTypes.NAMESPACE; }
  "template" { return SoyTypes.TEMPLATE; }

  /* Tag names that may be followed by CSS or Xid identifier */
  "css"/{NonSemantical}{CssXidIdentifier} { yybegin(TAG_CSS_XID); return SoyTypes.CSS; }
  "xid"/{NonSemantical}{CssXidIdentifier} { yybegin(TAG_CSS_XID); return SoyTypes.XID; }

  "css" { return SoyTypes.CSS; }
  "xid" { return SoyTypes.XID; }

  /* Other tag names */
  "@inject" { return SoyTypes.AT_INJECT; }
  "@inject?" { return SoyTypes.AT_INJECT_OPT; }
  "@param" { return SoyTypes.AT_PARAM; }
  "@param?" { return SoyTypes.AT_PARAM_OPT; }
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
  true { return SoyTypes.BOOL_LITERAL; }
  false { return SoyTypes.BOOL_LITERAL; }
  null { return SoyTypes.NULL_LITERAL; }

  /* Maybe followed by IdentifierWord, a special state to not trigger keyword rules. */
  "."/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.DOT; }
  "?."/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.DOT_NULL_CHECK; }
  "$"/{NonSemantical}{IdentifierWord} { yybegin(TAG_IDENTIFIER_WORD); return SoyTypes.DOLLAR; }

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
}

// Only QualifiedIdentifier expected (ensured by look-ahead).
<TAG_QUALIFIED_IDENTIFIER> {
  {QualifiedIdentifier} { yybegin(TAG); return SoyTypes.QUALIFIED_IDENTIFIER; }
}

// Only IdentifierWord expected (ensured by look-ahead).
<TAG_IDENTIFIER_WORD> {
  {IdentifierWord} { yybegin(TAG); return SoyTypes.IDENTIFIER_WORD; }
}

// Only CssXidIdentifier expected (ensured by look-ahead).
<TAG_CSS_XID> {
  {CssXidIdentifier} { yybegin(TAG); return SoyTypes.CSS_XID_IDENTIFIER; }
}

<LITERAL> {
  "{/literal}" { yybegin(YYINITIAL); return SoyTypes.END_LITERAL; }
  "{{/literal}}" { yybegin(YYINITIAL); return SoyTypes.END_LITERAL_DOUBLE; }
}

. { return SoyTypes.OTHER; }
