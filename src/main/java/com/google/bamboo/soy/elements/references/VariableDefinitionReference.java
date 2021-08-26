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
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.MultiRangeReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class VariableDefinitionReference extends PsiReferenceBase<PsiElement>
    implements PsiReference, MultiRangeReference {
  private static final ImmutableSet<IElementType> STRING_LITERAL_TYPES = ImmutableSet.of(
      SoyTypes.ANY_STRING_LITERAL,
      SoyTypes.STRING_LITERAL,
      SoyTypes.MULTI_LINE_STRING_LITERAL);
  private final String identifier;
  private final TextRange textRangeInElement;

  public VariableDefinitionReference(
      PsiElement element, String identifier, TextRange textRange, TextRange textRangeInElement) {
    super(element, textRange);
    this.identifier = identifier;
    this.textRangeInElement = textRangeInElement;
  }

  @NotNull
  private ResolveResult[] multiResolve() {
    final Collection<Variable> definitions =
        Scope.getScopeOrEmpty(this.getElement()).getVariables();
    List<ResolveResult> results = new ArrayList<>();
    for (Variable definition : definitions) {
      if (definition.name.equals(this.identifier)) {
        results.add(new PsiElementResolveResult(definition.element));
      }
    }

    return results.toArray(ResolveResult.EMPTY_ARRAY);
  }

  @Override
  public PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve();
    return resolveResults.length >= 1 ? resolveResults[0].getElement() : null;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    ResolveResult[] results = multiResolve();
    for (ResolveResult result : results) {
      if (this.getElement().getManager().areElementsEquivalent(result.getElement(), element)) {
        return true;
      }
    }
    return false;
  }

  @Override
  @NotNull
  public Object[] getVariants() {
    return Scope.getScopeOrEmpty(this.getElement())
        .getVariables()
        .stream()
        .map(v -> v.name)
        .map(VariableDefinitionReference::createLookupElementBuilder)
        .toArray();
  }

  private static LookupElementBuilder createLookupElementBuilder(String name) {
    return LookupElementBuilder.create("$" + name);
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
    return textRangeInElement;
  }

  @NotNull
  @Override
  public List<TextRange> getRanges() {
    IElementType elementType = getElement().getNode().getElementType();

    // Include the leading '$' character.
    int startOffset = STRING_LITERAL_TYPES.contains(elementType)
        ? textRangeInElement.getStartOffset() - 1
        : 0;
    return ImmutableList.of(new TextRange(startOffset, textRangeInElement.getEndOffset()));
  }
}
