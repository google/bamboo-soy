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

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Invokes an autopopup whenever a '.' or '$' is typed in.
 */
public class MemberHandler extends TypedHandlerDelegate {

  @Override
  @NotNull
  public Result beforeCharTyped(char charTyped, final @NotNull Project project,
      final @NotNull Editor editor,
      final @NotNull PsiFile file, final @NotNull FileType fileType) {
    if (charTyped == '.' || charTyped == '$') {
      AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, Conditions.notNull());
    }
    return Result.CONTINUE;
  }
}
