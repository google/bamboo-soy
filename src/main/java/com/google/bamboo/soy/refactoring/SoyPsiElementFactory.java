// Copyright 2020 Google Inc.
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

package com.google.bamboo.soy.refactoring;

import com.google.bamboo.soy.SoyLanguage;
import com.google.bamboo.soy.file.SoyFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;

public class SoyPsiElementFactory {

  private static final String IDENTIFIER_PREFIX = "{template f}{$";
  private static final String IDENTIFIER_SUFFIX = "}{/template}";

  public static PsiElement createIdentifierFromText(
      Project project, String name) throws IncorrectOperationException {
    SoyFile targetFile = createFile(project, IDENTIFIER_PREFIX + name + IDENTIFIER_SUFFIX);
    return targetFile.findElementAt(IDENTIFIER_PREFIX.length());
  }

  private static SoyFile createFile(Project project, String text) {
    return (SoyFile) PsiFileFactory.getInstance(project)
        .createFileFromText("targetFile", SoyLanguage.INSTANCE,
            text);
  }

  private SoyPsiElementFactory() {
    // Non-instantiable.
  }
}
