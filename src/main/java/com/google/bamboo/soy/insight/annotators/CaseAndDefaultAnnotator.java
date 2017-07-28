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

import com.google.bamboo.soy.elements.ChoiceStatementBaseElement;
import com.google.bamboo.soy.parser.SoyCaseClause;
import com.google.bamboo.soy.parser.SoyDefaultClause;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class CaseAndDefaultAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (psiElement instanceof ChoiceStatementBaseElement) {
      boolean foundDefault = false;
      for (PsiElement child : psiElement.getChildren()) {
        if (foundDefault) {
          if (child instanceof SoyCaseClause) {
            annotationHolder.createErrorAnnotation(
                child, "{case} clauses are not allowed after {default}.");
          } else if (child instanceof SoyDefaultClause) {
            annotationHolder.createErrorAnnotation(
                child, "There can only be one {default} clause.");
          }
        } else if (child instanceof SoyDefaultClause) {
          foundDefault = true;
        }
      }
    }
  }
}
