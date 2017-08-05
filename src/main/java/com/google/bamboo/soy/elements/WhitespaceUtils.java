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

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public class WhitespaceUtils {

  @Nullable
  public static PsiElement getFirstMeaningChild(PsiElement element) {
    PsiElement first = element.getFirstChild();
    return first instanceof PsiWhiteSpace || first instanceof PsiComment
        ? getNextMeaningSibling(first)
        : first;
  }

  @Nullable
  public static PsiElement getLastMeaningChild(PsiElement element) {
    PsiElement last = element.getLastChild();
    return last instanceof PsiWhiteSpace || last instanceof PsiComment
        ? getPrevMeaningSibling(last)
        : last;
  }

  @Nullable
  public static PsiElement getNextMeaningSibling(PsiElement element) {
    return PsiTreeUtil.skipSiblingsForward(element, PsiWhiteSpace.class, PsiComment.class);
  }

  @Nullable
  public static PsiElement getPrevMeaningSibling(PsiElement element) {
    return PsiTreeUtil.skipSiblingsBackward(element, PsiWhiteSpace.class, PsiComment.class);
  }
}
