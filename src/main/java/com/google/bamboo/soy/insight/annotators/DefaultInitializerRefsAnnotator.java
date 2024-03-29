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

import com.google.bamboo.soy.elements.AtElementSingle;
import com.google.bamboo.soy.lang.ParamUtils;
import com.google.bamboo.soy.lang.StateVariable;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyExpr;
import com.google.bamboo.soy.parser.SoyVariableReferenceIdentifier;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class DefaultInitializerRefsAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
    if (!(element instanceof SoyVariableReferenceIdentifier)) {
      return;
    }
    SoyVariableReferenceIdentifier variableRef = (SoyVariableReferenceIdentifier) element;

    AtElementSingle parentAtElement = PsiTreeUtil
        .getParentOfType(variableRef, AtElementSingle.class);
    if (parentAtElement == null) {
      return;
    }
    SoyExpr atDefaultInitializer = parentAtElement.getDefaultInitializerExpr();
    if (atDefaultInitializer == null) {
      return;
    }

    if (PsiTreeUtil.findFirstParent(
        element, el -> el == atDefaultInitializer) == null) {
      // element is not a child of a SoyAt[State|Param]Single's default initializer Expr.
      return;
    }

    if (parentAtElement instanceof SoyAtParamSingle) {
      annotationHolder.newAnnotation(HighlightSeverity.ERROR,
          "Default initializers cannot depend on other parameters or state").create();
      return;
    }

    Collection<StateVariable> declaredStates = ParamUtils.getStateDefinitions(element);

    if (variableRef.getIdentifierWord() == null) {
      return;
    }

    String variableName = variableRef.getIdentifierWord().getText();
    for (StateVariable stateVariable : declaredStates) {
      if (variableName.equals(stateVariable.name)) {
        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
            "State cannot be referenced in default initializers").create();
        return;
      }
    }
  }
}
