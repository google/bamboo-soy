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

import com.google.bamboo.soy.parser.SoyDelCallStatement;
import com.google.bamboo.soy.parser.SoyDelegateTemplateBlock;
import com.google.bamboo.soy.parser.SoyDirectCallStatement;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;

import static com.intellij.patterns.PlatformPatterns.psiElement;

class Matchers {
  static PsiElementPattern.Capture<PsiElement> templateBlockMatcher =
      psiElement()
          .andOr(psiElement(SoyTemplateBlock.class), psiElement(SoyDelegateTemplateBlock.class));

  static PsiElementPattern.Capture<PsiElement> templateCallStatementMatcher =
      psiElement()
          .andOr(psiElement(SoyDirectCallStatement.class), psiElement(SoyDelCallStatement.class));
}
