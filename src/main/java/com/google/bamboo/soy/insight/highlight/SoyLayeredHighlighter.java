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

package com.google.bamboo.soy.insight.highlight;

import static com.google.bamboo.soy.parser.SoyTypes.OTHER;

import com.google.bamboo.soy.SoyLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyLayeredHighlighter extends LayeredLexerEditorHighlighter {
  public SoyLayeredHighlighter(
      @Nullable Project project,
      @Nullable VirtualFile virtualFile,
      @NotNull EditorColorsScheme colors) {
    // Creating main highlighter.
    super(new SoySyntaxHighlighter(), colors);

    // Highlighter for the outer language.
    FileType type = null;
    if (project == null || virtualFile == null) {
      type = StdFileTypes.PLAIN_TEXT;
    } else {
      Language language = TemplateDataLanguageMappings.getInstance(project).getMapping(virtualFile);
      if (language != null) type = language.getAssociatedFileType();
      if (type == null) type = SoyLanguage.getDefaultTemplateLang();
    }

    SyntaxHighlighter outerHighlighter =
        SyntaxHighlighterFactory.getSyntaxHighlighter(type, project, virtualFile);

    registerLayer(OTHER, new LayerDescriptor(outerHighlighter, ""));
  }
}
