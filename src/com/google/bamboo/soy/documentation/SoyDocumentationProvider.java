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

package com.google.bamboo.soy.documentation;

import com.google.bamboo.soy.parser.*;
import com.google.common.collect.ImmutableList;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoyDocumentationProvider extends AbstractDocumentationProvider {

  private static int MAX_COMMENT_PREVIEW_LENGTH = 96;

  private static String uncommentify(@NotNull String docComment) {
    return docComment
        .replaceFirst("^/\\*[\\*\\n\\r\\t ]*", "")
        .replaceAll("[ \\t\\n\\r\\*]*\\*/$", "")
        .replaceAll("\n[\\t\\n\\r\\* ]*", "\n");
  }

  private static String produceCommentPreview(@NotNull String docComment) {
    String preview = uncommentify(docComment);
    // Drop everything starting from the first @param.
    int firstAtParam = preview.indexOf("@param");
    if (firstAtParam != -1) {
      preview = preview.substring(0, firstAtParam);
    }
    // Drop newlines.
    preview = preview.replaceAll("[\\n\\r\\f\\t ]+", " ");

    if (preview.length() > MAX_COMMENT_PREVIEW_LENGTH) {
      preview = preview.substring(0, Math.min(preview.length(), MAX_COMMENT_PREVIEW_LENGTH));

      int lastSpaceIndex = preview.lastIndexOf(" ");
      // If this comment is entirely without whitespaces then it's a weird one so let's just return
      // with it.
      if (lastSpaceIndex == -1) {
        return preview;
      }

      // If the last space is not at the end of the truncated part, then drop the partial word at
      // end of the preview text.
      if (lastSpaceIndex != preview.length() - 1) {
        preview = preview.substring(0, lastSpaceIndex);
      }

      // If there is any sentence-delimiting punctuation in the preview then let's truncate after
      // the first one.
      Matcher endTokenMatcher = Pattern.compile("[:;.?!]").matcher(preview);
      if (endTokenMatcher.matches()) {
        preview = preview.substring(0, endTokenMatcher.start() + 1);
      }
      preview = preview + " [...]";
    }
    return preview;
  }

  @Contract("null, _ -> null")
  private static PsiElement firstParent(PsiElement element, Class... classes) {
    return PsiTreeUtil.findFirstParent(
        element, new PsiElementInstanceofSelector(ImmutableList.copyOf(classes)));
  }

  @Contract("null -> false")
  private static boolean isDocComment(PsiElement element) {
    return element instanceof PsiComment
        && ((PsiComment) element).getTokenType().equals(SoyTypes.DOC_COMMENT_BLOCK);
  }

  @Nullable
  private static PsiComment lookupCommentRecursivelyAfter(PsiElement element) {
    while (element != null) {
      PsiElement maybeComment = PsiTreeUtil.skipSiblingsForward(element, PsiWhiteSpace.class);
      if (maybeComment == null) {
        element = element.getParent();
        continue;
      }
      if (isDocComment(maybeComment)) {
        return (PsiComment) maybeComment;
      } else {
        return null;
      }
    }
    return null;
  }

  @Nullable
  private static PsiComment lookupCommentRecursivelyBefore(PsiElement element) {
    while (element != null) {
      PsiElement maybeComment = PsiTreeUtil.skipSiblingsBackward(element, PsiWhiteSpace.class);
      if (maybeComment == null) {
        element = element.getParent();
        continue;
      }
      if (isDocComment(maybeComment)) {
        return (PsiComment) maybeComment;
      } else {
        return null;
      }
    }
    return null;
  }

  @Nullable
  private static String getOptionalDocCommentBefore(PsiElement element) {
    PsiElement optCommentBefore =
        lookupCommentRecursivelyBefore(
            firstParent(
                firstParent(element, SoyBeginTemplate.class, SoyBeginDelegateTemplate.class),
                SoyTemplateBlock.class,
                SoyDelegateTemplateBlock.class));
    return optCommentBefore != null ? optCommentBefore.getText() : null;
  }

  @Nullable
  private static String getOptionalDocCommentAfter(PsiElement element) {
    PsiElement optCommentAfter =
        lookupCommentRecursivelyAfter(
            firstParent(element, SoyAtParamSingle.class, SoyAtInjectSingle.class));
    return optCommentAfter != null ? optCommentAfter.getText() : null;
  }

  @Nullable
  private static String getDocNearElement(PsiElement element) {
    String optComment = getOptionalDocCommentBefore(element);
    if (optComment == null) {
      optComment = getOptionalDocCommentAfter(element);
    }
    return optComment;
  }

  @Nullable
  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    Document document =
        FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
    if (document == null) return "";

    int lineNum = document.getLineNumber(element.getTextOffset()) + 1 /* count starts at zero */;
    String path = element.getContainingFile().getVirtualFile().getName();
    StringBuilder navigateInfo = new StringBuilder("Defined at ");
    navigateInfo.append(path);
    navigateInfo.append(":");
    navigateInfo.append(lineNum);
    String optDoc = getDocNearElement(element);
    if (optDoc != null) {
      navigateInfo.append("\n");
      navigateInfo.append(produceCommentPreview(optDoc));
    }
    return navigateInfo.toString();
  }

  private static class PsiElementInstanceofSelector implements Condition<PsiElement> {
    private final ImmutableList<Class> acceptedClasses;

    PsiElementInstanceofSelector(@NotNull ImmutableList<Class> acceptedParentClasses) {
      this.acceptedClasses = acceptedParentClasses;
    }

    @Override
    public boolean value(PsiElement element) {
      for (Class c : acceptedClasses) {
        if (c.isInstance(element)) {
          return true;
        }
      }
      return false;
    }
  }
}
