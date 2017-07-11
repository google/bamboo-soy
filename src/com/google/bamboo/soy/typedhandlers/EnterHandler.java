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

package com.google.bamboo.soy.typedhandlers;

import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.file.SoyFileType;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.common.collect.ImmutableMultimap;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
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
import com.intellij.psi.tree.IElementType;
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
  @Override
  public Result preprocessEnter(
      @NotNull PsiFile psiFile,
      @NotNull Editor editor,
      @NotNull Ref<Integer> caretOffset,
      @NotNull Ref<Integer> caretOffsetChange,
      @NotNull DataContext dataContext,
      @Nullable EditorActionHandler originalHandler) {
    if (psiFile instanceof SoyFile && isBetweenBlockDefiningTags(psiFile, caretOffset.get())) {
      originalHandler.execute(editor, dataContext);
      return Result.Default;
    }
    return Result.Continue;
  }

  @Override
  public Result postProcessEnter(
      @NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
    if (file.getFileType() != SoyFileType.INSTANCE) return Result.Continue;

    int caretOffset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(caretOffset);
    Document document = editor.getDocument();

    int lineNumber = document.getLineNumber(caretOffset) - 1;
    int lineOffset = document.getLineStartOffset(lineNumber);
    String line = document.getText(new TextRange(lineOffset, caretOffset));

    if (element instanceof PsiComment) {
      handleEnterInComment(element, file, editor);
    } else if (line.startsWith("/*")) {
      insertText(file, editor, " * \n ", 3);
    }

    return Result.Continue;
  }

  private static void handleEnterInComment(
      PsiElement element, @NotNull PsiFile file, @NotNull Editor editor) {
    if (element.getText().startsWith("/*")) {
      int offset = editor.getCaretModel().getOffset();
      int lineNumber = editor.getDocument().getLineNumber(offset);
      int lineStartOffset = editor.getDocument().getLineStartOffset(lineNumber);

      String lineTextBeforeCaret =
          editor.getDocument().getText(new TextRange(lineStartOffset, offset));
      String toInsert = lineTextBeforeCaret.equals("") ? " * " : "* ";
      insertText(file, editor, toInsert, toInsert.length());
    }
  }

  private static void insertText(PsiFile file, Editor editor, String text, int numChar) {
    EditorModificationUtil.insertStringAtCaret(editor, text, false, numChar);
    PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
  }

  private static final ImmutableMultimap<IElementType, IElementType> rightTagToLeftTag =
      ImmutableMultimap.<IElementType, IElementType>builder()
          .put(SoyTypes.END_CALL_TAG, SoyTypes.BEGIN_CALL)
          .put(SoyTypes.END_DEL_CALL_TAG, SoyTypes.BEGIN_DEL_CALL)
          .put(SoyTypes.END_DEL_TEMPLATE_TAG, SoyTypes.BEGIN_DELEGATE_TEMPLATE)
          .put(SoyTypes.END_FOR_TAG, SoyTypes.BEGIN_FOR)
          .putAll(SoyTypes.END_FOREACH_TAG, SoyTypes.BEGIN_FOREACH, SoyTypes.IF_EMPTY_TAG)
          .putAll(SoyTypes.IF_EMPTY_TAG, SoyTypes.BEGIN_FOREACH)
          .putAll(SoyTypes.END_IF_TAG, SoyTypes.BEGIN_IF, SoyTypes.BEGIN_ELSE_IF, SoyTypes.ELSE_TAG)
          .putAll(SoyTypes.ELSE_TAG, SoyTypes.BEGIN_IF, SoyTypes.BEGIN_ELSE_IF)
          .putAll(SoyTypes.BEGIN_ELSE_IF, SoyTypes.BEGIN_IF)
          .putAll(SoyTypes.END_LET_TAG, SoyTypes.BEGIN_LET)
          .putAll(SoyTypes.END_MSG_TAG, SoyTypes.BEGIN_MSG)
          .putAll(SoyTypes.END_PARAM_TAG, SoyTypes.BEGIN_PARAM_TAG)
          .putAll(
              SoyTypes.END_PLURAL_TAG,
              SoyTypes.BEGIN_PLURAL,
              SoyTypes.CASE_CLAUSE,
              SoyTypes.DEFAULT_CLAUSE)
          .putAll(
              SoyTypes.END_SELECT_TAG,
              SoyTypes.BEGIN_SELECT_STATEMENT,
              SoyTypes.CASE_CLAUSE,
              SoyTypes.DEFAULT_CLAUSE)
          .putAll(
              SoyTypes.END_SWITCH_TAG,
              SoyTypes.BEGIN_SWITCH,
              SoyTypes.CASE_CLAUSE,
              SoyTypes.DEFAULT_CLAUSE)
          .putAll(
              SoyTypes.DEFAULT_CLAUSE,
              SoyTypes.BEGIN_PLURAL,
              SoyTypes.BEGIN_SWITCH,
              SoyTypes.BEGIN_SELECT_STATEMENT,
              SoyTypes.CASE_CLAUSE)
          .putAll(
              SoyTypes.CASE_CLAUSE,
              SoyTypes.BEGIN_PLURAL,
              SoyTypes.BEGIN_SWITCH,
              SoyTypes.BEGIN_SELECT_STATEMENT,
              SoyTypes.CASE_CLAUSE)
          .putAll(SoyTypes.END_TEMPLATE_TAG, SoyTypes.BEGIN_TEMPLATE)
          .build();

  /**
   * Method deciding whether the following transformation is applicable:
   * from
   * {left}<caret>{right}
   * to
   * {left}
   *   <caret>
   * {right}
   */
  private static boolean isBetweenBlockDefiningTags(PsiFile psiFile, int caretOffset) {
    PsiElement nextElement = psiFile.findElementAt(caretOffset);
    if (nextElement == null
        || !nextElement.getText().equals("{") && !nextElement.getText().equals("{{")) {
      return false;
    }
    PsiElement nextTag = nextElement.getParent();
    PsiElement prevTag = nextTag.getPrevSibling();
    return nextTag != null
        && prevTag != null
        && rightTagToLeftTag.containsKey(nextTag.getNode().getElementType())
        && rightTagToLeftTag
            .get(nextTag.getNode().getElementType())
            .contains(prevTag.getNode().getElementType());
  }
}
