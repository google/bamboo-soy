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

import com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter;
import com.google.bamboo.soy.parser.SoyAliasIdentifier;
import com.google.bamboo.soy.parser.SoyAttributeNameIdentifier;
import com.google.bamboo.soy.parser.SoyCssStatement;
import com.google.bamboo.soy.parser.SoyFieldIdentifier;
import com.google.bamboo.soy.parser.SoyFunctionIdentifier;
import com.google.bamboo.soy.parser.SoyIdentifier;
import com.google.bamboo.soy.parser.SoyNamespaceIdentifier;
import com.google.bamboo.soy.parser.SoyPackageIdentifier;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyParamSpecificationIdentifier;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyTemplateReferenceIdentifier;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyXidStatement;
import com.google.common.collect.ImmutableList;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;

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

  private static final List<Class> nonCompoundIdentifiers =
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

  private boolean inClassList(PsiElement element, List<Class> classes) {
    for (Class clazz : classes) {
      if (clazz.isInstance(element)) {
        return true;
      }
    }
    return false;
  }

  private void sanitizeWhitespaces(PsiElement element, AnnotationHolder holder) {
    if (element.getText().contains(" ")) {
      holder.createErrorAnnotation(element, "Whitespace is not allowed in identifiers");
    }
  }

  private void sanitizePercentageSign(PsiElement element, AnnotationHolder holder) {
    PsiElement parent = element.getParent();
    if (element.getText().startsWith("%") && !(parent instanceof SoyCssStatement)) {
      holder.createErrorAnnotation(
          element, "Percentage-prefixed identifiers are only allowed in css statements");
    }
  }

  private void sanitizeCompoundStatements(PsiElement element, AnnotationHolder holder) {
    PsiElement parent = element.getParent();
    if (element.getText().contains("-")
        && !(parent instanceof SoyCssStatement || parent instanceof SoyXidStatement)) {
      holder.createErrorAnnotation(
          element, "Identifiers with dashes are only allowed in css and xid statements");
    }
  }

  private void sanitizeDot(PsiElement element, AnnotationHolder holder) {
    PsiElement parent = element.getParent();
    String text = element.getText();

    // Handling identifiers that must start with a dot.
    if (parent instanceof SoyTemplateDefinitionIdentifier && !text.startsWith(".")) {
      holder.createErrorAnnotation(element, "Template names must begin with a dot");
    }

    // Handling identifiers that cannot contain a dot.
    if (text.contains(".")) {
      if (parent instanceof SoyParamDefinitionIdentifier) {
        holder.createErrorAnnotation(element, "Parameter definitions cannot contain dots");
      }
      if (parent instanceof SoyParamSpecificationIdentifier) {
        holder.createErrorAnnotation(element, "Parameter specifications cannot contain dots");
      }
    }
  }

  private void sanitizeDollarSign(PsiElement element, AnnotationHolder holder) {
    if (element.getText().startsWith("$")) {
      PsiElement parent = element.getParent();
      // It is worth checking here first if the element is of a type for which dollars are expected
      // since the list of those element types is much smaller than of those for which dollars are
      // forbidden.
      if (!inClassList(parent, identifiersWithDollar)
          && inClassList(parent, identifiersWithNoDollar)) {
        holder.createErrorAnnotation(element, "Expected identifier without $ here.");
      }
    } else {
      if (inClassList(element.getParent(), identifiersWithDollar)) {
        holder.createErrorAnnotation(element, "Expected identifier with $ here.");
      }
    }
  }

  private void highlightIdentifier(PsiElement element, AnnotationHolder holder) {
    PsiElement parent = element.getParent();
    if (element.getText().startsWith("$") || parent instanceof SoyParamDefinitionIdentifier) {
      PsiElement dollarSign = element.getFirstChild();
      holder
          .createInfoAnnotation(dollarSign, "")
          .setTextAttributes(SoySyntaxHighlighter.VARIABLE_REFERENCE);
      PsiElement identifierWord = dollarSign.getNextSibling();
      if (identifierWord != null) {
        holder
            .createInfoAnnotation(identifierWord, "")
            .setTextAttributes(SoySyntaxHighlighter.VARIABLE_REFERENCE);
      }
    }
  }

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof SoyIdentifier) {
      highlightIdentifier(element, holder);
      sanitizeWhitespaces(element, holder);
      sanitizePercentageSign(element, holder);
      sanitizeCompoundStatements(element, holder);
      sanitizeDollarSign(element, holder);
      sanitizeDot(element, holder);
    }
  }
}
