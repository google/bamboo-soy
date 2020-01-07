package com.google.bamboo.soy.parser;

import static com.google.bamboo.soy.parser.SoyTypes.CALL;
import static com.google.bamboo.soy.parser.SoyTypes.DELCALL;
import static com.google.bamboo.soy.parser.SoyTypes.DELTEMPLATE;
import static com.google.bamboo.soy.parser.SoyTypes.DEL_CALL_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.DIRECT_CALL_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.DOC_COMMENT_BLOCK;
import static com.google.bamboo.soy.parser.SoyTypes.ELEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.FOR;
import static com.google.bamboo.soy.parser.SoyTypes.FOREACH;
import static com.google.bamboo.soy.parser.SoyTypes.FOREACH_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.FOR_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.IF;
import static com.google.bamboo.soy.parser.SoyTypes.IF_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.LBRACE_LBRACE_SLASH;
import static com.google.bamboo.soy.parser.SoyTypes.LBRACE_SLASH;
import static com.google.bamboo.soy.parser.SoyTypes.LET;
import static com.google.bamboo.soy.parser.SoyTypes.LET_COMPOUND_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.MSG;
import static com.google.bamboo.soy.parser.SoyTypes.MSG_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.PARAM;
import static com.google.bamboo.soy.parser.SoyTypes.PARAM_LIST_ELEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.PLURAL;
import static com.google.bamboo.soy.parser.SoyTypes.PLURAL_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.SELECT;
import static com.google.bamboo.soy.parser.SoyTypes.SELECT_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.SWITCH;
import static com.google.bamboo.soy.parser.SoyTypes.SWITCH_STATEMENT;
import static com.google.bamboo.soy.parser.SoyTypes.TEMPLATE;
import static com.google.bamboo.soy.parser.SoyTypes.TEMPLATE_BLOCK;

import com.google.common.collect.ImmutableMap;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.lang.WhitespacesAndCommentsBinder.RecursiveBinder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoyParserUtil extends GeneratedParserUtilBase {

  private static final Pattern NEWLINE_PATTERN = Pattern.compile("\r\n|\r|\n");

  /**
   * Binds the last leading doc comment either on the same or previous line.
   */
  public static WhitespacesAndCommentsBinder LEADING_COMMENTS_BINDER =
      new WhitespacesAndCommentsBinder.RecursiveBinder() {
        @Override
        public int getEdgePosition(List<IElementType> tokens, boolean atStreamEdge,
            TokenTextGetter getter) {
          int newLinesFound = 0;
          for (int i = tokens.size() - 1; i > 0; i--) {
            if (tokens.get(i) == DOC_COMMENT_BLOCK) {
              return i;
            }
            newLinesFound += countNewlines(getter.get(i));
            if (newLinesFound > 1) {
              break;
            }
          }
          return tokens.size();
        }
      };

  /**
   * Binds the trailing doc comments on the same line
   */
  public static WhitespacesAndCommentsBinder TRAILING_COMMENTS_BINDER =
      new WhitespacesAndCommentsBinder.RecursiveBinder() {
        @Override
        public int getEdgePosition(List<IElementType> tokens, boolean atStreamEdge,
            TokenTextGetter getter) {
          int edgePosition = 0;
          for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i) == DOC_COMMENT_BLOCK) {
              edgePosition = i + 1;
            }
            if (countNewlines(getter.get(i)) > 0) {
              break;
            }
          }
          return edgePosition;
        }
      };
  private static ImmutableMap<IElementType, IElementType> closingTokenToBlock =
      ImmutableMap.<IElementType, IElementType>builder()
          .put(CALL, DIRECT_CALL_STATEMENT)
          .put(DELCALL, DEL_CALL_STATEMENT)
          .put(DELTEMPLATE, TEMPLATE_BLOCK)
          .put(ELEMENT, TEMPLATE_BLOCK)
          .put(FOREACH, FOREACH_STATEMENT)
          .put(FOR, FOR_STATEMENT)
          .put(IF, IF_STATEMENT)
          .put(LET, LET_COMPOUND_STATEMENT)
          .put(MSG, MSG_STATEMENT)
          .put(PARAM, PARAM_LIST_ELEMENT)
          .put(PLURAL, PLURAL_STATEMENT)
          .put(SELECT, SELECT_STATEMENT)
          .put(SWITCH, SWITCH_STATEMENT)
          .put(TEMPLATE, TEMPLATE_BLOCK)
          .build();

  private static int countNewlines(CharSequence s) {
    int n = 0;
    Matcher matcher = NEWLINE_PATTERN.matcher(s);
    while (matcher.find()) {
      n++;
    }
    return n;
  }

  /**
   * Matches a closing tag iff there is frame in the stack which it may close.
   */
  public static boolean parseEndOfStatementBlock(PsiBuilder builder, int level) {
    if (!nextTokenIs(builder, "", LBRACE_SLASH, LBRACE_LBRACE_SLASH)) {
      return false;
    }
    Marker marker = enter_section_(builder);
    boolean r = consumeToken(builder, LBRACE_SLASH) || consumeToken(builder, LBRACE_LBRACE_SLASH);

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
