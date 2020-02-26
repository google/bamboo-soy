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
import com.google.bamboo.soy.file.SoyFileType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.completion.CompletionType;
import java.util.List;
import java.util.Set;

public class SoyCompletionTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/insight";
  }

  protected void doTest(String inputText, Set<String> expectedCompletions) {
    myFixture.configureByFiles("CompletionSourceTemplate.soy", "ExtraCompletionSource.soy");
    myFixture.configureByText(SoyFileType.INSTANCE, inputText);
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> actualCompletions = myFixture.getLookupElementStrings();
    assertSameElements(
        actualCompletions == null ? ImmutableList.of() : actualCompletions, expectedCompletions);
  }

  protected void doTest(String inputText, String expectedText) {
    myFixture.configureByFiles("CompletionSourceTemplate.soy", "ExtraCompletionSource.soy");
    myFixture.configureByText(SoyFileType.INSTANCE, inputText);
    myFixture.complete(CompletionType.BASIC, 1);
    myFixture.checkResult(expectedText);
  }

  public void testNamespaceLookup() {
    doTest("{alias o<caret>", "{alias outer");
    doTest("{alias outer.<caret>", ImmutableSet.of("outer.space", "outer.spaceship"));
  }

  public void testTemplateLookup() {
    doTest("{template}{call o<caret>", "{template}{call outer");
    doTest("{template}{call outer.<caret>", ImmutableSet.of("outer.space", "outer.spaceship"));
    doTest(
        "{template}{call outer.space.m<caret>",
        ImmutableSet.of("outer.space.mars", "outer.space.moon"));
    doTest("{template}{delcall d<caret>", "{template}{delcall delegate");
  }

  public void testLocalTemplateLookup() {
    doTest("{template .local}{/template}{template}{call .<caret>",
        "{template .local}{/template}{template}{call .local");
    doTest("{deltemplate local.template}{/deltemplate}{template}{delcall l<caret>",
        ImmutableSet.of());
    doTest(
        "{template}{call outer.space.m<caret>",
        ImmutableSet.of("outer.space.mars", "outer.space.moon"));
  }

  public void testLookupWithAlias() {
    doTest(
        "{alias outer.space}{template}{call s<caret>", "{alias outer.space}{template}{call space");
    doTest(
        "{alias outer.space}{template}{call space.e<caret>",
        "{alias outer.space}{template}{call space.earth");
  }

  public void testLookupWithPartialAlias() {
    doTest(
        "{alias outer as inner}{template}{call i<caret>",
        "{alias outer as inner}{template}{call inner");
    doTest(
        "{alias outer as inner}{template}{call inner.<caret>",
        ImmutableSet.of("inner.space", "inner.spaceship"));
  }

  public void testLookupWithImproperPartialAlias() {
    // Ensuring outer.spaceship exists
    doTest(
        "{alias outer.spaceship}{template}{call spaceship.e<caret>",
        "{alias outer.spaceship}{template}{call spaceship.enterprise");
    //Ensuring it is not matched by outer.space alias
    doTest(
        "{alias outer.space}{template}{call spaceship.e<caret>",
        "{alias outer.space}{template}{call spaceship.e");
  }

  public void testVariablesWithoutLeadingDollar() {
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

  public void testGlobals() {
    doTest(
        "{template .foo}"
            + "{@param dimension: number}"
            + "{@inject force: number}"
            + "{let $multiplier: 10}"
            + "{foreach $ignored in $dimension}{/foreach}"
            + "{for $loop in range(1, 2)}"
            + "  {t<caret>",
        ImmutableSet.of());
  }

  public void testVariablesInTemplateScope() {
    doTest(
        "{template .foo}"
            + "{@param dimension: number}"
            + "{@inject force: number}"
            + "{let $multiplier: 10}"
            + "{foreach $ignored in $dimension}{/foreach}"
            + "{for $loop in range(1, 2)}"
            + "  {$<caret>",
        ImmutableSet.of("$dimension", "$force", "$multiplier", "$loop"));
  }

  public void testVariablesInElementScope() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{let $multiplier: 10}"
            + "{foreach $ignored in $dimension}{/foreach}"
            + "{for $loop in range(1, 2)}"
            + "  {$<caret>",
        ImmutableSet.of("$dimension", "$isLoaded", "$force", "$multiplier", "$loop"));
  }

  public void testVariablesInUntypedAtParamDefaultInitializer() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{@param another := <caret>",
        ImmutableSet.of());
  }

  public void testVariablesInTypedAtParamDefaultInitializer() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{@param another: number = <caret>",
        ImmutableSet.of());
  }

  public void testVariablesInUntypedAtParamDefaultInitializerLeadingDollar() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{@param another := $<caret>",
        ImmutableSet.of());
  }

  public void testVariablesInTypedAtParamDefaultInitializerLeadingDollar() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{@param another: number = $<caret>",
        ImmutableSet.of());
  }

  public void testVariablesInUntypedAtStateDefaultInitializer() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{@state another := <caret>",
        ImmutableSet.of("$force", "$dimension"));
  }

  public void testVariablesInTypedAtStateDefaultInitializer() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{@state another: number = <caret>",
        ImmutableSet.of("$force", "$dimension"));
  }

  public void testVariablesInUntypedAtStateDefaultInitializerLeadingDollar() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{@state another := $<caret>",
        ImmutableSet.of("$force", "$dimension"));
  }

  public void testVariablesInTypedAtStateDefaultInitializerLeadingDollar() {
    doTest(
        "{element .foo}"
            + "{@param dimension: number}"
            + "{@state isLoaded := false}"
            + "{@inject force: number}"
            + "{@state another: number = $<caret>",
        ImmutableSet.of("$force", "$dimension"));
  }

  public void testVariablesInFieldAccessLeft() {
    doTest(
        "{template .foo}"
            + "{@param dimension: number}"
            + "{@param dimensionData: number}"
            + "{$dimension<caret>.}"
            + "{/template}",
        ImmutableSet.of("$dimension", "$dimensionData"));
  }

  public void testVariablesInFieldAccessRight() {
    doTest(
        "{template .foo}"
            + "{@param dimension: number}"
            + "{@param dimensionData: number}"
            + "{$dimension.<caret>}"
            + "{/template}",
        ImmutableSet.of());
  }

  public void testTemplateAttributes() {
    doTest(
        "{template .foo <caret>}",
        ImmutableSet.of("stricthtml=\"true\"", "visibility=\"private\"", "kind"));
  }

  public void testTemplateKindValues() {
    doTest(
        "{template .foo kind=\"<caret>\"}",
        ImmutableSet.of("css", "js", "attributes", "html", "text", "uri"));
  }
}
