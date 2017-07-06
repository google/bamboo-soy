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

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

/**
 * Automatically inserts a matching quote when typing one.
 */
public class QuoteHandler implements TypedActionHandler {
  private final TypedActionHandler myOriginalHandler;

  public QuoteHandler(TypedActionHandler originalHandler) {
    myOriginalHandler = originalHandler;
  }

  public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
    if (charTyped == '"') {
      int caretOffset = editor.getCaretModel().getOffset();
      String nextChar =
          caretOffset >= editor.getDocument().getTextLength()
              ? ""
              : editor.getDocument().getText(new TextRange(caretOffset, caretOffset + 1));

      if (!nextChar.equals(charTyped + "")) {
        editor.getDocument().insertString(editor.getCaretModel().getOffset(), charTyped + "");
        if (editor.getProject() != null) {
          PsiDocumentManager.getInstance(editor.getProject()).commitDocument(editor.getDocument());
        }
      }
    }

    myOriginalHandler.execute(editor, charTyped, dataContext);
  }
}
