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

package com.google.bamboo.soy.elements.impl;

import com.google.bamboo.soy.elements.IdentifierElement;
import com.google.bamboo.soy.elements.references.TemplateDefinitionReference;
import com.google.bamboo.soy.elements.references.VariableDefinitionReference;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class IdentifierMixin extends ASTWrapperPsiElement implements IdentifierElement {

  private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\$[a-zA-Z_][a-zA-Z_0-9]*");

  public IdentifierMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiReference getReference() {
    PsiElement element = getNode().getPsi();
    return getIdentifierReference(element, element.getTextRange().getStartOffset(), 0, element.getText());
  }

  private PsiReferenceBase<PsiElement> getIdentifierReference(PsiElement element, int startOffset,
                                                              int startOffsetInElement, String identifier) {
    if (identifier.startsWith("$")) {
      String fullIdentifier = identifier.substring(1);
      String[] fragments = fullIdentifier.split("\\.");

      int dollarFragmentLength = fragments[0].length() + 1;
      return new VariableDefinitionReference(
          element,
          fragments[0],
          new TextRange(
              startOffset,
              startOffset + dollarFragmentLength),
          new TextRange(startOffsetInElement + 1, startOffsetInElement + dollarFragmentLength));
    } else if (identifier.startsWith(".")) {
      return new TemplateDefinitionReference(element, element.getTextRange());
    } else {
      if (identifier.split("\\.").length >= 2) {
        // Fully qualified template identifier.
        return new TemplateDefinitionReference(element, element.getTextRange());
      }
    }
    return null;
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    PsiElement element = getNode().getPsi();
    String maybeEmbeddedExpression = this.getText();
    if (!maybeEmbeddedExpression.startsWith("\"")) {
      PsiReference singleReference = getReference();
      return singleReference == null
          ? PsiReference.EMPTY_ARRAY
          : new PsiReference[]{singleReference};
    }

    Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(maybeEmbeddedExpression);
    List<PsiReference> variableReferenceList = new ArrayList<>();
    while (identifierMatcher.find()) {
      variableReferenceList.add(
          getIdentifierReference(element,
                                 element.getTextRange().getStartOffset() + identifierMatcher.start(),
                                 identifierMatcher.start(),
                                 identifierMatcher.group())
      );
    }
    return variableReferenceList.toArray(PsiReference.EMPTY_ARRAY);
  }
}
