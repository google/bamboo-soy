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

package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class UnterminatedCommentAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
    if (element instanceof PsiComment) {
      IElementType commentTokenType = ((PsiComment) element).getTokenType();
      if (commentTokenType != SoyTypes.DOC_COMMENT_BLOCK
          && commentTokenType != SoyTypes.COMMENT_BLOCK) {
        return;
      }
      if (!element.getText().endsWith("*/")) {
        int start = element.getTextRange().getEndOffset() - 1;
        int end = start + 1;
        annotationHolder
            .newAnnotation(HighlightSeverity.ERROR, "Unterminated comment")
            .range(TextRange.create(start, end)).create();
      }
    }
  }
}
