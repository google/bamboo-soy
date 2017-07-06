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

import com.google.bamboo.soy.ParamUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
  private ResolveResult[] multiResolve() {
    final Collection<PsiNamedElement> definitions =
        ParamUtils.getIdentifiersInScope(this.getElement());

    List<ResolveResult> results = new ArrayList<>();
    for (PsiNamedElement definition : definitions) {
      if (definition.getName().equals(this.identifier)) {
        results.add(new PsiElementResolveResult(definition));
      }
    }
    return results.toArray(new ResolveResult[results.size()]);
  }

  @Override
  public PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve();
    return resolveResults.length >= 1 ? resolveResults[0].getElement() : null;
  }

  @Override
  @NotNull
  public Object[] getVariants() {
    return ParamUtils.getParamDefinitions(this.getElement())
        .stream()
        .map(PsiElement::getText)
        .map(LookupElementBuilder::create)
        .collect(Collectors.toList())
        .toArray();
  }

  @Override
  public TextRange getRangeInElement() {
    return textRangeInElement;
  }
}
