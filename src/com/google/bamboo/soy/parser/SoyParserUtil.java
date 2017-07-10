package com.google.bamboo.soy.parser;

import static com.google.bamboo.soy.parser.SoyTypes.DEL_CALL_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.DIRECT_CALL_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.END_CALL;
import static com.google.bamboo.soy.parser.SoyTypes.END_DELCALL;
import static com.google.bamboo.soy.parser.SoyTypes.END_DELTEMPLATE;
import static com.google.bamboo.soy.parser.SoyTypes.END_FOR;
import static com.google.bamboo.soy.parser.SoyTypes.END_FOREACH;
import static com.google.bamboo.soy.parser.SoyTypes.END_IF;
import static com.google.bamboo.soy.parser.SoyTypes.END_LET;
import static com.google.bamboo.soy.parser.SoyTypes.END_MSG;
import static com.google.bamboo.soy.parser.SoyTypes.END_PARAM;
import static com.google.bamboo.soy.parser.SoyTypes.END_PLURAL;
import static com.google.bamboo.soy.parser.SoyTypes.END_SELECT;
import static com.google.bamboo.soy.parser.SoyTypes.END_SWITCH;
import static com.google.bamboo.soy.parser.SoyTypes.END_TEMPLATE;
import static com.google.bamboo.soy.parser.SoyTypes.FOREACH_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.FOR_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.IF_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.LBRACE;
import static com.google.bamboo.soy.parser.SoyTypes.LBRACE_LBRACE;
import static com.google.bamboo.soy.parser.SoyTypes.LET_COMPOUND_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.MSG_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.PARAM_LIST_ELEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.PLURAL_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.SELECT_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.SWITCH_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.TEMPLATE_BLOCK;

import com.google.common.collect.ImmutableMap;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;

public class SoyParserUtil extends GeneratedParserUtilBase {
  private static ImmutableMap<IElementType, IElementType> closingTokenToBlock =
      ImmutableMap.<IElementType, IElementType>builder()
          .put(END_CALL, DIRECT_CALL_STATEMENT)
          .put(END_DELCALL, DEL_CALL_STATEMENT)
          .put(END_DELTEMPLATE, TEMPLATE_BLOCK)
          .put(END_FOREACH, FOREACH_STATEMENT)
          .put(END_FOR, FOR_STATEMENT)
          .put(END_IF, IF_STATEMENT)
          .put(END_LET, LET_COMPOUND_STATEMENT)
          .put(END_MSG, MSG_STATEMENT)
          .put(END_PARAM, PARAM_LIST_ELEMENT)
          .put(END_PLURAL, PLURAL_STATEMENT)
          .put(END_SELECT, SELECT_STATEMENT)
          .put(END_SWITCH, SWITCH_STATEMENT)
          .put(END_TEMPLATE, TEMPLATE_BLOCK)
          .build();

  /* Matches a closing tag iff there is frame in the stack which it may close. */
  public static boolean parseEndOfStatementBlock(PsiBuilder builder, int level) {
    if (!nextTokenIs(builder, "", LBRACE, LBRACE_LBRACE)) return false;
    Marker marker = enter_section_(builder);
    boolean r = consumeToken(builder, LBRACE) || consumeToken(builder, LBRACE_LBRACE);

    IElementType block = null;
    for (IElementType token : closingTokenToBlock.keySet()) {
      if (consumeToken(builder, token)) {
        block = closingTokenToBlock.get(token);
        break;
      }
    }
    if (block == null) {
      // None of the expected closing tags found.
      exit_section_(builder, marker, null, false);
      return false;
    }

    Frame frame = ErrorState.get(builder).currentFrame;
    while (frame != null) {
      if (frame.elementType == block) {
        // Found a frame that matches the closing tag.
        exit_section_(builder, marker, null, true);
        return true;
      }
      frame = frame.parentFrame;
    }

    // No matching frame found.
    exit_section_(builder, marker, null, false);
    return false;
  }
}
