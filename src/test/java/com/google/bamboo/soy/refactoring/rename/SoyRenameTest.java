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

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.google.common.collect.ImmutableList;

public abstract class SoyRenameTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/refactoring/rename";
  }

  void doTest(String newName, String... additionalFiles) {
    myFixture.configureByFiles(
        ImmutableList.builder().add(getTestName(false) + ".soy").add((Object[]) additionalFiles)
            .build()
            .toArray(new String[0]));
    myFixture.testRename(getTestName(false) + "_after.soy", newName);
  }
}
