package com.google.bamboo.soy.elements;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WhitespaceUtils {
  @Nullable
  public static PsiElement getFirstMeaningChild(PsiElement element) {
    PsiElement first = element.getFirstChild();
    return first instanceof PsiWhiteSpace || first instanceof PsiComment
        ? getNextMeaningSibling(first)
        : first;
  }

  @Nullable
  public static PsiElement getLastMeaningChild(PsiElement element) {
    PsiElement last = element.getLastChild();
    return last instanceof PsiWhiteSpace || last instanceof PsiComment
        ? getPrevMeaningSibling(last)
        : last;
  }

  @Nullable
  public static PsiElement getNextMeaningSibling(PsiElement element) {
    return PsiTreeUtil.skipSiblingsForward(element, PsiWhiteSpace.class, PsiComment.class);
  }

  @Nullable
  public static PsiElement getPrevMeaningSibling(PsiElement element) {
    return PsiTreeUtil.skipSiblingsBackward(element, PsiWhiteSpace.class, PsiComment.class);
  }
}
