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
import com.google.common.collect.ImmutableSet;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.OuterLanguageElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SoyFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider
    implements ConfigurableTemplateLanguageFileViewProvider {

  // Base language, a template language like Soy
  private static final Language BASE_LANGUAGE = SoyLanguage.INSTANCE;

  // Template data language, like HTML
  private static final Language TEMPLATE_DATA_LANGUAGE = HTMLLanguage.INSTANCE;

  public static final IElementType OUTER_ELEMENT_TYPE =
      new OuterLanguageElementType("CLOSURE_TEMPLATE_DATA in Soy", SoyLanguage.INSTANCE);

  // Element type for template data language
  private static final TemplateDataElementType TEMPLATE_DATA_ELEMENT_TYPE =
      new TemplateDataElementType("CLOSURE_TEMPLATE_DATA", TEMPLATE_DATA_LANGUAGE, OTHER, OUTER_ELEMENT_TYPE);

  SoyFileViewProvider(PsiManager manager, VirtualFile file, boolean physical) {
    super(manager, file, physical);
  }

  @Override
  public boolean supportsIncrementalReparse(@NotNull Language rootLanguage) {
    return false;
  }

  @NotNull
  @Override
  public Language getBaseLanguage() {
    return BASE_LANGUAGE;
  }

  @NotNull
  @Override
  public Language getTemplateDataLanguage() {
    return TEMPLATE_DATA_LANGUAGE;
  }

  @NotNull
  @Override
  public Set<Language> getLanguages() {
    return ImmutableSet.of(BASE_LANGUAGE, TEMPLATE_DATA_LANGUAGE);
  }

  @NotNull
  @Override
  protected MultiplePsiFilesPerDocumentFileViewProvider cloneInner(
      @NotNull VirtualFile virtualFile) {
    return new SoyFileViewProvider(getManager(), virtualFile, false);
  }

  @Override
  protected PsiFile createFile(@NotNull Language lang) {
    ParserDefinition parserDefinition = getDefinition(lang);
    if (parserDefinition == null) {
      return null;
    }

    if (lang.is(TEMPLATE_DATA_LANGUAGE)) {
      PsiFileImpl file = (PsiFileImpl) parserDefinition.createFile(this);
      file.setContentElementType(TEMPLATE_DATA_ELEMENT_TYPE);
      return file;
    } else if (lang.isKindOf(BASE_LANGUAGE)) {
      return parserDefinition.createFile(this);
    } else {
      return null;
    }
  }

  private ParserDefinition getDefinition(Language lang) {
    return LanguageParserDefinitions.INSTANCE.forLanguage(lang);
  }
}
