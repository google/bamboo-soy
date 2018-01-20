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

package com.google.bamboo.soy.elements.references;

import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.lang.Variable;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class VariableReference extends PsiReferenceBase<PsiElement> implements PsiReference {
  private String identifier;
  private TextRange textRangeInElement;

  public VariableReference(
      PsiElement element, String identifier, TextRange textRange, TextRange textRangeInElement) {
    super(element, textRange);
    this.identifier = identifier;
    this.textRangeInElement = textRangeInElement;
  }

  @NotNull
  private Stream<ResolveResult> multiResolve() {
    return Scope.getScopeOrEmpty(getElement())
        .getVariables()
        .stream()
        .filter(definition -> definition.name.equals(identifier))
        .map(definition -> new PsiElementResolveResult(definition.element));
  }

  @Override
  public PsiElement resolve() {
    return multiResolve().findFirst().map(ResolveResult::getElement).orElse(null);
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return multiResolve().anyMatch(result ->
        getElement().getManager().areElementsEquivalent(result.getElement(), element));
  }

  @Override
  @NotNull
  public Object[] getVariants() {
    return Scope.getScopeOrEmpty(getElement())
        .getVariables()
        .stream()
        .map(v -> LookupElementBuilder.create(v.name))
        .toArray();
  }

  @Override
  public TextRange getRangeInElement() {
    return textRangeInElement;
  }
}
