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

package com.google.bamboo.soy.elements;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public interface TagBase extends PsiElement {

  @NotNull
  default TagName getTagName() {
    try {
      // The first child is the opening tag, it's first child is an LBRACE,
      // the next non-whitespace token is the name.
      return TagName
          .valueOf(PsiTreeUtil.skipSiblingsForward(getFirstChild().getFirstChild(),
              PsiWhiteSpace.class).getText().toUpperCase());
    } catch (NullPointerException | IllegalArgumentException e) {
      return TagName._UNKNOWN_;
    }
  }

  enum TagName {
    _UNKNOWN_, CALL, DELCALL, TEMPLATE, DELTEMPLATE, FOR, FOREACH, IF, LET, MSG, PARAM, PLURAL, SELECT, SWITCH
  }
}
