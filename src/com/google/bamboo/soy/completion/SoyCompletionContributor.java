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
import com.google.bamboo.soy.parser.SoyAliasBlock;
import com.google.bamboo.soy.parser.SoyAtInjectSingle;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyBeginCall;
import com.google.bamboo.soy.parser.SoyBeginDelCall;
import com.google.bamboo.soy.parser.SoyBeginLet;
import com.google.bamboo.soy.parser.SoyBeginParamTag;
import com.google.bamboo.soy.parser.SoyBeginTemplate;
import com.google.bamboo.soy.parser.SoyExpression;
import com.google.bamboo.soy.parser.SoyIdentifier;
import com.google.bamboo.soy.parser.SoyListType;
import com.google.bamboo.soy.parser.SoyMapType;
import com.google.bamboo.soy.parser.SoyParamSpecificationIdentifier;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class SoyCompletionContributor extends CompletionContributor {

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

  SoyCompletionContributor() {
    extendWithVisibilityKeyword();
    extendWithKindKeyword();
    extendWithVariableNamesInScope();
    extendWithTemplateCallIdentifiers();
    extendWithIdentifierFragmentsForAlias();
    extendWithParameterNames();
    extendWithParameterTypes();
  }

  /** Complete the "visibility" keyword in template definition open tags. */
  private void extendWithVisibilityKeyword() {
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
  }

  /**
   * Complete the "kind" keyword in begin parameter tags and complete the supported kind literals in
   * the string literal.
   */
  // TODO(thso): Add support for same completion in let statements.
  private void extendWithKindKeyword() {
    // Complete "kind" keyword after the identifier for let statements and parameters in template
    // function calls.
    extend(
        CompletionType.BASIC,
        psiElement()
            .andOr(
                psiElement().inside(SoyBeginParamTag.class),
                psiElement().inside(SoyBeginLet.class)),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            PsiElement prevSibling = completionParameters.getPosition().getPrevSibling();
            while (prevSibling != null) {
              if (!(prevSibling instanceof PsiWhiteSpace)) {
                if (prevSibling instanceof SoyParamSpecificationIdentifier
                    || prevSibling instanceof SoyVariableDefinitionIdentifier) {
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

    // Complete supported kind literals for names for let statements and parameters in template
    // function calls.
    extend(
        CompletionType.BASIC,
        psiElement()
            .andOr(
                psiElement().inside(SoyBeginParamTag.class).afterLeaf("="),
                psiElement().inside(SoyBeginLet.class).afterLeaf("=")),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            completionResultSet.addAllElements(kindLiterals);
          }
        });
  }

  /** Complete variable names that are in scope when in an expression. */
  private void extendWithVariableNamesInScope() {
    extend(
        CompletionType.BASIC,
        psiElement().inside(SoyExpression.class),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            Collection<ParamUtils.Variable> params =
                ParamUtils.getIdentifiersInScope(completionParameters.getPosition());
            completionResultSet.addAllElements(
                params
                    .stream()
                    .map(param -> "$" + param.name)
                    .map(LookupElementBuilder::create)
                    .collect(Collectors.toList()));
          }
        });
  }

  /**
   * Complete local template identifiers and global fully qualified template name fragments at
   * template call site.
   */
  private void extendWithTemplateCallIdentifiers() {
    // Complete local template identifiers (only for {call})
    extend(
        CompletionType.BASIC,
        psiElement().inside(SoyBeginCall.class),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            completionResultSet.addAllElements(
                TemplateNameUtils.findLocalTemplateNames(completionParameters.getPosition())
                    .stream()
                    .map(LookupElementBuilder::create)
                    .collect(Collectors.toList()));
          }
        });

    // Complete fully qualified template identifiers fragments for {call} and {delcall}.
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

            boolean isDelegate =
                PsiTreeUtil.getParentOfType(
                        identifierElement, SoyBeginCall.class, SoyBeginDelCall.class)
                    instanceof SoyBeginDelCall;

            String prefix = identifier.replaceFirst("IntellijIdeaRulezzz", "");
            Collection<TemplateNameUtils.Fragment> completions =
                TemplateNameUtils.getPossibleNextIdentifierFragments(
                    completionParameters.getPosition().getProject(),
                    identifierElement,
                    prefix,
                    isDelegate);

            completionResultSet.addAllElements(
                completions
                    .stream()
                    .map(
                        (fragment) ->
                            LookupElementBuilder.create(fragment.text)
                                .withTypeText(
                                    fragment.isFinalFragment
                                        ? (isDelegate ? "Delegate template" : "Template")
                                        : "Partial namespace"))
                    .collect(Collectors.toList()));
          }
        });
  }

  /** Complete fully qualified namespace fragments for alias declaration. */
  private void extendWithIdentifierFragmentsForAlias() {
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
            Collection<TemplateNameUtils.Fragment> completions =
                TemplateNameUtils.getTemplateNamespaceFragments(
                    completionParameters.getPosition().getProject(), prefix);

            completionResultSet.addAllElements(
                completions
                    .stream()
                    .map(
                        (fragment) ->
                            LookupElementBuilder.create(fragment.text)
                                .withTypeText(
                                    fragment.isFinalFragment ? "Namespace" : "Partial namespace"))
                    .collect(Collectors.toList()));
          }
        });
  }

  /** Complete parameter names for {param .. /} in template function calls. */
  private void extendWithParameterNames() {
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

            Collection<String> givenParameters = ParamUtils.getGivenParameters(callStatement);
            List<ParamUtils.Variable> parameters =
                ParamUtils.getParametersForInvocation(position, identifier.getText())
                    .stream()
                    .filter(v -> !givenParameters.contains(v.name))
                    .collect(Collectors.toList());

            completionResultSet.addAllElements(
                parameters
                    .stream()
                    .map(
                        (variable) ->
                            LookupElementBuilder.create(variable.name).withTypeText(variable.type))
                    .collect(Collectors.toList()));
          }
        });
  }

  /** Complete types in {@param ...} . */
  private void extendWithParameterTypes() {
    // Complete types in @param.
    extend(
        CompletionType.BASIC,
        psiElement()
            .andOr(
                psiElement().inside(SoyAtParamSingle.class).afterLeaf(":"),
                psiElement().inside(SoyAtInjectSingle.class).afterLeaf(":"),

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
}
