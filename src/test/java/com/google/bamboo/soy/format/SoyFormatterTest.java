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

package com.google.bamboo.soy.format;

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.containers.ContainerUtil;

public class SoyFormatterTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/format";
  }

  protected void doTest() {
    myFixture.configureByFiles(getTestName(false) + ".soy");
    WriteCommandAction.writeCommandAction(getProject()).compute(() -> {
      CodeStyleManager.getInstance(getProject()).reformatText(myFixture.getFile(),
          ContainerUtil.newArrayList(myFixture.getFile().getTextRange()));
      return null;
    });
    myFixture.checkResultByFile(getTestName(false) + "_after.soy");
  }

  public void testNestedBlocks() {
    doTest();
  }

  public void testSpacing() {
    doTest();
  }

  public void testLiteral() {
    doTest();
  }

  public void testProtobufLiteral() {
    doTest();
  }
}
