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

import com.google.bamboo.soy.elements.TagElement;
import com.google.bamboo.soy.parser.impl.SoyAliasBlockImpl;
import com.google.bamboo.soy.parser.impl.SoyAtParamSingleImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginCaseClauseImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginChoiceImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginElseIfImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginForImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginForeachImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginIfImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginLetImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginMsgImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginTemplateImpl;
import com.google.bamboo.soy.parser.impl.SoyCssStatementImpl;
import com.google.bamboo.soy.parser.impl.SoyDefaultTagImpl;
import com.google.bamboo.soy.parser.impl.SoyDelegatePackageBlockImpl;
import com.google.bamboo.soy.parser.impl.SoyElseTagImpl;
import com.google.bamboo.soy.parser.impl.SoyEndCallImpl;
import com.google.bamboo.soy.parser.impl.SoyEndChoiceImpl;
import com.google.bamboo.soy.parser.impl.SoyEndDelTemplateTagImpl;
import com.google.bamboo.soy.parser.impl.SoyEndForTagImpl;
import com.google.bamboo.soy.parser.impl.SoyEndForeachTagImpl;
import com.google.bamboo.soy.parser.impl.SoyEndIfTagImpl;
import com.google.bamboo.soy.parser.impl.SoyEndLetTagImpl;
import com.google.bamboo.soy.parser.impl.SoyEndMsgTagImpl;
import com.google.bamboo.soy.parser.impl.SoyEndTemplateTagImpl;
import com.google.bamboo.soy.parser.impl.SoyFallbackMsgTagImpl;
import com.google.bamboo.soy.parser.impl.SoyLbStatementImpl;
import com.google.bamboo.soy.parser.impl.SoyLetSingleStatementImpl;
import com.google.bamboo.soy.parser.impl.SoyNamespaceBlockImpl;
import com.google.bamboo.soy.parser.impl.SoyNilStatementImpl;
import com.google.bamboo.soy.parser.impl.SoyPrintStatementImpl;
import com.google.bamboo.soy.parser.impl.SoyRbStatementImpl;
import com.google.bamboo.soy.parser.impl.SoySpStatementImpl;
import com.google.bamboo.soy.parser.impl.SoyWhitespaceStatementImpl;
import com.google.bamboo.soy.parser.impl.SoyXidStatementImpl;
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
          .add(SoyBeginElseIfImpl.class)
          .add(SoyBeginForImpl.class)
          .add(SoyBeginForeachImpl.class)
          .add(SoyBeginIfImpl.class)
          .add(SoyBeginLetImpl.class)
          .add(SoyBeginMsgImpl.class)
          .add(SoyBeginChoiceImpl.class)
          .add(SoyBeginTemplateImpl.class)
          .add(SoyCssStatementImpl.class)
          .add(SoyDefaultTagImpl.class)
          .add(SoyDelegatePackageBlockImpl.class)
          .add(SoyElseTagImpl.class)
          .add(SoyEndCallImpl.class)
          .add(SoyEndChoiceImpl.class)
          .add(SoyEndDelTemplateTagImpl.class)
          .add(SoyEndForTagImpl.class)
          .add(SoyEndForeachTagImpl.class)
          .add(SoyEndIfTagImpl.class)
          .add(SoyEndLetTagImpl.class)
          .add(SoyEndMsgTagImpl.class)
          .add(SoyEndTemplateTagImpl.class)
          .add(SoyFallbackMsgTagImpl.class)
          .add(SoyLbStatementImpl.class)
          .add(SoyNamespaceBlockImpl.class)
          .add(SoyNilStatementImpl.class)
          .add(SoyPrintStatementImpl.class)
          .add(SoyRbStatementImpl.class)
          .add(SoySpStatementImpl.class)
          .add(SoyWhitespaceStatementImpl.class)
          .add(SoyXidStatementImpl.class)
          .build();

  static {
    for (Class clazz : mustCloseRBraceTags) {
      assert (TagElement.class.isAssignableFrom(clazz));
    }
  }

  private static ImmutableSet<Class> mustCloseSlashRBraceTags =
      ImmutableSet.of(SoyLetSingleStatementImpl.class);

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (psiElement instanceof TagElement) {
      if (mustCloseRBraceTags.contains(psiElement.getClass())
          && ((TagElement) psiElement).isSelfClosed()) {
        annotationHolder.createErrorAnnotation(psiElement, MUST_CLOSE_RBRACE);
      }

      if (mustCloseSlashRBraceTags.contains(psiElement.getClass())
          && !((TagElement) psiElement).isSelfClosed()) {
        annotationHolder.createErrorAnnotation(psiElement, MUST_CLOSE_SLASH_RBRACE);
      }
    }
  }
}
