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

import com.google.bamboo.soy.parser.SoyCssStatement;
import com.google.bamboo.soy.parser.SoyXidStatement;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;

public class IdentifierSanityAnnotator implements Annotator {

  @Override
  public void annotate(PsiElement element, AnnotationHolder holder) {
    if (element instanceof SoyXidStatement) {
      if (((SoyXidStatement) element).getCssXidIdentifier().getText().startsWith("%")) {
        holder.createErrorAnnotation(element, "Xid identifiers cannot start with '%'.");
      }
    }
    if (element instanceof SoyCssStatement) {
      if (((SoyCssStatement) element).getCssXidIdentifier().getText().contains(".")) {
        holder.createErrorAnnotation(element, "CSS identifiers cannot contain '.'.");
      }
    }
  }
}
