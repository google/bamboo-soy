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
import com.google.bamboo.soy.elements.CallStatementElement;
import com.google.bamboo.soy.parser.SoyMsgStatement;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyParamListElement;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyVariableReferenceIdentifier;
import com.google.common.collect.Iterables;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import java.util.Collection;
import org.junit.Assert;

public class SoyReferenceTest extends SoyCodeInsightFixtureTestCase {

  private static void assertIsAtParam(PsiElement target, String name) {
    Assert.assertTrue(target instanceof SoyParamDefinitionIdentifier);
    Assert.assertEquals(name, target.getText());
  }

  private static void assertIsAtState(PsiElement target, String name) {
    Assert.assertTrue(target instanceof SoyParamDefinitionIdentifier);
    Assert.assertEquals(name, target.getText());
  }

  private static void assertIsAtInject(PsiElement target, String name) {
    Assert.assertTrue(target instanceof SoyParamDefinitionIdentifier);
    Assert.assertEquals(name, target.getText());
  }

  private static void assertIsVariableDefinition(PsiElement target, String name) {
    Assert.assertTrue(target instanceof SoyVariableDefinitionIdentifier);
    Assert.assertEquals(name, target.getText());
  }

  @Override
  protected String getBasePath() {
    return "/insight";
  }

  private PsiElement resolve() {
    String filename = getTestName(false) + ".soy";
    PsiReference ref = myFixture.getReferenceAtCaretPosition(filename);
    assertNotNull(ref);
    return ref.resolve();
  }

  public void testAtParamReference() {
    assertIsAtParam(resolve(), "planet");
  }

  public void testAtStateReference() {
    assertIsAtState(resolve(), "planet");
  }

  public void testAtInjectReference() {
    assertIsAtInject(resolve(), "planet");
  }

  public void testLetDefinitionReference() {
    assertIsVariableDefinition(resolve(), "$planet");
  }

  public void testTemplateReference() {
    myFixture.configureByFiles("ReferenceSource.soy", "CompletionSourceTemplate.soy");
    CallStatementElement element =
        PsiTreeUtil.findChildOfType(myFixture.getFile(), CallStatementElement.class);
    PsiElement id =
        element.getBeginCall().getTemplateReferenceIdentifier().getReference().resolve();
    assertInstanceOf(id, SoyTemplateDefinitionIdentifier.class);
    assertEquals(".moon", ((SoyTemplateDefinitionIdentifier) id).getName());
  }

  public void testParamReference() {
    myFixture.configureByFiles("ReferenceSource.soy", "CompletionSourceTemplate.soy");
    SoyParamListElement element =
        PsiTreeUtil.findChildOfType(myFixture.getFile(), SoyParamListElement.class);
    PsiElement id =
        element.getBeginParamTag().getParamSpecificationIdentifier().getReference().resolve();
    assertInstanceOf(id, SoyParamDefinitionIdentifier.class);
    assertEquals("planet", ((SoyParamDefinitionIdentifier) id).getName());
  }

  public void testVariableReferencesInTemplate() {
    myFixture.configureByFiles("CompletionSourceTemplate.soy");
    PsiElement container = PsiTreeUtil.findChildOfType(myFixture.getFile(), SoyMsgStatement.class);
    Collection<SoyVariableReferenceIdentifier> vars =
        PsiTreeUtil
            .findChildrenOfType(container, SoyVariableReferenceIdentifier.class);
    assertSize(3, vars);
    for (SoyVariableReferenceIdentifier var : vars) {
      switch (var.getText()) {
        case "$planet": // @param
          assertInstanceOf(var.getReference().resolve(), SoyParamDefinitionIdentifier.class);
          break;
        case "$probe": // @inject
          assertInstanceOf(var.getReference().resolve(), SoyParamDefinitionIdentifier.class);
          break;
        case "$shape": // {let}
          assertInstanceOf(var.getReference().resolve(), SoyVariableDefinitionIdentifier.class);
          break;
        case "$isLoaded": // @state
          Assert.fail("isLoaded is a @state property, should not be valid inside {template}");
          break;
      }
    }
  }

  public void testVariableReferencesInElement() {
    myFixture.configureByFiles("CompletionSourceElement.soy");
    PsiElement container = PsiTreeUtil.findChildOfType(myFixture.getFile(), SoyMsgStatement.class);
    Collection<SoyVariableReferenceIdentifier> vars =
        PsiTreeUtil
            .findChildrenOfType(container, SoyVariableReferenceIdentifier.class);
    assertSize(4, vars);
    for (SoyVariableReferenceIdentifier var : vars) {
      switch (var.getText()) {
        case "$planet": // @param
          assertInstanceOf(var.getReference().resolve(), SoyParamDefinitionIdentifier.class);
          break;
        case "$probe": // @inject
          assertInstanceOf(var.getReference().resolve(), SoyParamDefinitionIdentifier.class);
          break;
        case "$isLoaded": // @state
          assertInstanceOf(var.getReference().resolve(), SoyParamDefinitionIdentifier.class);
          break;
        case "$shape": // {let}
          assertInstanceOf(var.getReference().resolve(), SoyVariableDefinitionIdentifier.class);
          break;
      }
    }
  }

  public void testFindVariableUsagesInTemplate() {
    myFixture.configureByFile("CompletionSourceTemplate.soy");
    Collection<SoyParamDefinitionIdentifier> params =
        PsiTreeUtil.findChildrenOfType(myFixture.getFile(), SoyParamDefinitionIdentifier.class);
    assertSize(2, params); // 1 @param and 1 @inject
    for (SoyParamDefinitionIdentifier param : params) {
      Collection<UsageInfo> usages = myFixture.findUsages(param);
      assertSize(1, usages);
      assertInstanceOf(
          Iterables.getOnlyElement(usages).getElement(), SoyVariableReferenceIdentifier.class);
    }

    // {let}
    SoyVariableDefinitionIdentifier var =
        PsiTreeUtil.findChildOfType(myFixture.getFile(), SoyVariableDefinitionIdentifier.class);
    Collection<UsageInfo> usages = myFixture.findUsages(var);
    assertSize(1, usages);
    assertInstanceOf(
        Iterables.getOnlyElement(usages).getElement(), SoyVariableReferenceIdentifier.class);
  }

  public void testFindVariableUsagesInElement() {
    myFixture.configureByFile("CompletionSourceElement.soy");
    Collection<SoyParamDefinitionIdentifier> params =
        PsiTreeUtil.findChildrenOfType(myFixture.getFile(), SoyParamDefinitionIdentifier.class);
    assertSize(3, params); // 1 @param, 1 @state and 1 @inject
    for (SoyParamDefinitionIdentifier param : params) {
      Collection<UsageInfo> usages = myFixture.findUsages(param);
      assertSize(1, usages);
      assertInstanceOf(
          Iterables.getOnlyElement(usages).getElement(), SoyVariableReferenceIdentifier.class);
    }

    // {let}
    SoyVariableDefinitionIdentifier var =
        PsiTreeUtil.findChildOfType(myFixture.getFile(), SoyVariableDefinitionIdentifier.class);
    Collection<UsageInfo> usages = myFixture.findUsages(var);
    assertSize(1, usages);
    assertInstanceOf(
        Iterables.getOnlyElement(usages).getElement(), SoyVariableReferenceIdentifier.class);
  }
}
