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

import static com.google.bamboo.soy.parser.SoyTypes.OTHER;

import com.google.bamboo.soy.SoyLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutors;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class SoyFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider
    implements ConfigurableTemplateLanguageFileViewProvider {
  // Base language, like HTML
  private final Language baseLanguage;

  // Template language, like Soy
  private final Language templateLanguage;

  public SoyFileViewProvider(
      PsiManager manager, VirtualFile file, boolean physical, Language baseLanguage) {
    this(manager, file, physical, baseLanguage, getTemplateDataLanguage(manager, file));
  }

  public SoyFileViewProvider(
      PsiManager manager,
      VirtualFile file,
      boolean physical,
      Language baseLanguage,
      Language templateLanguage) {
    super(manager, file, physical);
    this.baseLanguage = baseLanguage;
    this.templateLanguage = templateLanguage;
  }

  @Override
  public boolean supportsIncrementalReparse(@NotNull Language rootLanguage) {
    return false;
  }

  @NotNull
  private static Language getTemplateDataLanguage(PsiManager manager, VirtualFile file) {
    Language dataLang =
        TemplateDataLanguageMappings.getInstance(manager.getProject()).getMapping(file);
    if (dataLang == null) {
      dataLang = SoyLanguage.getDefaultTemplateLang().getLanguage();
    }

    Language substituteLang =
        LanguageSubstitutors.INSTANCE.substituteLanguage(dataLang, file, manager.getProject());

    // only use a substituted language if it's templateable
    if (TemplateDataLanguageMappings.getTemplateableLanguages().contains(substituteLang)) {
      dataLang = substituteLang;
    }

    return dataLang;
  }

  @NotNull
  @Override
  public Language getBaseLanguage() {
    return baseLanguage;
  }

  @NotNull
  @Override
  public Language getTemplateDataLanguage() {
    return templateLanguage;
  }

  @NotNull
  @Override
  public Set<Language> getLanguages() {
    return new HashSet<>(Arrays.asList(new Language[] {baseLanguage, templateLanguage}));
  }

  @Override
  protected MultiplePsiFilesPerDocumentFileViewProvider cloneInner(VirtualFile virtualFile) {
    return new SoyFileViewProvider(
        getManager(), virtualFile, false, baseLanguage, templateLanguage);
  }

  @Override
  protected PsiFile createFile(@NotNull Language lang) {
    ParserDefinition parserDefinition = getDefinition(lang);
    if (parserDefinition == null) {
      return null;
    }

    if (lang.is(templateLanguage)) {
      PsiFileImpl file = (PsiFileImpl) parserDefinition.createFile(this);
      file.setContentElementType(
          new TemplateDataElementType("CLOSURE_TEMPLATE_DATA", lang, OTHER, OTHER));
      return file;
    } else if (lang.isKindOf(baseLanguage)) {
      return parserDefinition.createFile(this);
    } else {
      return null;
    }
  }

  private ParserDefinition getDefinition(Language lang) {
    return LanguageParserDefinitions.INSTANCE.forLanguage(lang);
  }
}
