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

package com.google.bamboo.soy.completion;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import com.google.bamboo.soy.ParamUtils;
import com.google.bamboo.soy.TemplateNameUtils;
import com.google.bamboo.soy.elements.CallStatementBase;
import com.google.bamboo.soy.elements.TemplateDefinitionElement;
import com.google.bamboo.soy.parser.*;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

public class SoyCompletionContributor extends CompletionContributor {

  SoyCompletionContributor() {
    // Complete variable names that are in scope when in expressions.
    extend(
        CompletionType.BASIC,
        psiElement().inside(SoyExpression.class),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            Collection<PsiNamedElement> params =
                ParamUtils.getIdentifiersInScope(completionParameters.getPosition());
            completionResultSet.addAllElements(
                params
                    .stream()
                    .map(PsiNamedElement::getName)
                    .map(param -> "$" + param)
                    .map(LookupElementBuilder::create)
                    .collect(Collectors.toList()));
          }
        });

    // Complete template names for function calls.
    extend(
        CompletionType.BASIC,
        psiElement()
            .andOr(
                psiElement().inside(SoyBeginCall.class),
                psiElement().inside(SoyBeginDelCall.class)),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            Collection<TemplateDefinitionElement> templates =
                TemplateNameUtils.findLocalTemplateDefinitions(completionParameters.getPosition());
            completionResultSet.addAllElements(
                templates
                    .stream()
                    .map(PsiElement::getText)
                    .filter(identifier -> identifier.startsWith("."))
                    .map(LookupElementBuilder::create)
                    .collect(Collectors.toList()));
          }
        });

    // Complete `visibility="private"` in template definition open tag.
    extend(
        CompletionType.BASIC,
        psiElement().andOr(psiElement().inside(SoyBeginTemplate.class)),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            PsiElement prevSibling = completionParameters.getPosition().getPrevSibling();
            while (prevSibling != null) {
              if (!(prevSibling instanceof PsiWhiteSpace)) {
                if (prevSibling instanceof SoyTemplateDefinitionIdentifier) {
                  completionResultSet.addElement(
                      LookupElementBuilder.create("visibility=\"private\""));
                } else {
                  return;
                }
              }

              prevSibling = prevSibling.getPrevSibling();
            }
          }
        });

    // Complete fully qualified template identifiers fragments for function calls.
    extend(
        CompletionType.BASIC,
        psiElement()
            .andOr(
                psiElement().inside(SoyBeginCall.class),
                psiElement().inside(SoyBeginDelCall.class)),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            PsiElement identifierElement =
                PsiTreeUtil.getParentOfType(
                    completionParameters.getPosition(), SoyIdentifier.class);
            String identifier = identifierElement == null ? "" : identifierElement.getText();

            String prefix = identifier.replaceFirst("IntellijIdeaRulezzz", "");
            Collection<String> completions =
                TemplateNameUtils.getTemplateNameIdentifiersFragments(
                    completionParameters.getPosition().getProject(), identifierElement, prefix);

            completionResultSet.addAllElements(
                completions
                    .stream()
                    .map(LookupElementBuilder::create)
                    .collect(Collectors.toList()));
          }
        });

    // Complete fully qualified namespace fragments for alias declaration.
    extend(
        CompletionType.BASIC,
        psiElement().andOr(psiElement().inside(SoyAliasBlock.class)),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            PsiElement identifierElement =
                PsiTreeUtil.getParentOfType(
                    completionParameters.getPosition(), SoyIdentifier.class);
            String identifier = identifierElement == null ? "" : identifierElement.getText();

            String prefix = identifier.replaceFirst("IntellijIdeaRulezzz", "");
            Collection<String> completions =
                TemplateNameUtils.getTemplateNamespaceFragments(
                    completionParameters.getPosition().getProject(), prefix);

            completionResultSet.addAllElements(
                completions
                    .stream()
                    .map(LookupElementBuilder::create)
                    .collect(Collectors.toList()));
          }
        });

    // Complete parameter names for {param .. /} in template function calls.
    extend(
        CompletionType.BASIC,
        psiElement()
            .inside(SoyBeginParamTag.class)
            .and(
                psiElement()
                    .afterLeafSkipping(
                        psiElement(PsiWhiteSpace.class), psiElement().withText("param"))),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            PsiElement position = completionParameters.getPosition();
            CallStatementBase callStatement =
                (CallStatementBase)
                    PsiTreeUtil.findFirstParent(position, elt -> elt instanceof CallStatementBase);

            if (callStatement == null) {
              return;
            }

            PsiElement identifier = PsiTreeUtil.findChildOfType(callStatement, SoyIdentifier.class);

            if (identifier == null) {
              return;
            }

            PsiElement templateDefinition =
                TemplateNameUtils.findTemplateDefinition(position, identifier.getText());
            Collection<String> parameters =
                ParamUtils.getParamDefinitions(templateDefinition)
                    .stream()
                    .map(PsiElement::getText)
                    .collect(Collectors.toList());
            Collection<String> givenParameters = ParamUtils.getGivenParameters(callStatement);

            parameters.removeAll(givenParameters);

            completionResultSet.addAllElements(
                parameters.stream().map(LookupElementBuilder::create).collect(Collectors.toList()));
          }
        });

    // Complete "kind" keyword after param identifier.
    extend(
        CompletionType.BASIC,
        psiElement().inside(SoyBeginParamTag.class),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            PsiElement prevSibling = completionParameters.getPosition().getPrevSibling();
            while (prevSibling != null) {
              if (!(prevSibling instanceof PsiWhiteSpace)) {
                if (prevSibling instanceof SoyParamSpecificationIdentifier) {
                  completionResultSet.addElement(
                      LookupElementBuilder.create("kind")
                          .withInsertHandler(new PostfixInsertHandler("=\"", "\"")));
                }

                return;
              }

              prevSibling = prevSibling.getPrevSibling();
            }
          }
        });

    // Complete supported kind literal for names for {param .. /} in template function calls.
    extend(
        CompletionType.BASIC,
        psiElement().inside(SoyBeginParamTag.class).afterLeaf("="),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            completionResultSet.addAllElements(kindLiterals);
          }
        });

    // Complete types in @param.
    extend(
        CompletionType.BASIC,
        psiElement()
            .andOr(
                psiElement().inside(SoyAtParamBody.class).afterLeaf(":"),
                psiElement().inside(SoyAtInjectBody.class).afterLeaf(":"),

                // List type literal.
                psiElement().inside(SoyListType.class).afterLeaf("<"),

                // Map type literal.
                psiElement().inside(SoyMapType.class).afterLeaf("<"),
                psiElement().inside(SoyMapType.class).afterLeaf(",")),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            completionResultSet.addAllElements(soyTypeLiterals);
          }
        });
  }

  @Override
  public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
    return (typeChar == '.' || typeChar == '$');
  }

  private static LookupElement soyListTypeLiteral =
      LookupElementBuilder.create("list").withInsertHandler(new PostfixInsertHandler("<", ">"));

  private static LookupElement soyMapTypeLiteral =
      LookupElementBuilder.create("map").withInsertHandler(new PostfixInsertHandler("<", ",>"));

  private static List<LookupElement> soyTypeLiterals =
      Stream.concat(
              Stream.of(
                      "any",
                      "null",
                      "?",
                      "string",
                      "bool",
                      "int",
                      "float",
                      "number",
                      "html",
                      "uri",
                      "js",
                      "css",
                      "attributes")
                  .map(LookupElementBuilder::create),
              Stream.of(soyListTypeLiteral, soyMapTypeLiteral))
          .collect(Collectors.toList());

  private static List<LookupElement> kindLiterals =
      Stream.of("text", "html", "attributes", "uri", "css", "js")
          .map(LookupElementBuilder::create)
          .collect(Collectors.toList());
}
