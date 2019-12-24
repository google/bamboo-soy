// Copyright 2019 Google Inc.
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

import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class DefaultInitializerOnOptionalParameterAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
    if (element instanceof SoyAtParamSingle) {
      SoyAtParamSingle atParamSingle = (SoyAtParamSingle) element;
      if (atParamSingle.getTagNameTokenType() == SoyTypes.AT_PARAM_OPT
          && atParamSingle.getExpr() != null) {
        annotationHolder.createErrorAnnotation(atParamSingle,
            "Default initializers are not supported on optional parameters. Did you mean '{@param "
                + atParamSingle.getName() + " ...}'?");
      }
    }
  }
}
