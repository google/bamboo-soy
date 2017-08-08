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

package com.google.bamboo.soy.typing;

import com.google.bamboo.soy.file.SoyFileType;
import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.intellij.openapi.fileTypes.LanguageFileType;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SoyTypingTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/typing";
  }

  protected void doTest(char charToType) throws Throwable {
    myFixture.configureByFiles(getTestName(false) + ".soy");
    myFixture.type(charToType);
    myFixture.checkResultByFile(getTestName(false) + "_after.soy");
  }

  private void doTypingTest(
      final char charToType, @NotNull final String textBefore, @NotNull final String textAfter) {
    doTypingTest(SoyFileType.INSTANCE, charToType, textBefore, textAfter);
  }

  private void doTypingTest(
      @NotNull final LanguageFileType fileType,
      final char charToType,
      @NotNull final String textBefore,
      @NotNull final String textAfter) {
    myFixture.configureByText(fileType, textBefore);
    myFixture.type(charToType);
    myFixture.checkResult(textAfter);
  }

  public void testAfterCloseTag() throws Throwable {
    doTest('\n');
  }

  public void testInDocComment() throws Throwable {
    doTest('\n');
  }

  public void testBeforeDocComment() throws Throwable {
    doTest('\n');
  }

  public void testEndDocComment() throws Throwable {
    doTest('\n');
  }

  public void testTemplateBeginTag() throws Throwable {
    doTest('\n');
  }

  public void testBetweenOpenCloseTags() throws Throwable {
    doTest('\n');
  }

  public void testCloseCharacterInsertion() throws Throwable {
    // Auto-closing
    doTypingTest('\'', "<caret>", "'<caret>'");
    doTypingTest('\'', "=<caret>", "='<caret>'");
    doTypingTest('"', "<caret>", "\"<caret>\"");
    doTypingTest('"', "=<caret>", "=\"<caret>\"");
    doTypingTest('[', "<caret>", "[<caret>]");
    doTypingTest('[', "foo<caret>", "foo[<caret>]");
    doTypingTest('(', "<caret>", "(<caret>)");

    // Auto-closing exceptions
    doTypingTest('\'', "foo<caret>", "foo'<caret>");

    // Character skipping
    doTypingTest(']', "[]<caret>", "[]]<caret>");
    doTypingTest(']', "[<caret>]", "[]<caret>");
    doTypingTest(']', "[[]<caret>]", "[[]]<caret>");
    doTypingTest(']', "[[]][<caret>]]", "[[]][]<caret>]");
    doTypingTest(']', "]]][<caret>]", "]]][]<caret>");
  }

  public void testBeginTags_noWhiteSpace() throws Throwable {
    List<String> topLevelTags = Arrays.asList("template", "deltemplate");
    for (String tag : topLevelTags) {
      doTypingTest(
          '\n',
          "{" + tag + " .foo}<caret>{/" + tag + "}",
          "{" + tag + " .foo}\n    <caret>\n{/" + tag + "}");
    }

    List<String> nestedTags = Arrays.asList("call", "delcall", "switch", "if", "let");
    for (String tag : nestedTags) {
      doTypingTest(
          '\n',
          "{template .bar}{" + tag + " .foo}<caret>{/" + tag + "}{/template}",
          "{template .bar}{" + tag + " .foo}\n    <caret>\n{/" + tag + "}{/template}");
    }

    doTypingTest(
        '\n',
        "{template .bar}{msg description='Hello'}<caret>{/msg}{/template}",
        "{template .bar}{msg description='Hello'}\n    <caret>\n{/msg}{/template}");
  }

  public void testBetweenTags_noWhiteSpace() throws Throwable {
    List<String> choiceTags = Arrays.asList("select", "switch", "plural");
    for (String tag : choiceTags) {
      doTypingTest(
          '\n',
          "{template .bar}{" + tag + "}{case}<caret>{default}{/template}",
          "{template .bar}{" + tag + "}{case}\n    <caret>\n{default}{/template}");
      doTypingTest(
          '\n',
          "{template .bar}{" + tag + "}{case}<caret>{case}{/template}",
          "{template .bar}{" + tag + "}{case}\n    <caret>\n{case}{/template}");
    }

    List<String> callTags = Arrays.asList("call", "delcall");
    for (String tag : callTags) {
      doTypingTest(
          '\n',
          "{template .bar}{" + tag + "}{param/}<caret>{param/}{/template}",
          "{template .bar}{" + tag + "}{param/}\n    <caret>\n{param/}{/template}");
    }
  }

  public void testClosingTags() throws Throwable {
    List<String> simpleTags =
        Arrays.asList("if", "for", "foreach", "msg", "plural", "select", "switch");
    for (String tag : simpleTags) {
      doTypingTest(
          '/',
          "{template .bar}{" + tag + "}{<caret>{/template}",
          "{template .bar}{" + tag + "}{/" + tag + "}<caret>{/template}");
      doTypingTest(
          '/',
          "{template .bar}\n{" + tag + "}\n{<caret>\n{/template}",
          "{template .bar}\n    {" + tag + "}\n    {/" + tag + "}<caret>\n{/template}");
      doTypingTest(
          '/',
          "{template .bar}{{" + tag + "}} {<caret>} {/template}",
          "{template .bar}{{" + tag + "}} {{/" + tag + "}}<caret> {/template}");
    }

    List<String> choiceTags =
        Arrays.asList("plural", "select", "switch");
    for (String tag : choiceTags) {
      doTypingTest(
          '/',
          "{template .bar}{" + tag + "}{case}{<caret>{/template}",
          "{template .bar}{" + tag + "}{case}{/" + tag + "}{/template}");
      doTypingTest(
          '/',
          "{template .bar}{" + tag + "}{default}{<caret>{/template}",
          "{template .bar}{" + tag + "}{default}{/" + tag + "}{/template}");
    }

    // closing compound let
    doTypingTest(
        '/', "{template .bar}{let $var}{<caret>", "{template .bar}{let $var}{/let}<caret>");

    // ignoring single let
    doTypingTest(
        '/',
        "{template .bar}{let $var : 1}{<caret>",
        "{template .bar}{let $var : 1}{/template}<caret>");

    List<String> callTags = Arrays.asList("call", "delcall");
    for (String tag : callTags) {
      // Inlined {call} should be ignored.
      doTypingTest(
          '/',
          "{template .bar}{" + tag + "/} test {<caret>",
          "{template .bar}{" + tag + "/} test {/template}<caret>");
      // Non-inlined {call} should be closed.
      doTypingTest(
          '/',
          "{template .bar}{ " + tag + "} {param} test {/param} {<caret>",
          "{template .bar}{ " + tag + "} {param} test {/param} {/" + tag + "}<caret>");
      // {param/} should be ignored.
      doTypingTest(
          '/',
          "{template .bar}{" + tag + "} {param/} {<caret>",
          "{template .bar}{" + tag + "} {param/} {/" + tag + "}<caret>");
      // {param} should be closed.
      doTypingTest(
          '/',
          "{template .bar}{" + tag + "} {param} test {<caret>",
          "{template .bar}{" + tag + "} {param} test {/param}<caret>");
    }
  }

  public void testNewlineIndent() throws Throwable {
    List<String> topLevelTags =
        Arrays.asList("namespace", "delpackage", "alias");
    for (String tag : topLevelTags) {
      doTypingTest(
          '\n',
          "{" + tag + "}<caret>\n{template}{/template}",
          "{" + tag + "}\n<caret>\n{template}{/template}");
    }

    List<String> simpleTags =
        Arrays.asList("if", "for", "foreach", "msg", "plural", "select", "switch", "call");
    for (String tag : simpleTags) {
      doTypingTest(
          '\n',
          "{template .bar}\n    {" + tag + "}<caret>\n{/template}",
          "{template .bar}\n    {" + tag + "}\n        <caret>\n{/template}");
    }

    List<String> choiceTags = Arrays.asList("select", "switch", "plural");
    for (String tag : choiceTags) {
      doTypingTest(
          '\n',
          "{template .bar}\n    {" + tag + "}\n        {case}<caret>\n{/template}",
          "{template .bar}\n    {" + tag + "}\n        {case}\n            <caret>\n{/template}");
      doTypingTest(
          '\n',
          "{template .bar}\n    {" + tag + "}\n        {default}<caret>\n{/template}",
          "{template .bar}\n    {" + tag + "}\n        {default}\n            <caret>\n{/template}");
    }

    List<String> callTags = Arrays.asList("call", "delcall");
    for (String tag : callTags) {
      doTypingTest(
          '\n',
          "{template .bar}\n    {" + tag + "/}<caret>\n{/template}",
          "{template .bar}\n    {" + tag + "/}\n    <caret>\n{/template}");
      doTypingTest(
          '\n',
          "{template .bar}\n    {" + tag + "}\n        {param}<caret>\n{/template}",
          "{template .bar}\n    {" + tag + "}\n        {param}\n            <caret>\n{/template}");
      doTypingTest(
          '\n',
          "{template .bar}\n    {" + tag + "}\n        {param/}<caret>\n{/template}",
          "{template .bar}\n    {" + tag + "}\n        {param/}\n        <caret>\n{/template}");
    }
  }

  public void testNewlineContinuationIndent() throws Throwable {
    List<String> simpleTags =
        Arrays.asList("if", "for", "foreach", "msg", "plural", "select", "switch", "call");
    for (String tag : simpleTags) {
      doTypingTest(
          '\n',
          "{template .bar}\n    {" + tag + "<caret>\n{/template}",
          "{template .bar}\n    {" + tag + "\n            <caret>\n{/template}");
    }
  }
}
