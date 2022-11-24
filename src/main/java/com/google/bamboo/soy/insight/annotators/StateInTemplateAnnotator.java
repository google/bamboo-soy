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

package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.elements.impl.TemplateBlockMixin;
import com.google.bamboo.soy.parser.SoyAtStateSingle;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class StateInTemplateAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
    if (element instanceof SoyAtStateSingle) {
      TemplateBlockMixin templateBlock = PsiTreeUtil
          .getParentOfType(element, TemplateBlockMixin.class);
      if (templateBlock == null || !templateBlock.isElementBlock()) {
        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                "@state is only allowed inside an {element} block.")
            .range(((SoyAtStateSingle) element).getTagNameToken()).create();
      }
    }
  }
}
