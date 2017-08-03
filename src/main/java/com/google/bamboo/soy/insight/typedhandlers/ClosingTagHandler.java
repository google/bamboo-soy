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

import com.google.bamboo.soy.elements.TagBlockElement;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
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
    TagBlockElement block = (TagBlockElement) PsiTreeUtil
        .findFirstParent(el, parent -> parent instanceof TagBlockElement);
    if (block != null) {
      return block.getOpeningTag().generateClosingTag();
    }
    return null;
  }

  public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
    myOriginalHandler.execute(editor, charTyped, dataContext);
    if (isMatchForClosingTag(editor, charTyped)) {
      int offset = editor.getCaretModel().getOffset();
      PsiFile file = dataContext.getData(LangDataKeys.PSI_FILE);
      if (file == null) {
        return;
      }
      PsiElement el = file.findElementAt(offset - 1);
      String closingTag = generateClosingTag(el);
      if (closingTag != null) {
        insertClosingTag(editor, offset, closingTag);
      }
    }
  }
}
