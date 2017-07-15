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

package com.google.bamboo.soy.templates;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import com.google.bamboo.soy.file.SoyFileType;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class TemplateBlockContext extends TemplateContextType {
  protected TemplateBlockContext() {
    super("CLOSURE_TEMPLATE_TEMPLATE_BLOCK", "Closure template: template block");
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    if (file.getFileType() != SoyFileType.INSTANCE) return false;

    PsiElement element = file.findElementAt(offset);
    return psiElement()
        .inside(Matchers.templateBlockMatcher)
        .andNot(psiElement().withAncestor(2, Matchers.templateCallStatementMatcher))
        .accepts(element);
  }
}
