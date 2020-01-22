// Copyright 2020 Google Inc.
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
package com.google.bamboo.soy.commenting;

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.intellij.codeInsight.generation.actions.CommentByBlockCommentAction;
import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;

public class SoyCommenterTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/commenting";
  }

  public void testSoyBlockCommenting() {
    doBlockTest();
  }

  public void testSoyBlockUncommenting() {
    doBlockTest();
  }

  public void testSoySingleLineCommenting() {
    doLineTest();
  }

  public void testSoySingleLineUncommenting() {
    doLineTest();
  }

  public void testSoyMultiLineCommenting() {
    doLineTest();
  }

  public void testSoyMultiLineUncommenting() {
    doLineTest();
  }

  public void testHtmlBlockCommenting() {
    doBlockTest();
  }

  public void testHtmlBlockUncommenting() {
    doBlockTest();
  }

  public void testHtmlSingleLineCommenting() {
    doLineTest();
  }

  public void testHtmlSingleLineUncommenting() {
    doLineTest();
  }

  public void testHtmlMultiLineCommenting() {
    doLineTest();
  }

  public void testHtmlMultiLineUncommenting() {
    doLineTest();
  }

  private void doLineTest() {
    myFixture.configureByFiles(getTestName(false) + ".soy");
    CommentByLineCommentAction action = new CommentByLineCommentAction();
    action.actionPerformedImpl(getProject(), myFixture.getEditor());
    myFixture.checkResultByFile(getTestName(false) + "_after.soy");
  }

  private void doBlockTest() {
    myFixture.configureByFiles(getTestName(false) + ".soy");
    CommentByBlockCommentAction action = new CommentByBlockCommentAction();
    action.actionPerformedImpl(getProject(), myFixture.getEditor());
    myFixture.checkResultByFile(getTestName(false) + "_after.soy");
  }
}
