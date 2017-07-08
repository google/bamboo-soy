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

package com.google.bamboo.soy.format;

import com.google.bamboo.soy.elements.ParamListElementBase;
import com.google.bamboo.soy.elements.StatementBase;
import com.google.bamboo.soy.parser.SoyAtInjectSingle;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyCaseClause;
import com.google.bamboo.soy.parser.SoyDefaultClause;
import com.google.bamboo.soy.parser.SoyStatementList;
import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory;
import com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.xml.HtmlPolicy;
import com.intellij.psi.formatter.xml.SyntheticBlock;
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTag;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyFormattingModelBuilder extends TemplateLanguageFormattingModelBuilder {
  private static boolean isAlwaysIndented(PsiElement element) {
    return element instanceof ParamListElementBase
        || element instanceof SoyAtParamSingle
        || element instanceof SoyAtInjectSingle
        || element instanceof SoyDefaultClause
        || element instanceof SoyCaseClause;
  }

  @Override
  public TemplateLanguageBlock createTemplateLanguageBlock(
      @NotNull ASTNode node,
      @Nullable Wrap wrap,
      @Nullable Alignment alignment,
      @Nullable List<DataLanguageBlockWrapper> foreignChildren,
      @NotNull CodeStyleSettings codeStyleSettings) {
    final FormattingDocumentModelImpl documentModel =
        FormattingDocumentModelImpl.createOn(node.getPsi().getContainingFile());
    return new SoyBlock(
        this,
        codeStyleSettings,
        node,
        foreignChildren,
        new HtmlPolicy(codeStyleSettings, documentModel));
  }

  @NotNull
  public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
    final PsiFile file = element.getContainingFile();

    if (element.getNode().getElementType() == SoyTypes.OTHER) {
      return new SimpleTemplateLanguageFormattingModelBuilder().createModel(element, settings);
    } else {
      return new DocumentBasedFormattingModel(
          getRootBlock(file, file.getViewProvider(), settings),
          element.getProject(),
          settings,
          file.getFileType(),
          file);
    }
  }

  @Override
  public boolean dontFormatMyModel() {
    return false;
  }

  private static class SoyBlock extends TemplateLanguageBlock {
    private HtmlPolicy myHtmlPolicy;

    SoyBlock(
        @NotNull TemplateLanguageBlockFactory blockFactory,
        @NotNull CodeStyleSettings settings,
        @NotNull ASTNode node,
        @Nullable List<DataLanguageBlockWrapper> foreignChildren,
        HtmlPolicy htmlPolicy) {
      super(blockFactory, settings, node, foreignChildren);
      myHtmlPolicy = htmlPolicy;
    }

    /**
     * Three basic considerations around which this method is built:
     *
     * <p>1. All HTML-content nodes must be siblings (not nephews) to the statements that follow
     * them, otherwise the latter won't form a child [Block]. For example, for a case like this:
     *
     * <pre>
     * <div>
     *   {msg} ... {/msg}
     *   ...
     * </pre>
     *
     * this [PsiElement] subtree is acceptable:
     *
     * <pre>
     * OTHER
     * MsgStatement
     * </pre>
     *
     * and this one is not:
     *
     * <pre>
     * Content
     * |- OTHER
     * MsgStatement
     * </pre>
     *
     * <p>2. All HTML-content nodes must not be direct children of their parent logical blocks,
     * because the indentation cannot be applied to an HTML-content node itself.
     *
     * <p>From 1. and 2. follows the necessary [PsiElement] tree structure, i.e.,
     *
     * <pre>
     * IfStatement
     * ...
     * |- StatementList
     *    |- OTHER
     *    |- MsgStatement
     *    ...
     * </pre>
     *
     * for
     *
     * <pre>
     * {if $condition}
     *   <div>
     *     {msg}
     *       ...
     *     {/msg}
     * </pre>
     *
     * <p>3. HTML-content manages indentation of the child blocks independently. That means that
     * naive implementation, in which, for example, all StatementLists are indented from their
     * parents, can lead to an undesirable effect:
     *
     * <pre>
     * <div>
     *   {msg}
     *   [soy indent][HTML indent]<span> ... <span>
     *   [soy indent]{some other soy statement}
     *   {/msg}
     * </div>
     * </pre>
     *
     * In which HTML indent is automagically added, because from HTML perspective the tree looks
     * like this:
     *
     * <pre>
     * <div>
     * [HTML indent]<span> ... </span>
     * </div>
     * </pre>
     *
     * <p>The conclusion from all 3 premises is as follows: <b>Outside an HTML [Block]
     * StatementLists should be indented (2), so Statements should not; inside an HTML [Block]
     * StatementLists should not be indented (3), so Statements should.</b>
     *
     * <p>The last consideration is a simple optimisation idea:
     *
     * <p>1. StatementLists and Statements always interleave.
     *
     * <p>2. A logical child to an HTML Block can only be a Statement Block.
     *
     * <p>3. It is inefficient to go up from each of them to the root of the tree to discover
     * whether there is an HTML Block somewhere.
     *
     * <p>So we can simply always indent a direct logical child of an HTML Block (which would be a
     * Statement) and for all other Statements/StatementLists indent them <i>iff their closest
     * Statement/StatementList ancestor is not indented</i>. (You can quickly check that it works).
     */
    @Override
    public Indent getIndent() {
      if (myNode.getText().trim().length() == 0) {
        return Indent.getNoneIndent();
      }

      if (isAlwaysIndented(myNode.getPsi())) {
        return Indent.getNormalIndent();
      }

      if (hasIndentingForeignBlockParent()) {
        return Indent.getNormalIndent();
      }

      if (isStatementOrStatementContainer() && !isParentStatementOrStatementContainerIndented()) {
        return Indent.getNormalIndent();
      } else {
        return Indent.getNoneIndent();
      }
    }

    @Override
    public Alignment getAlignment() {
      return null;
    }

    @Override
    protected IElementType getTemplateTextElementType() {
      return SoyTypes.OTHER;
    }

    @Override
    public boolean isRequiredRange(TextRange range) {
      return false;
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
      return new ChildAttributes(Indent.getNoneIndent(), null);
    }

    private boolean hasIndentingForeignBlockParent() {
      BlockWithParent parent = getParent();

      while (parent instanceof DataLanguageBlockWrapper) {
        if (!(((DataLanguageBlockWrapper) parent).getOriginal() instanceof SyntheticBlock)) {
          ASTNode foreignNode = ((DataLanguageBlockWrapper) parent).getNode();
          // Returning false if it is an XmlTag that doesn't force indentation.
          return !(foreignNode instanceof XmlTag)
              || myHtmlPolicy.indentChildrenOf((XmlTag) foreignNode);
        }
        parent = parent.getParent();
      }
      return false;
    }

    private boolean isParentStatementOrStatementContainerIndented() {
      BlockWithParent parent = getParent();
      while (parent instanceof SoyBlock) {
        if (((SoyBlock) parent).isStatementOrStatementContainer()) {
          return ((SoyBlock) parent).getIndent() != Indent.getNoneIndent();
        }
        parent = parent.getParent();
      }
      return false;
    }

    private boolean isStatementOrStatementContainer() {
      return myNode.getPsi() instanceof SoyStatementList
          || myNode.getPsi() instanceof StatementBase;
    }
  }
}
