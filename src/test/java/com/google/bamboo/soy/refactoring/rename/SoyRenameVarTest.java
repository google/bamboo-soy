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

package com.google.bamboo.soy.refactoring.rename;

public class SoyRenameVarTest extends SoyRenameTest {

  public void testTemplateLetSingleRenameDeclaration() {
    doTest("bar");
  }

  public void testTemplateLetSingleRenameReference() {
    doTest("bar");
  }

  public void testTemplateLetCompoundRenameDeclaration() {
    doTest("bar");
  }

  public void testTemplateLetCompoundRenameReference() {
    doTest("bar");
  }

  public void testForRenameDeclaration() {
    doTest("bar");
  }

  public void testForRenameReference() {
    doTest("bar");
  }

  public void testForRenameIndex() {
    doTest("bar");
  }

  public void testForRenameIndexReference() {
    doTest("bar");
  }

  public void testForeachRenameDeclaration() {
    doTest("bar");
  }

  public void testForeachRenameReference() {
    doTest("bar");
  }

  public void testListComprehensionRenameDeclaration() {
    doTest("bar");
  }

  public void testListComprehensionRenameReference() {
    doTest("bar");
  }
}
