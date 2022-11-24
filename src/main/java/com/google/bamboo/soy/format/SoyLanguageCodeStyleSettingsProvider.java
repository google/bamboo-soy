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
import com.intellij.application.options.XmlLanguageCodeStyleSettingsProvider;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

public class SoyLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

  @NotNull
  @Override
  public Language getLanguage() {
    return SoyLanguage.INSTANCE;
  }

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    return settingsType == SettingsType.INDENT_SETTINGS
        ? loadIndentSample()
        : null;
  }

  @Override
  protected void customizeDefaults(
      @NotNull CommonCodeStyleSettings commonSettings,
      @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
    CommonCodeStyleSettings settings =
        XmlLanguageCodeStyleSettingsProvider.getDefaultCommonSettings(HTMLLanguage.INSTANCE);
    if (settings == null) {
      return;
    }
    commonSettings.copyFrom(settings);
    indentOptions.copyFrom(settings.getIndentOptions());
  }

  @Override
  public IndentOptionsEditor getIndentOptionsEditor() {
    return new SmartIndentOptionsEditor();
  }

  private String loadIndentSample() {
    try {
      return StreamUtil
          .readText(new InputStreamReader(
              getClass().getClassLoader().getResourceAsStream("codeSamples/IndentSettings.soy"),
              StandardCharsets.UTF_8));
    } catch (IOException e) {
      return "";
    }
  }
}
