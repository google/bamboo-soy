package com.google.bamboo.soy.format;

import com.google.bamboo.soy.lexer.SoyTokenTypes;
import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.TokenSet;

public class SoySpacing {

  public static SpacingBuilder getSpacingBuilder(CommonCodeStyleSettings settings) {
    return new SpacingBuilder(settings)
        // Rules defined earlier have higher priority.

        // In a key-value pair no whitespace around equals.
        .aroundInside(SoyTypes.EQUAL, SoyTypes.ATTRIBUTE_KEY_VALUE_PAIR).spaces(0)

        // : should not be preceded by whitespace unless in a ternary expression.
        .beforeInside(SoyTypes.COLON, SoyTypes.NULL_CHECK_TERNARY_EXPR).spaces(1)
        .before(SoyTypes.COLON).spacing(0, 0, 0, false, 0)
        // : should be followed by a space unless in a print directive.
        .afterInside(SoyTypes.COLON, SoyTypes.DIRECTIVE).spaces(0)
        .after(SoyTypes.COLON).spaces(1)

        // , should not be preceded by whitespace.
        .before(SoyTypes.COMMA).spacing(0, 0, 0, false, 0)
        // , should be followed by a space unless in a print directive.
        .afterInside(SoyTypes.COMMA, SoyTypes.DIRECTIVE).spaces(0)
        .after(SoyTypes.COMMA).spaces(1)

        // $ should not be followed by a space.
        .after(SoyTypes.DOLLAR).spacing(0, 0, 0, false, 0)

        // Left brace should not be followed by whitespace.
        .after(SoyTokenTypes.LEFT_BRACES).spacing(0, 0, 0, false, 0)
        // /} and /} should be preceded by a space.
        .before(SoyTokenTypes.SLASH_R_BRACES).spacing(1, 1, 0, false, 0)
        // } and }} should not be preceded by whitespace.
        .before(SoyTokenTypes.RIGHT_BRACES).spacing(0, 0, 0, false, 0)

        // Template contents should be surrounded by line breaks.
        .after(SoyTypes.BEGIN_TEMPLATE).lineBreakInCode()
        .beforeInside(SoyTypes.END_TAG, SoyTypes.TEMPLATE_BLOCK).lineBreakInCode()
        .around(SoyTypes.TEMPLATE_BLOCK).blankLines(1)

        // Blocks surrounded by line breaks.
        .around(SoyTypes.ALIAS_BLOCK).lineBreakInCode()
        .around(SoyTypes.DELEGATE_PACKAGE_BLOCK).lineBreakInCode()
        .around(SoyTypes.NAMESPACE_BLOCK).lineBreakInCode()
        .around(SoyTypes.AT_INJECT_SINGLE).lineBreakInCode()
        .around(SoyTypes.AT_PARAM_SINGLE).lineBreakInCode()
        .around(SoyTypes.CHOICE_CLAUSE).lineBreakInCode()
        .around(SoyTypes.PARAM_LIST_ELEMENT).lineBreakInCode()

        .around(SoyTokenTypes.KEYWORDS).spaces(1)

        // Inside expressions and type expressions.
        .aroundInside(SoyTypes.LESS, TokenSet.create(SoyTypes.MAP_TYPE, SoyTypes.LIST_TYPE)).spaces(0)
        .beforeInside(SoyTypes.GREATER, TokenSet.create(SoyTypes.MAP_TYPE, SoyTypes.LIST_TYPE)).spaces(0)
        .around(SoyTypes.PIPE).spaces(0)
        .afterInside(SoyTokenTypes.UNARY_OPERATORS, SoyTypes.UNARY_EXPR).spaces(0)
        .around(SoyTokenTypes.BINARY_OPERATORS).spaces(1)
        .around(TokenSet.create(SoyTypes.DOT, SoyTypes.DOT_NULL_CHECK)).spaces(0)
        .around(
            TokenSet.create(SoyTypes.SQUARE_OPEN, SoyTypes.INDEX_NULL_CHECK, SoyTypes.PARENS_OPEN))
        .spaces(0)
        .before(TokenSet.create(SoyTypes.SQUARE_CLOSE, SoyTypes.PARENS_CLOSE)).spaces(0);
  }

  public static Spacing getSpacing(CommonCodeStyleSettings settings, Block parent, Block child1,
      Block child2) {
    return getSpacingBuilder(settings).getSpacing(parent, child1, child2);
  }

}
