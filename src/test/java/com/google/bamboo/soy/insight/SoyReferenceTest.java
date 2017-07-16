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

package com.google.bamboo.soy.insight;

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.google.bamboo.soy.elements.CallStatementBase;
import com.google.bamboo.soy.parser.SoyMsgStatement;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyParamListElement;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyVariableReferenceIdentifier;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SoyReferenceTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/insight";
  }

  public void testTemplateReference() throws Throwable {
    myFixture
        .configureByFiles("ReferenceSource.soy", "CompletionSource.soy"
        );
    CallStatementBase element = PsiTreeUtil
        .findChildOfType(myFixture.getFile(), CallStatementBase.class);
    PsiElement id = element.getBeginCall().getTemplateReferenceIdentifier().getReference()
        .resolve();
    assertInstanceOf(id, SoyTemplateDefinitionIdentifier.class);
    assertEquals(".moon", ((SoyTemplateDefinitionIdentifier) id).getName());
  }

  public void testParamReference() throws Throwable {
    myFixture.configureByFiles("ReferenceSource.soy", "CompletionSource.soy");
    SoyParamListElement element = PsiTreeUtil
        .findChildOfType(myFixture.getFile(), SoyParamListElement.class);
    PsiElement id = element.getBeginParamTag().getParamSpecificationIdentifier().getReference()
        .resolve();
    assertInstanceOf(id, SoyParamDefinitionIdentifier.class);
    assertEquals("planet", ((SoyParamDefinitionIdentifier) id).getName());
  }

  public void testVariableReferences() throws Throwable {
    myFixture.configureByFiles("CompletionSource.soy");
    PsiElement container = PsiTreeUtil.findChildOfType(myFixture.getFile(), SoyMsgStatement.class);
    Collection<SoyVariableReferenceIdentifier> vars = PsiTreeUtil
        .findChildrenOfType(container, SoyVariableReferenceIdentifier.class);
    assertSize(3, vars);
    for (SoyVariableReferenceIdentifier var : vars) {
      Class expectedClass;
      if (var.getText().equals("$planet")) {
        // @param
        expectedClass = SoyParamDefinitionIdentifier.class;
      } else if (var.getText().equals("$probe")) {
        // @inject
        expectedClass = SoyParamDefinitionIdentifier.class;
      } else {
        expectedClass = SoyVariableDefinitionIdentifier.class;
      }
      PsiElement id = var.getReference().resolve();
      assertInstanceOf(id, expectedClass);
    }
  }
}
