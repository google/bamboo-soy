// Copyright 2020 Google Inc.
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

package com.google.bamboo.soy.insight.folding;

import com.google.bamboo.soy.elements.*;
import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyAtStateSingle;
import com.google.bamboo.soy.parser.SoyEndTag;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyFoldingBuilder extends CustomFoldingBuilder {

  private static final int MAX_PLACEHOLDER_LENGTH = 50;
  private static final Pattern BLOCK_COMMENT_PATTERN =
      Pattern.compile("^(/\\*{1,2})(.*)\\*/$", Pattern.DOTALL);
  private static final Pattern BLOCK_COMMENT_LINE_CONTINUATION =
      Pattern.compile("\\n\\s*\\*\\s*", Pattern.DOTALL);
  private static final Pattern BLOCK_COMMENT_NEWLINE_PATTERN =
      Pattern.compile("[\\r\\n]", Pattern.DOTALL);
  private static final Pattern BLOCK_COMMENT_WHITESPACE = Pattern.compile("\\s+", Pattern.DOTALL);

  @Override
  protected void buildLanguageFoldRegions(
      @NotNull List<FoldingDescriptor> descriptors,
      @NotNull PsiElement root,
      @NotNull Document document,
      boolean quick) {
    PsiElement toVisit = (root instanceof SoyFile) ? root.getFirstChild() : root;
    toVisit.accept(
        new SoyRecursiveElementVisitor() {
          @Override
          public void visitTemplateBlock(@NotNull SoyTemplateBlock element) {
            processTagBlockElement(element, descriptors);
            super.visitTemplateBlock(element);
          }

          @Override
          public void visitCallStatementElement(@NotNull CallStatementElement element) {
            if (!element.getBeginCall().isSelfClosed()) {
              processTagBlockElement(element, descriptors);
            }
            super.visitCallStatementElement(element);
          }

          @Override
          public void visitAtParamSingle(@NotNull SoyAtParamSingle element) {
            maybeAddDescriptorForDocComment(element.getDocComment(), descriptors);
            super.visitAtParamSingle(element);
          }

          @Override
          public void visitAtStateSingle(@NotNull SoyAtStateSingle element) {
            maybeAddDescriptorForDocComment(element.getDocComment(), descriptors);
            super.visitAtStateSingle(element);
          }
        });
  }

  private static void processTagBlockElement(
      @NotNull TagBlockElement element, @NotNull List<FoldingDescriptor> descriptors) {
    if (element.isIncomplete()) {
      return;
    }
    PsiComment commentElement = getCommentElement(element);
    maybeAddDescriptorForDocComment(commentElement, descriptors);
    TextRange meaningfulTextRange = buildMeaningfulTextRange(element, commentElement);
    if (meaningfulTextRange != null) {
      descriptors.add(new FoldingDescriptor(element, meaningfulTextRange));
    }
  }

  private static void maybeAddDescriptorForDocComment(
      @Nullable PsiComment commentElement, @NotNull List<FoldingDescriptor> descriptors) {
    if (commentElement != null
        && BLOCK_COMMENT_NEWLINE_PATTERN.matcher(commentElement.getText()).find()) {
      descriptors.add(new FoldingDescriptor(commentElement, commentElement.getTextRange()));
    }
  }

  @Nullable
  private static PsiComment getCommentElement(TagBlockElement element) {
    TagElement openingTag = element.getOpeningTag();
    return openingTag.getFirstChild() instanceof PsiComment
        ? (PsiComment) openingTag.getFirstChild()
        : null;
  }

  @Nullable
  private static TextRange buildMeaningfulTextRange(
      TagBlockElement element, PsiElement commentElement) {
    if (commentElement == null) {
      return element.getTextRange();
    }
    PsiElement firstMeaningfulElement = WhitespaceUtils.getNextMeaningSibling(commentElement);
    if (firstMeaningfulElement != null) {
      return TextRange.create(
          firstMeaningfulElement.getTextOffset(), element.getTextRange().getEndOffset());
    }
    return null;
  }

  @Override
  protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
    PsiElement element = node.getPsi();
    if (element instanceof TagBlockElement) {
      TagBlockElement block = (TagBlockElement) element;
      String openingTagText = block.getOpeningTag().getText();
      PsiElement firstMeaningfulChild = WhitespaceUtils.getFirstMeaningChild(block.getOpeningTag());
      if (firstMeaningfulChild == null) {
        return "...";
      }
      int tagStart = firstMeaningfulChild.getStartOffsetInParent();
      if (tagStart != 0) {
        // Strip leading doc comment.
        openingTagText = openingTagText.substring(tagStart);
      }
      return normalizePlaceHolderText(openingTagText)
          + "..."
          + normalizePlaceHolderText(getClosingTagText(element));
    }
    if (element instanceof PsiComment) {
      String placeholderText = buildCommentPlaceholderText(element);
      if (placeholderText != null) {
        return placeholderText;
      }
    }
    return "{...}";
  }

  @Nullable
  private String buildCommentPlaceholderText(PsiElement element) {
    Matcher matcher = BLOCK_COMMENT_PATTERN.matcher(element.getText());
    if (!matcher.find()) {
      return null;
    }

    String sanitizedComment =
        BLOCK_COMMENT_LINE_CONTINUATION.matcher(matcher.group(2)).replaceAll(" ");
    sanitizedComment = BLOCK_COMMENT_WHITESPACE.matcher(sanitizedComment).replaceAll(" ").trim();
    if (sanitizedComment.length() > MAX_PLACEHOLDER_LENGTH) {
      int lastSpaceIndex = sanitizedComment.lastIndexOf(' ', MAX_PLACEHOLDER_LENGTH);
      sanitizedComment =
          sanitizedComment.substring(
              0, lastSpaceIndex > 0 ? lastSpaceIndex : MAX_PLACEHOLDER_LENGTH);
      return matcher.group(1) + " " + sanitizedComment + " ...*/";
    }

    return matcher.group(1) + " " + sanitizedComment + " */";
  }

  /** This is a multiline doc comment that should be collapsed. */
  private static String getClosingTagText(PsiElement element) {
    SoyEndTag endTag = PsiTreeUtil.getChildOfType(element, SoyEndTag.class);
    return endTag == null ? "" : endTag.getText();
  }

  private static String normalizePlaceHolderText(@Nullable String text) {
    if (text == null) {
      return null;
    }

    if (text.length() <= MAX_PLACEHOLDER_LENGTH) {
      return text;
    }
    return StringUtil.trimMiddle(text, MAX_PLACEHOLDER_LENGTH);
  }

  @Override
  protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }

  @Override
  protected boolean isCustomFoldingCandidate(@NotNull ASTNode node) {
    return false;
  }
}
