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

package com.google.bamboo.soy.file;

import com.google.bamboo.soy.SoyLanguage;
import com.google.bamboo.soy.highlight.SoyLayeredHighlighter;
import com.google.bamboo.soy.icons.SoyIcons;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeEditorHighlighterProviders;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.TemplateLanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import javax.swing.*;
import org.jetbrains.annotations.*;

public class SoyFileType extends LanguageFileType implements TemplateLanguageFileType {
  public static final SoyFileType INSTANCE = new SoyFileType();

  private SoyFileType() {
    super(SoyLanguage.INSTANCE);
    FileTypeEditorHighlighterProviders.INSTANCE.addExplicitExtension(
        this,
        (@Nullable Project project,
            @NotNull FileType fileType,
            @Nullable VirtualFile virtualFile,
            @NotNull EditorColorsScheme editorColorsScheme) ->
            new SoyLayeredHighlighter(project, virtualFile, editorColorsScheme));
  }

  @NotNull
  @Override
  public String getName() {
    return "Closure template file";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Closure template file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "soy";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return SoyIcons.FILE;
  }
}
