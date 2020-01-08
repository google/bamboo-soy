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

package com.google.bamboo.soy.elements.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiNamedElement;
import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

class SoyPsiElementPresentationFactory {

  static ItemPresentation getItemPresentation(PsiNamedElement element) {
    return new ItemPresentation() {

      @Nullable
      @Override
      public String getPresentableText() {
        return element.getName();
      }

      @Nullable
      @Override
      public String getLocationString() {
        return null;
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        return element.getIcon(Iconable.ICON_FLAG_READ_STATUS);
      }
    };
  }

  private SoyPsiElementPresentationFactory() {}
}
