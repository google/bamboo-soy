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

package com.google.bamboo.soy.insight.typedhandlers;

import com.google.bamboo.soy.BracedTagUtils;
import com.google.bamboo.soy.elements.StatementBase;
import com.google.bamboo.soy.elements.TagBase;
import com.google.bamboo.soy.parser.SoyParamListElement;
import com.google.bamboo.soy.parser.SoyParserDefinition;
import com.google.bamboo.soy.parser.SoyStatementList;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Automatically inserts a matching closing tag when "{/" is typed.
 */
public class ClosingTagHandler implements TypedActionHandler {

  private final TypedActionHandler myOriginalHandler;

  public ClosingTagHandler(TypedActionHandler originalHandler) {
    myOriginalHandler = originalHandler;
  }

  private static String getTagNameForElement(PsiElement element) {
    if (element instanceof TagBase) {
      return ((TagBase) element).getTagName().name().toLowerCase();
    }

    return null;
  }

  private static boolean isMatchForClosingTag(@NotNull Editor editor, char charTyped) {
    return charTyped == '/'
        && editor.getCaretModel().getOffset() >= 2
        && editor.getDocument().getCharsSequence().charAt(editor.getCaretModel().getOffset() - 2)
        == '{';
  }

  private static void insertClosingTag(@NotNull Editor editor, int offset, String tag) {
    Document document = editor.getDocument();
    CharSequence charSequence = document.getImmutableCharSequence();
    int startPosition = offset - 2;
    // Consume second left brace if present.
    if (offset > 0 && charSequence.charAt(startPosition - 1) == '{') {
      startPosition--;
    }
    int endPosition = offset;
    // Consume at most 2 right braces if present.
    while (endPosition < charSequence.length()
        && charSequence.charAt(endPosition) == '}'
        && endPosition < offset + 2) {
      endPosition++;
    }
    editor.getDocument().replaceString(startPosition, endPosition, tag);
    editor.getCaretModel().moveToOffset(startPosition + tag.length());
    if (editor.getProject() != null) {
      PsiDocumentManager.getInstance(editor.getProject()).commitDocument(document);
    }
  }

  private static String generateClosingTag(PsiElement el) {
    PsiElement prev = null;
    while (el != null && !(el instanceof PsiFile)) {
      if (!isEmptyInlinedStatement(el, prev)) {
        if (getTagNameForElement(el) != null) {
          String closingTag = "{/" + getTagNameForElement(el) + "}";
          // Assuming statement's first child is a braced tag.
          if (BracedTagUtils.isDoubleBraced(el.getFirstChild())) {
            closingTag = "{" + closingTag + "}";
          }
          return closingTag;
        }
      }
      prev = el;
      el = el.getParent();
      // Skipping the StatementList wrapper
      if (el instanceof SoyStatementList) {
        el = el.getParent();
      }
    }
    return null;
  }

  /**
   * There are 3 types of statements:
   *
   * <p>1. Normal statements that always need a closing tag, i.e., {if}, {for}, compound {let} etc.
   *
   * <p>2. Maybe-self-closed statements that would contain the [caretElement] _only_if_ they aren't
   * already closed, i.e. {call} and {delcall}.
   *
   * <p>3. Maybe-self-closed statements that would _always_ contain following statements (and thus,
   * the [caretElement]), i.e. {param}.
   *
   * <p>This method is trying to process the last case. In particular, the following situation:
   * "{param ... /} <whitespace only> {/<caret>" in which the {param} tags should not be attempted
   * to close.
   */
  private static boolean isEmptyInlinedStatement(PsiElement statement, PsiElement caretElement) {
    if (!(statement instanceof SoyParamListElement)
        || caretElement.getParent().getParent() != statement) {
      return false;
    }

    // If {param} is not self-closed, do not skip.
    if (!BracedTagUtils.isSelfClosed(statement.getFirstChild())) {
      return false;
    }

    // If there are non-whitespace statements inside the {param} preceding current
    // editing position, do not skip.
    // Skipping the begin_*_tag and diving into StatementList
    PsiElement statementList = PsiTreeUtil.findChildOfType(statement, SoyStatementList.class);
    if (statementList == null) {
      return false;
    }
    PsiElement child = statementList.getFirstChild();
    while (child != null && !child.isEquivalentTo(caretElement)) {
      if (!(SoyParserDefinition.WHITE_SPACES.contains(child.getNode().getElementType()))) {
        return false;
      }
      child = child.getNextSibling();
    }

    // Otherwise, skip.
    return true;
  }

  public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
    myOriginalHandler.execute(editor, charTyped, dataContext);
    if (isMatchForClosingTag(editor, charTyped)) {
      int offset = editor.getCaretModel().getOffset();
      PsiElement el = dataContext.getData(LangDataKeys.PSI_FILE).findElementAt(offset - 1);
      String closingTag = generateClosingTag(el);
      if (closingTag != null) {
        insertClosingTag(editor, offset, closingTag);
      }
    }
  }
}
