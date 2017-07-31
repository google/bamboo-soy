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

import com.google.bamboo.soy.elements.TagElement;
import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.file.SoyFileType;
import com.google.bamboo.soy.parser.SoyBeginChoiceClause;
import com.google.bamboo.soy.parser.SoyBeginParamTag;
import com.google.bamboo.soy.parser.SoyChoiceClause;
import com.google.bamboo.soy.parser.SoyParamListElement;
import com.google.bamboo.soy.parser.SoyStatementList;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Inserts appropriate characters and indentation after pressing "Enter" in a closure template file.
 *
 * <p>In comments this handler inserts "*" on the next line and moves the cursor behind it when
 * pressing "Enter".
 *
 * <p>If pressed right after an opening tag this handler will indent the cursor on the next line.
 */
public class EnterHandler extends EnterHandlerDelegateAdapter {

  private static final Logger LOG = Logger.getInstance(EnterHandler.class);

  private static void handleEnterInComment(
      PsiElement element, @NotNull PsiFile file, @NotNull Editor editor) {
    if (element.getText().startsWith("/*")) {
      Document document = editor.getDocument();

      int caretOffset = editor.getCaretModel().getOffset();
      int lineNumber = document.getLineNumber(caretOffset);

      String lineTextBeforeCaret =
          document.getText(new TextRange(document.getLineStartOffset(lineNumber), caretOffset));
      String lineTextAfterCaret =
          document.getText(new TextRange(caretOffset, document.getLineEndOffset(lineNumber)));

      if (lineTextAfterCaret.equals("*/")) {
        return;
      }

      String toInsert = lineTextBeforeCaret.equals("") ? " * " : "* ";
      insertText(file, editor, toInsert, toInsert.length());
    }
  }

  private static void insertText(PsiFile file, Editor editor, String text, int numChar) {
    EditorModificationUtil.insertStringAtCaret(editor, text, false, numChar);
    PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
  }

  /**
   * Method deciding whether the following transformation is applicable: from
   *
   * <pre><code>
   *   {left}<caret>{right}
   * </code></pre>
   *
   * to
   *
   * <pre<code>
   * {left}
   * <caret>
   * {right}
   * </code></pre>
   */
  private static boolean isBetweenSiblingTags(PsiFile psiFile, int caretOffset) {
    PsiElement nextElement = psiFile.findElementAt(caretOffset);
    if (nextElement == null || nextElement.getParent() == null) {
      return false;
    }
    nextElement = nextElement.getParent();
    if (nextElement instanceof SoyBeginChoiceClause || nextElement instanceof SoyBeginParamTag) {
      nextElement = nextElement.getParent();
      PsiElement prevElement = PsiTreeUtil
          .skipSiblingsBackward(nextElement, SoyStatementList.class);
      return prevElement instanceof SoyChoiceClause || prevElement instanceof SoyParamListElement;
    } else {
      PsiElement prevElement = PsiTreeUtil
          .skipSiblingsBackward(nextElement, SoyStatementList.class);

      return prevElement instanceof TagElement && nextElement instanceof TagElement;
    }
  }

  @Override
  public Result preprocessEnter(
      @NotNull PsiFile psiFile,
      @NotNull Editor editor,
      @NotNull Ref<Integer> caretOffset,
      @NotNull Ref<Integer> caretOffsetChange,
      @NotNull DataContext dataContext,
      @Nullable EditorActionHandler originalHandler) {
    if (psiFile instanceof SoyFile && isBetweenSiblingTags(psiFile, caretOffset.get())) {
      if (originalHandler != null) {
        originalHandler.execute(editor, dataContext);
      }
      return Result.Default;
    }
    return Result.Continue;
  }

  @Override
  public Result postProcessEnter(
      @NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
    if (file.getFileType() != SoyFileType.INSTANCE) {
      return Result.Continue;
    }

    int caretOffset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(caretOffset);
    Document document = editor.getDocument();

    int lineNumber = document.getLineNumber(caretOffset) - 1;
    int lineStartOffset = document.getLineStartOffset(lineNumber);
    String lineTextBeforeCaret = document.getText(new TextRange(lineStartOffset, caretOffset));

    if (element instanceof PsiComment && element.getTextOffset() < caretOffset) {
      handleEnterInComment(element, file, editor);
    } else if (lineTextBeforeCaret.startsWith("/*")) {
      insertText(file, editor, " * \n ", 3);
    }

    return Result.Continue;
  }
}
