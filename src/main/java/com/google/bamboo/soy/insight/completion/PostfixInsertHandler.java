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

package com.google.bamboo.soy.insight.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;

public class PostfixInsertHandler implements InsertHandler<LookupElement> {
  private final String closingTagBeforeCaret;
  private final String closingTagAfterCaret;

  public PostfixInsertHandler(String closingTagBeforeCaret, String closingTagAfterCaret) {
    this.closingTagBeforeCaret = closingTagBeforeCaret;
    this.closingTagAfterCaret = closingTagAfterCaret;
  }

  public void handleInsert(InsertionContext context, LookupElement item) {
    Editor editor = context.getEditor();
    Project project = editor.getProject();

    if (project != null) {
      EditorModificationUtil.insertStringAtCaret(
          editor, closingTagBeforeCaret + closingTagAfterCaret);
      PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
      EditorModificationUtil.moveCaretRelatively(editor, -closingTagAfterCaret.length());
    }
  }
}
