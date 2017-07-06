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

package com.google.bamboo.soy;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** IntelliJ programming language object describing closure templates. */
public class SoyLanguage extends Language {
  public static final SoyLanguage INSTANCE = new SoyLanguage();

  private SoyLanguage() {
    super("ClosureTemplate");
  }

  public SoyLanguage(
      @Nullable Language baseLanguage,
      @NotNull @NonNls final String ID,
      @NotNull @NonNls final String... mimeTypes) {
    super(baseLanguage, ID, mimeTypes);
  }

  public static LanguageFileType getDefaultTemplateLang() {
    return StdFileTypes.HTML;
  }
}
