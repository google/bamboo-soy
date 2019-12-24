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

import com.google.bamboo.soy.elements.DefaultInitializerAware;
import com.google.bamboo.soy.lang.ParamUtils;
import com.google.bamboo.soy.lang.StateVariable;
import com.google.bamboo.soy.lang.Variable;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyExpr;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.bamboo.soy.parser.SoyVariableReferenceIdentifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
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

    DefaultInitializerAware atParamOrState = PsiTreeUtil
        .getParentOfType(variableRef, DefaultInitializerAware.class);
    if (atParamOrState == null) {
      return;
    }
    SoyExpr atDefaultInitializer = atParamOrState.getDefaultInitializerExpr();
    if (atDefaultInitializer == null) {
      return;
    }

    if (PsiTreeUtil.findFirstParent(
        element, el -> el == atDefaultInitializer) == null) {
      // element is not a child of a SoyAt[State|Param]Single's default initializer Expr.
      return;
    }

    if (atParamOrState instanceof SoyAtParamSingle) {
      annotationHolder.createErrorAnnotation(element,
          "Default initializers cannot depend on other parameters or state");
      return;
    }

    Collection<StateVariable> declaredStates = ParamUtils.getStateDefinitions(element);

    if (variableRef.getIdentifierWord() == null) {
      return;
    }

    String variableName = variableRef.getIdentifierWord().getText();
    for (StateVariable stateVariable : declaredStates) {
      if (variableName.equals(stateVariable.name)) {
        annotationHolder.createErrorAnnotation(element,
            "State cannot be referenced in default initializers");
        return;
      }
    }
  }
}
