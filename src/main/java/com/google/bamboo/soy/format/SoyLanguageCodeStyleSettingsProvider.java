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

import com.google.bamboo.soy.SoyLanguage;
import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class SoyLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

  @NotNull
  @Override
  public Language getLanguage() {
    return SoyLanguage.INSTANCE;
  }

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    switch (settingsType) {
      case INDENT_SETTINGS:
        return loadSample("IndentSettings");
      default:
        return null;
    }
  }

  @Override
  public CommonCodeStyleSettings getDefaultCommonSettings() {
    return CodeStyleSettingsManager.getInstance().getCurrentSettings()
        .getCommonSettings(HTMLLanguage.INSTANCE);
  }

  @Override
  public IndentOptionsEditor getIndentOptionsEditor() {
    return new SmartIndentOptionsEditor();
  }

  private String loadSample(String name) {
    try {
      return StreamUtil
          .readText(getClass().getClassLoader().getResourceAsStream("codeSamples/" + name + ".soy"),
              "UTF-8");
    } catch (IOException e) {
      return "";
    }
  }
}
