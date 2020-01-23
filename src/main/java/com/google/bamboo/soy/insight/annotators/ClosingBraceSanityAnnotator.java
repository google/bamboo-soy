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
import com.google.bamboo.soy.lexer.SoyTokenTypes;
import com.google.bamboo.soy.parser.impl.SoyAliasBlockImpl;
import com.google.bamboo.soy.parser.impl.SoyAtParamSingleImpl;
import com.google.bamboo.soy.parser.impl.SoyAtStateSingleImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginChoiceClauseImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginChoiceImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginElseIfImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginForImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginForeachImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginIfImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginLetImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginMsgImpl;
import com.google.bamboo.soy.parser.impl.SoyBeginTemplateImpl;
import com.google.bamboo.soy.parser.impl.SoyDelegatePackageBlockImpl;
import com.google.bamboo.soy.parser.impl.SoyElseTagImpl;
import com.google.bamboo.soy.parser.impl.SoyEndTagImpl;
import com.google.bamboo.soy.parser.impl.SoyFallbackMsgTagImpl;
import com.google.bamboo.soy.parser.impl.SoyLetSingleStatementImpl;
import com.google.bamboo.soy.parser.impl.SoyNamespaceBlockImpl;
import com.google.bamboo.soy.parser.impl.SoyPrintStatementImpl;
import com.google.bamboo.soy.parser.impl.SoySpecialCharacterStatementImpl;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class ClosingBraceSanityAnnotator implements Annotator {

  @VisibleForTesting
  static final ImmutableSet<Class> mustCloseRBraceTags =
      ImmutableSet.<Class>builder()
          .add(SoyAliasBlockImpl.class)
          .add(SoyAtParamSingleImpl.class)
          .add(SoyAtStateSingleImpl.class)
          .add(SoyBeginChoiceClauseImpl.class)
          .add(SoyBeginElseIfImpl.class)
          .add(SoyBeginForImpl.class)
          .add(SoyBeginForeachImpl.class)
          .add(SoyBeginIfImpl.class)
          .add(SoyBeginLetImpl.class)
          .add(SoyBeginMsgImpl.class)
          .add(SoyBeginChoiceImpl.class)
          .add(SoyBeginTemplateImpl.class)
          .add(SoyDelegatePackageBlockImpl.class)
          .add(SoyElseTagImpl.class)
          .add(SoyEndTagImpl.class)
          .add(SoyFallbackMsgTagImpl.class)
          .add(SoySpecialCharacterStatementImpl.class)
          .add(SoyNamespaceBlockImpl.class)
          .add(SoyPrintStatementImpl.class)
          .build();

  private static ImmutableSet<Class> mustCloseSlashRBraceTags =
      ImmutableSet.of(SoyLetSingleStatementImpl.class);

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (psiElement instanceof TagElement) {
      TagElement tagElement = (TagElement) psiElement;

      TokenSet allowedRBraces = SoyTokenTypes.RIGHT_BRACES;
      if (mustCloseRBraceTags.contains(tagElement.getClass())) {
        allowedRBraces = TokenSet.andNot(allowedRBraces, SoyTokenTypes.SLASH_R_BRACES);
      } else if (mustCloseSlashRBraceTags.contains(tagElement.getClass())) {
        allowedRBraces = TokenSet.andSet(allowedRBraces, SoyTokenTypes.SLASH_R_BRACES);
      }

      if (tagElement.isDoubleBraced()) {
        allowedRBraces = TokenSet.andSet(allowedRBraces, SoyTokenTypes.DOUBLE_BRACES);
      } else {
        allowedRBraces = TokenSet.andNot(allowedRBraces, SoyTokenTypes.DOUBLE_BRACES);
      }

      if (!allowedRBraces.contains(tagElement.getClosingBraceType())) {
        annotationHolder.createErrorAnnotation(tagElement, "Must close by " +
            Stream.of(allowedRBraces.getTypes())
                .map(SoyTokenTypes.BRACE_TYPE_TO_STRING::get)
                .sorted()
                .collect(Collectors.joining(" or ")));
      }
    }
  }
}
