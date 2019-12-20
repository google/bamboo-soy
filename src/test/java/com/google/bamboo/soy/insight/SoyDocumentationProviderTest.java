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
import com.google.bamboo.soy.insight.documentation.SoyDocumentationProvider;
import com.intellij.psi.PsiElement;

public class SoyDocumentationProviderTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/insight";
  }

  private void doTest(String expectedQuickNavigateInfo) {
    String filename = getTestName(false) + ".soy";
    myFixture.configureByFile(filename);
    PsiElement element = myFixture.getElementAtCaret();
    String info = (new SoyDocumentationProvider()).getQuickNavigateInfo(element, null);
    assertEquals(expectedQuickNavigateInfo, info);
  }

  public void testAtInjectReference() {
    doTest("Defined at AtInjectReference.soy:5\n"
        + "Leading doc");
  }

  public void testAtParamReference() {
    doTest("Defined at AtParamReference.soy:5\n"
        + "Leading doc");
  }

  public void testAtStateReference() {
    doTest("Defined at AtStateReference.soy:5\n"
        + "Leading doc");
  }

  public void testLetDefinitionReference() {
    doTest("Defined at LetDefinitionReference.soy:5\n"
        + "Leading doc");
  }

  public void testAtInjectReferenceTrailingDoc() {
    doTest("Defined at AtInjectReferenceTrailingDoc.soy:4\n"
        + "Same-line doc");
  }

  public void testAtParamReferenceTrailingDoc() {
    doTest("Defined at AtParamReferenceTrailingDoc.soy:4\n"
        + "Same-line doc");
  }

  public void testAtStateReferenceTrailingDoc() {
    doTest("Defined at AtStateReferenceTrailingDoc.soy:4\n"
        + "Same-line doc");
  }

  public void testLetDefinitionReferenceTrailingDoc() {
    doTest("Defined at LetDefinitionReferenceTrailingDoc.soy:4\n"
        + "Same-line doc");
  }
}
