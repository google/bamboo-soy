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

package com.google.bamboo.soy.refactoring;

import static com.intellij.patterns.StandardPatterns.instanceOf;

import com.google.bamboo.soy.elements.impl.IdentifierMixin;
import com.google.bamboo.soy.elements.impl.ParamIdentifierMixin;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class SoyReferenceContributor extends PsiReferenceContributor {

  private static final PsiReferenceProvider PSI_REFERENCE_PROVIDER = new PsiReferenceProvider() {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
        @NotNull PsiElement element,
        @NotNull ProcessingContext context) {
      return element.getReferences();
    }
  };

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
        PlatformPatterns.or(
            instanceOf(IdentifierMixin.class),
            instanceOf(ParamIdentifierMixin.class)),
        PSI_REFERENCE_PROVIDER);
  }
}
