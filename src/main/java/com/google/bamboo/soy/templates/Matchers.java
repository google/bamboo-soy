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

import com.google.bamboo.soy.elements.CallStatementElement;
import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;

class Matchers {

  static final PsiElementPattern.Capture<PsiElement> templateBlockMatcher =
      psiElement(SoyTypes.TEMPLATE_BLOCK);

  static final PsiElementPattern.Capture<PsiElement> templateCallStatementMatcher =
      psiElement().inside(CallStatementElement.class);
}
