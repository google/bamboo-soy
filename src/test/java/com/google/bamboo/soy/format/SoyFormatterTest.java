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

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.google.bamboo.soy.SoyLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import org.jetbrains.annotations.NotNull;

public abstract class SoyFormatterTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/format";
  }

  protected void doTest() throws Throwable {
    PsiFile baseFile = myFixture.configureByFile(getTestName(false) + ".soy");
    VirtualFile virtualFile = baseFile.getVirtualFile();
    assert virtualFile != null;
    TemplateDataLanguageMappings.getInstance(getProject())
        .setMapping(virtualFile, SoyLanguage.INSTANCE);

    // fetch a fresh instance of the file -- the template data mapping creates a new instance,
    // which was causing problems in PsiFileImpl.isValid()
    final PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
    assert file != null;

    TextRange rangeToUse = file.getTextRange();
    CodeStyleManager styleManager = CodeStyleManager.getInstance(getProject());
    styleManager.reformatText(file, rangeToUse.getStartOffset(), rangeToUse.getEndOffset());
    myFixture.checkResultByFile(getTestName(false) + "_after.soy");
  }


  private void doFormatterTest(
      @NotNull final LanguageFileType fileType,
      @NotNull final String textBefore,
      @NotNull final String textAfter) {
    myFixture.configureByText(fileType, textBefore);

    myFixture.checkResult(textAfter);
  }
}
