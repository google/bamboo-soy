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

package com.google.bamboo.soy.annotators;

import com.google.bamboo.soy.BracedTagUtils;
import com.google.bamboo.soy.parser.*;
import com.google.bamboo.soy.parser.impl.*;
import com.google.common.collect.ImmutableSet;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class ClosingBraceSanityAnnotator implements Annotator {
  private static final String MUST_CLOSE_SLASH_RBRACE = "Must close by /} or /}}";
  private static final String MUST_CLOSE_RBRACE = "Must close by } or }}";
  private static final ImmutableSet<Class> mustCloseRBraceTags =
      ImmutableSet.<Class>builder()
          .add(SoyAliasBlockImpl.class)
          .add(SoyAtParamSingleImpl.class)
          .add(SoyBeginCaseClauseImpl.class)
          .add(SoyBeginDelegateTemplateImpl.class)
          .add(SoyBeginElseIfImpl.class)
          .add(SoyBeginForImpl.class)
          .add(SoyBeginForeachImpl.class)
          .add(SoyBeginIfImpl.class)
          .add(SoyBeginLetImpl.class)
          .add(SoyBeginMsgImpl.class)
          .add(SoyBeginPluralImpl.class)
          .add(SoyBeginSelectStatementImpl.class)
          .add(SoyBeginSwitchImpl.class)
          .add(SoyBeginTemplateImpl.class)
          .add(SoyCssStatementImpl.class)
          .add(SoyDefaultTagImpl.class)
          .add(SoyDelegatePackageBlockImpl.class)
          .add(SoyElseTagImpl.class)
          .add(SoyEndCallTagImpl.class)
          .add(SoyEndDelCallTagImpl.class)
          .add(SoyEndDelTemplateTagImpl.class)
          .add(SoyEndForTagImpl.class)
          .add(SoyEndForeachTagImpl.class)
          .add(SoyEndIfTagImpl.class)
          .add(SoyEndLetTagImpl.class)
          .add(SoyEndMsgTagImpl.class)
          .add(SoyEndParamTagImpl.class)
          .add(SoyEndPluralTagImpl.class)
          .add(SoyEndSelectTagImpl.class)
          .add(SoyEndSwitchTagImpl.class)
          .add(SoyEndTemplateTagImpl.class)
          .add(SoyFallbackMsgClauseImpl.class)
          .add(SoyLetCompoundStatementImpl.class)
          .add(SoyLbStatementImpl.class)
          .add(SoyNamespaceBlockImpl.class)
          .add(SoyNilStatementImpl.class)
          .add(SoyPrintStatementImpl.class)
          .add(SoyRbStatementImpl.class)
          .add(SoySpStatementImpl.class)
          .add(SoyWhitespaceStatementImpl.class)
          .add(SoyXidStatementImpl.class)
          .build();

  private static ImmutableSet<Class> mustCloseSlashRBraceTags =
      ImmutableSet.of(SoyLetSingleStatementImpl.class);

  private static void checkAndAnnotateSingleOrBlockTag(
      Class endTagType, PsiElement element, AnnotationHolder annotationHolder) {
    PsiElement beginTagElement = element.getFirstChild();
    if (endTagType.isInstance(element.getLastChild())) {
      if (BracedTagUtils.isSelfClosed(beginTagElement)) {
        annotationHolder.createErrorAnnotation(beginTagElement, MUST_CLOSE_RBRACE);
      }
    } else if (element.getChildren().length == 1) {
      if (!BracedTagUtils.isSelfClosed(beginTagElement)) {
        annotationHolder.createErrorAnnotation(beginTagElement, MUST_CLOSE_SLASH_RBRACE);
      }
    }
  }

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (psiElement instanceof SoyDirectCallStatement) {
      checkAndAnnotateSingleOrBlockTag(SoyEndCallTag.class, psiElement, annotationHolder);
      return;
    }

    if (psiElement instanceof SoyDelCallStatement) {
      checkAndAnnotateSingleOrBlockTag(SoyEndDelCallTag.class, psiElement, annotationHolder);
      return;
    }

    if (psiElement instanceof SoyParamListElement) {
      checkAndAnnotateSingleOrBlockTag(SoyEndParamTag.class, psiElement, annotationHolder);
      return;
    }

    if (mustCloseRBraceTags.contains(psiElement.getClass())
        && BracedTagUtils.isSelfClosed(psiElement)) {
      annotationHolder.createErrorAnnotation(psiElement, MUST_CLOSE_RBRACE);
    }

    if (mustCloseSlashRBraceTags.contains(psiElement.getClass())
        && !BracedTagUtils.isSelfClosed(psiElement)) {
      annotationHolder.createErrorAnnotation(psiElement, MUST_CLOSE_SLASH_RBRACE);
    }
  }
}
