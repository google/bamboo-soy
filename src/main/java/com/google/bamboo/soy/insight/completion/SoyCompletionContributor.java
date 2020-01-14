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

package com.google.bamboo.soy.insight.completion;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.instanceOf;
import static com.intellij.patterns.StandardPatterns.or;

import com.google.bamboo.soy.elements.AtElementSingle;
import com.google.bamboo.soy.elements.CallStatementElement;
import com.google.bamboo.soy.elements.WhitespaceUtils;
import com.google.bamboo.soy.lang.ParamUtils;
import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.lang.StateVariable;
import com.google.bamboo.soy.lang.TemplateNameUtils;
import com.google.bamboo.soy.lang.Variable;
import com.google.bamboo.soy.parser.SoyAliasBlock;
import com.google.bamboo.soy.parser.SoyAtInjectSingle;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyAtStateSingle;
import com.google.bamboo.soy.parser.SoyBeginCall;
import com.google.bamboo.soy.parser.SoyBeginElseIf;
import com.google.bamboo.soy.parser.SoyBeginFor;
import com.google.bamboo.soy.parser.SoyBeginForeach;
import com.google.bamboo.soy.parser.SoyBeginIf;
import com.google.bamboo.soy.parser.SoyBeginLet;
import com.google.bamboo.soy.parser.SoyBeginParamTag;
import com.google.bamboo.soy.parser.SoyBeginTemplate;
import com.google.bamboo.soy.parser.SoyExpr;
import com.google.bamboo.soy.parser.SoyListType;
import com.google.bamboo.soy.parser.SoyMapType;
import com.google.bamboo.soy.parser.SoyNamespaceIdentifier;
import com.google.bamboo.soy.parser.SoyParamSpecificationIdentifier;
import com.google.bamboo.soy.parser.SoyPrintStatement;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyTemplateReferenceIdentifier;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.google.common.collect.ImmutableList;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class SoyCompletionContributor extends CompletionContributor {

  private static final LookupElement SOY_LIST_TYPE_LITERAL =
      LookupElementBuilder.create("list").withInsertHandler(new PostfixInsertHandler("<", ">"));

  private static final LookupElement SOY_MAP_TYPE_LITERAL =
      LookupElementBuilder.create("map").withInsertHandler(new PostfixInsertHandler("<", ",>"));

  private static final ImmutableList<LookupElement> SOY_TYPE_LITERALS =
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
          Stream.of(SOY_LIST_TYPE_LITERAL, SOY_MAP_TYPE_LITERAL))
          .collect(ImmutableList.toImmutableList());

  private static final ImmutableList<LookupElement> KIND_LITERALS =
      Stream.of("text", "html", "attributes", "uri", "css", "js")
          .map(LookupElementBuilder::create)
          .collect(ImmutableList.toImmutableList());

  SoyCompletionContributor() {
    extendWithTemplateDefinitionLevelKeywords();
    extendWithKindKeyword();
    extendWithVariableNamesInScope();
    extendWithTemplateCallIdentifiers();
    extendWithIdentifierFragmentsForAlias();
    extendWithParameterNames();
    extendWithParameterTypes();
  }

  @Override
  public void fillCompletionVariants(
      @NotNull final CompletionParameters parameters,
      @NotNull final CompletionResultSet resultSet) {
    super.fillCompletionVariants(parameters, resultSet);

    // Discard _semantically_ invalid suggestions accepted by CamelHumpMatcher
    // (e.g. @state in @state/@param default initializer). We provide these ourselves.
    resultSet.runRemainingContributors(
        parameters,
        completionResult -> {
          if (completionResult.getLookupElement() != null) {
            if (completionResult.getLookupElement().getLookupString().startsWith("$")) {
              return;
            }
            resultSet.addElement(completionResult.getLookupElement());
          }
        });
  }

  /**
   * Complete the "visibility" and "stricthtml" keywords in template definition open tags.
   */
  private void extendWithTemplateDefinitionLevelKeywords() {
    extend(
        CompletionType.BASIC,
        psiElement().andOr(psiElement().inside(SoyBeginTemplate.class)),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            if (isPrecededBy(
                completionParameters.getPosition(),
                elt -> elt instanceof SoyTemplateDefinitionIdentifier)) {
              completionResultSet.addElement(LookupElementBuilder.create("visibility=\"private\""));
              completionResultSet.addElement(LookupElementBuilder.create("stricthtml=\"true\""));
            }
          }
        });
  }

  /**
   * Complete the "kind" keyword in begin parameter tags and complete the supported kind literals in
   * the string literal.
   */
  private void extendWithKindKeyword() {
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
            if (isPrecededBy(
                completionParameters.getPosition(),
                elt ->
                    elt instanceof SoyParamSpecificationIdentifier
                        || elt instanceof SoyVariableDefinitionIdentifier)) {
              completionResultSet.addElement(
                  LookupElementBuilder.create("kind")
                      .withInsertHandler(new PostfixInsertHandler("=\"", "\"")));
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
            completionResultSet.addAllElements(KIND_LITERALS);
          }
        });
  }

  /**
   * Complete variable names that are in scope when in an expression.
   */
  private void extendWithVariableNamesInScope() {
    extend(
        CompletionType.BASIC,
        psiElement()
            .andOr(
                psiElement().inside(SoyExpr.class),
                psiElement().inside(SoyBeginIf.class),
                psiElement().inside(SoyBeginElseIf.class),
                psiElement().inside(SoyBeginFor.class),
                psiElement().inside(SoyBeginForeach.class),
                psiElement().inside(SoyPrintStatement.class),
                psiElement()
                    .inside(SoyBeginParamTag.class)
                    .and(
                        psiElement()
                            .afterLeafSkipping(
                                psiElement(PsiWhiteSpace.class), psiElement(SoyTypes.COLON))),
                psiElement()
                    .inside(
                        or(instanceOf(SoyAtParamSingle.class), instanceOf(SoyAtStateSingle.class)))
                    .and(
                        psiElement()
                            .afterLeafSkipping(
                                psiElement(PsiWhiteSpace.class),
                                or(psiElement(SoyTypes.EQUAL), psiElement(SoyTypes.COLON_EQUAL))))),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              @NotNull ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {

            PsiElement currentElement = completionParameters.getPosition();

            boolean isInsideDefaultInitializer = isInsideDefaultInitializer(currentElement);
            if (isInsideDefaultInitializer
                && PsiTreeUtil.getParentOfType(currentElement, SoyAtParamSingle.class) != null) {
              // Default parameters cannot depend on other parameters or state.
              return;
            }

            Collection<Variable> params = Scope.getScopeOrEmpty(currentElement).getVariables();
            completionResultSet.addAllElements(
                params.stream()
                    .filter(
                        variable ->
                            !isInsideDefaultInitializer
                                // State cannot be referenced in default initializers.
                                || !(variable instanceof StateVariable))
                    .map(param -> "$" + param.name)
                    .map(LookupElementBuilder::create)
                    .collect(Collectors.toList()));
          }
        });
  }

  private boolean isInsideDefaultInitializer(PsiElement currentElement) {
    AtElementSingle parentAtElement =
        PsiTreeUtil.getParentOfType(currentElement, AtElementSingle.class);
    if (parentAtElement == null) {
      return false;
    }

    if (parentAtElement.getLastChild() != null
        && PsiTreeUtil.findSiblingBackward(
                parentAtElement.getLastChild(),
                currentElement.getNode().getElementType(),
                false,
                null)
            == currentElement) {
      // currentElement is an immediate child of a SoyAt[State|Param]Single that does not have
      // a valid default initializer Expr (due to malformed source code during typing).
      return true;
    }
    SoyExpr atDefaultInitializer = parentAtElement.getDefaultInitializerExpr();
    if (atDefaultInitializer == null) {
      // This is also the case for @inject.
      return false;
    }

    // currentElement is a child of a SoyAt[State|Param]Single's default initializer Expr.
    return PsiTreeUtil.findFirstParent(currentElement, element -> element == atDefaultInitializer)
        != null;
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
            if (PsiTreeUtil.getParentOfType(
                completionParameters.getPosition(), CallStatementElement.class)
                .isDelegate()) {
              return;
            }

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
        psiElement().inside(SoyBeginCall.class),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            PsiElement identifierElement =
                PsiTreeUtil.getParentOfType(
                    completionParameters.getPosition(), SoyTemplateReferenceIdentifier.class);

            if (identifierElement == null) {
              return;
            }

            String identifier = identifierElement.getText();

            boolean isDelegate =
                PsiTreeUtil.getParentOfType(identifierElement, CallStatementElement.class)
                    .isDelegate();

            String prefix = identifier.replaceFirst("IntellijIdeaRulezzz", "");
            Collection<TemplateNameUtils.Fragment> completions =
                TemplateNameUtils.getPossibleNextIdentifierFragments(
                    completionParameters.getPosition().getProject(),
                    identifierElement,
                    prefix,
                    isDelegate);

            completionResultSet.addAllElements(
                completions.stream()
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

  /**
   * Complete fully qualified namespace fragments for alias declaration.
   */
  private void extendWithIdentifierFragmentsForAlias() {
    extend(
        CompletionType.BASIC,
        psiElement().inside(SoyAliasBlock.class),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(
              @NotNull CompletionParameters completionParameters,
              ProcessingContext processingContext,
              @NotNull CompletionResultSet completionResultSet) {
            PsiElement identifierElement =
                PsiTreeUtil.getParentOfType(
                    completionParameters.getPosition(), SoyNamespaceIdentifier.class);
            String identifier = identifierElement == null ? "" : identifierElement.getText();

            String prefix = identifier.replaceFirst("IntellijIdeaRulezzz", "");
            Collection<TemplateNameUtils.Fragment> completions =
                TemplateNameUtils.getTemplateNamespaceFragments(
                    completionParameters.getPosition().getProject(), prefix);

            completionResultSet.addAllElements(
                completions.stream()
                    .map(
                        (fragment) ->
                            LookupElementBuilder.create(fragment.text)
                                .withTypeText(
                                    fragment.isFinalFragment ? "Namespace" : "Partial namespace"))
                    .collect(Collectors.toList()));
          }
        });
  }

  /**
   * Complete parameter names for {param .. /} in template function calls.
   */
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
            CallStatementElement callStatement =
                (CallStatementElement)
                    PsiTreeUtil.findFirstParent(
                        position, elt -> elt instanceof CallStatementElement);

            if (callStatement == null) {
              return;
            }

            PsiElement identifier =
                PsiTreeUtil.findChildOfType(callStatement, SoyTemplateReferenceIdentifier.class);

            if (identifier == null) {
              return;
            }

            Collection<String> givenParameters = ParamUtils.getGivenParameters(callStatement);
            List<Variable> parameters =
                ParamUtils.getParametersForInvocation(position, identifier.getText()).stream()
                    .filter(v -> !givenParameters.contains(v.name))
                    .collect(Collectors.toList());

            completionResultSet.addAllElements(
                parameters.stream()
                    .map(
                        (variable) ->
                            LookupElementBuilder.create(variable.name).withTypeText(variable.type))
                    .collect(Collectors.toList()));
          }
        });
  }

  /**
   * Complete types in {@param ...} .
   */
  private void extendWithParameterTypes() {
    // Complete types in @param.
    extend(
        CompletionType.BASIC,
        psiElement()
            .andOr(
                psiElement().inside(SoyAtParamSingle.class).afterLeaf(":"),
                psiElement().inside(SoyAtInjectSingle.class).afterLeaf(":"),
                psiElement().inside(SoyAtStateSingle.class).afterLeaf(":"),

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
            completionResultSet.addAllElements(SOY_TYPE_LITERALS);
          }
        });
  }

  @Override
  public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
    return (typeChar == '.' || typeChar == '$');
  }

  /**
   * Whether the given element is directly preceded by an element matching the predicate (ignoring
   * whitespaces).
   */
  private boolean isPrecededBy(PsiElement startElement, Predicate<PsiElement> predicate) {
    for (PsiElement element = WhitespaceUtils.getPrevMeaningSibling(startElement);
        element != null;
        element = WhitespaceUtils.getPrevMeaningSibling(element)) {
      if (predicate.test(element)) {
        return true;
      }
    }
    return false;
  }
}
