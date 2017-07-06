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

import com.google.bamboo.soy.SoyLanguage;
import com.google.bamboo.soy.file.SoyFileType;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Inserts appropriate characters and indentation after pressing "Enter" in a closure template file.
 *
 * <p>In comments this handler inserts "*" on the next line and moves the cursor behind it when
 * pressing "Enter".
 *
 * <p>If pressed right after an opening tag this handler will indent the cursor on the next line.
 */
public class EnterHandler implements EnterHandlerDelegate {
  private final PsiElementPattern.Capture<PsiElement> openTagMatcher =
      psiElement()
          .andOr(
              psiElement(SoyBeginTemplate.class),
              psiElement(SoyBeginCall.class),
              psiElement(SoyBeginDelCall.class),
              psiElement(SoyBeginDelegateTemplate.class),
              psiElement(SoyBeginIf.class),
              psiElement(SoyBeginElseIf.class),
              psiElement(SoyBeginFor.class),
              psiElement(SoyBeginForeach.class),
              psiElement(SoyBeginCaseClause.class),
              psiElement(SoyBeginLet.class),
              psiElement(SoyBeginMsg.class),
              psiElement(SoyBeginSwitch.class),
              psiElement(SoyBeginParamTag.class));

  @Override
  public Result preprocessEnter(
      @NotNull PsiFile psiFile,
      @NotNull Editor editor,
      @NotNull Ref<Integer> ref,
      @NotNull Ref<Integer> ref1,
      @NotNull DataContext dataContext,
      @Nullable EditorActionHandler editorActionHandler) {
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
      insertText(file, editor, " * \n */", 3);
    } else if (element != null) {
      // Add indentation if pressed enter after an open tag.
      PsiElement previousElement = getPreviousElement(element);

      while (previousElement instanceof PsiWhiteSpace) {
        previousElement = getPreviousElement(previousElement);
      }

      if (previousElement == null) return Result.Continue;

      int caretLineNumber = editor.getDocument().getLineNumber(caretOffset);

      // PsiElement.getTextOffset() returns -1 if the caret is at the start of the document so we
      // need to handle to special case it here.
      int tagLineNumber =
          previousElement.getTextOffset() > 0
              ? editor.getDocument().getLineNumber(previousElement.getTextOffset())
              : 0;
      boolean isAfterOpenTag =
          openTagMatcher.accepts(previousElement) && (caretLineNumber - tagLineNumber <= 1);

      if (isAfterOpenTag) {
        CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(SoyLanguage.INSTANCE);
        CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();

        String indentChar = indentOptions.USE_TAB_CHARACTER ? "\t" : " ";
        int indentSize = 2; // TODO(thso): Get this from settings.
        int numChar = indentSize / (indentOptions.USE_TAB_CHARACTER ? 2 : 1);
        Collection<String> spaces = Collections.nCopies(numChar, indentChar);

        insertText(file, editor, String.join("", spaces), numChar);
      }
    }

    return Result.Continue;
  }

  private void handleEnterInComment(
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

  private void insertText(PsiFile file, Editor editor, String text, int numChar) {
    EditorModificationUtil.insertStringAtCaret(editor, text, false, numChar);
    PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
  }

  private PsiElement getPreviousElement(PsiElement element) {
    while (element.getPrevSibling() == null) {
      element = element.getParent();
      if (element == null) return null;
    }

    return element.getPrevSibling();
  }
}
