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

import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** Inserts a matching single or double quote when typing one. */
public class QuoteHandler implements TypedActionHandler {
  private final Set<Character> matchingQuotes = ImmutableSet.of('"', '\'');
  private final TypedActionHandler myOriginalHandler;

  public QuoteHandler(TypedActionHandler originalHandler) {
    myOriginalHandler = originalHandler;
  }

  public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
    if (matchingQuotes.contains(charTyped)) {
      Document document = editor.getDocument();
      int caretOffset = editor.getCaretModel().getOffset();

      String prevChar = getPreviousChar(document, caretOffset);
      String nextChar = getNextChar(document, caretOffset);

      if (prevChar.equals(" ") && !nextChar.equals(charTyped + "")) {
        document.insertString(editor.getCaretModel().getOffset(), charTyped + "");
        if (editor.getProject() != null) {
          PsiDocumentManager.getInstance(editor.getProject()).commitDocument(document);
        }
      }
    }

    myOriginalHandler.execute(editor, charTyped, dataContext);
  }

  private String getPreviousChar(Document document, int offset) {
    return offset <= 0 ? " " : document.getText(new TextRange(offset - 1, offset));
  }

  private String getNextChar(Document document, int offset) {
    return offset >= document.getTextLength()
        ? " "
        : document.getText(new TextRange(offset, offset + 1));
  }
}
