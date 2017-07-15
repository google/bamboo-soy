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

package com.google.bamboo.soy.completion;

import com.google.bamboo.soy.file.SoyFileType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.intellij.codeInsight.completion.CompletionType;
import java.util.List;
import java.util.Set;

public class SoyCompletionTest extends SoyCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/completion";
  }

  protected void doTest(String inputText, Set<String> expectedCompletions) throws Throwable {
    myFixture.configureByFiles("CompletionSource.soy", "ExtraCompletionSource.soy");
    myFixture.configureByText(SoyFileType.INSTANCE, inputText);
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> actualCompletions = myFixture.getLookupElementStrings();
    assertSameElements(
        actualCompletions == null ? ImmutableList.of() : actualCompletions, expectedCompletions);
  }

  protected void doTest(String inputText, String expectedText) throws Throwable {
    myFixture.configureByFiles("CompletionSource.soy", "ExtraCompletionSource.soy");
    myFixture.configureByText(SoyFileType.INSTANCE, inputText);
    myFixture.complete(CompletionType.BASIC, 1);
    myFixture.checkResult(expectedText);
  }

  public void testNamespaceLookup() throws Throwable {
    doTest("{alias o<caret>", "{alias outer");
    doTest("{alias outer.<caret>", ImmutableSet.of("outer.space", "outer.spaceship"));
  }

  public void testTemplateLookup() throws Throwable {
    doTest("{template}{call o<caret>", "{template}{call outer");
    doTest("{template}{call outer.<caret>", ImmutableSet.of("outer.space", "outer.spaceship"));
    doTest(
        "{template}{call outer.space.m<caret>",
        ImmutableSet.of("outer.space.mars", "outer.space.moon"));
  }

  public void testLookupWithAlias() throws Throwable {
    doTest(
        "{alias outer.space}{template}{call s<caret>", "{alias outer.space}{template}{call space");
    doTest(
        "{alias outer.space}{template}{call space.e<caret>",
        "{alias outer.space}{template}{call space.earth");
  }

  public void testLookupWithPartialAlias() throws Throwable {
    doTest(
        "{alias outer as inner}{template}{call i<caret>",
        "{alias outer as inner}{template}{call inner");
    doTest(
        "{alias outer as inner}{template}{call inner.<caret>",
        ImmutableSet.of("inner.space", "inner.spaceship"));
  }

  public void testLookupWithImproperPartialAlias() throws Throwable {
    // Ensuring outer.spaceship exists
    doTest(
        "{alias outer.spaceship}{template}{call spaceship.e<caret>",
        "{alias outer.spaceship}{template}{call spaceship.enterprise");
    //Ensuring it is not matched by outer.space alias
    doTest(
        "{alias outer.space}{template}{call spaceship.e<caret>",
        "{alias outer.space}{template}{call spaceship.e");
  }

  public void testVariablesInScope() throws Throwable {
    doTest(
        "{template .foo}"
            + "{@param dimension: number}"
            + "{@inject force: number}"
            + "{let $multiplier: 10}"
            + "{foreach $ignored in $dimension}{/foreach}"
            + "{for $loop in range(1, 2)}"
            + "  {<caret>",
        ImmutableSet.of("$dimension", "$force", "$multiplier", "$loop"));
  }
}
