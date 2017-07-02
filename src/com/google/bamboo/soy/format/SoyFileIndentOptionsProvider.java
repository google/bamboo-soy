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

package com.google.bamboo.soy.format;

import com.google.bamboo.soy.file.SoyFile;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.FileIndentOptionsProvider;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyFileIndentOptionsProvider extends FileIndentOptionsProvider {
  @Nullable
  @Override
  public CommonCodeStyleSettings.IndentOptions getIndentOptions(
      @NotNull CodeStyleSettings settings, @NotNull PsiFile file) {
    if (file instanceof SoyFile) {
      if (file.getViewProvider() instanceof TemplateLanguageFileViewProvider) {
        Language language =
            ((TemplateLanguageFileViewProvider) file.getViewProvider()).getTemplateDataLanguage();
        return settings.getCommonSettings(language).getIndentOptions();
      }
    }
    return null;
  }
}
