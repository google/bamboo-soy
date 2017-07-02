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

import com.google.bamboo.soy.TemplateNameUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class TemplateDefinitionReference extends PsiReferenceBase<PsiElement>
    implements PsiPolyVariantReference {
  private String templateName;

  public TemplateDefinitionReference(PsiElement element, TextRange textRange) {
    super(element, textRange);
    this.templateName = element.getText();
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return PsiElementResolveResult.createResults(
        TemplateNameUtils.findTemplateDefinitions(this.getElement(), templateName));
  }

  @Override
  public PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    ResolveResult[] results = multiResolve(false);
    for (ResolveResult result : results) {
      if (this.getElement().getManager().areElementsEquivalent(result.getElement(), element))
        return true;
    }
    return false;
  }

  @Override
  @NotNull
  public Object[] getVariants() {
    return new Object[0];
  }

  @Override
  public TextRange getRangeInElement() {
    return new TextRange(0, this.getElement().getNode().getTextLength());
  }
}
