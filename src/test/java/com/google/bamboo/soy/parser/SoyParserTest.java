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

package com.google.bamboo.soy.parser;

import com.google.bamboo.soy.SoyTestUtils;
import com.intellij.testFramework.ParsingTestCase;

/** Unit tests for the closure template parser. */
public class SoyParserTest extends ParsingTestCase {
  @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
  public SoyParserTest() {
    super("parser", "soy", new SoyParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return SoyTestUtils.getTestDataFolder();
  }

  public void testAlias() throws Throwable {
    doTest(true);
  }

  public void testAllBlockTypes() throws Throwable {
    doTest(true);
  }

  public void testAtParamList() throws Throwable {
    doTest(true);
  }

  public void testEmptyFile() throws Throwable {
    doTest(true);
  }

  public void testExpression() throws Throwable {
    doTest(true);
  }

  public void testForStatement() throws Throwable {
    doTest(true);
  }

  public void testIncompleteTree() throws Throwable {
    doTest(true);
  }

  public void testIfStatement() throws Throwable {
    doTest(true);
  }

  public void testSimpleStatements() throws Throwable {
    doTest(true);
  }

  public void testTypeExpression() throws Throwable {
    doTest(true);
  }

  @Override
  protected boolean checkAllPsiRoots() {
    return false;
  }
}
