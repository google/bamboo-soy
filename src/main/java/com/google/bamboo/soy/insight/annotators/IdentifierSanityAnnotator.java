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

package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.parser.SoyAliasIdentifier;
import com.google.bamboo.soy.parser.SoyAttributeNameIdentifier;
import com.google.bamboo.soy.parser.SoyFieldIdentifier;
import com.google.bamboo.soy.parser.SoyFunctionIdentifier;
import com.google.bamboo.soy.parser.SoyNamespaceIdentifier;
import com.google.bamboo.soy.parser.SoyPackageIdentifier;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyParamSpecificationIdentifier;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyTemplateReferenceIdentifier;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.google.common.collect.ImmutableList;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import java.util.List;

public class IdentifierSanityAnnotator implements Annotator {
  private static final List<Class> identifiersWithNoDollar =
      ImmutableList.copyOf(
          new Class[] {
            SoyTemplateDefinitionIdentifier.class,
            SoyTemplateReferenceIdentifier.class,
            SoyParamDefinitionIdentifier.class,
            SoyParamSpecificationIdentifier.class,
            SoyNamespaceIdentifier.class,
            SoyAliasIdentifier.class,
            SoyPackageIdentifier.class,
            SoyFieldIdentifier.class,
            SoyAttributeNameIdentifier.class,
            SoyFunctionIdentifier.class
          });

  private static final List<Class> identifiersWithDollar =
      ImmutableList.of(SoyVariableDefinitionIdentifier.class);

  boolean inClassList(PsiElement element, List<Class> classes) {
    for (Class clazz : classes) {
      if (clazz.isInstance(element)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void annotate(PsiElement element, AnnotationHolder holder) {
    String text = element.getText();
    if (text.startsWith("$")) {
      // It is worth checking here first if the element is of a type for which dollars are expected
      // since the list of those element types is much smaller than of those for which dollars are
      // forbidden.
      if (!inClassList(element, identifiersWithDollar)
          && inClassList(element, identifiersWithNoDollar)) {
        holder.createErrorAnnotation(element, "Expected identifier without $ here.");
      }
    } else {
      if (inClassList(element, identifiersWithDollar)) {
        holder.createErrorAnnotation(element, "Expected identifier with $ here.");
      }
    }
  }
}
