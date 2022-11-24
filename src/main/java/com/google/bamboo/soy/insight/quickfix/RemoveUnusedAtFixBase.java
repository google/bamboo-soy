package com.google.bamboo.soy.insight.quickfix;

import com.google.bamboo.soy.elements.AtElementSingle;
import com.google.bamboo.soy.elements.WhitespaceUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public abstract class RemoveUnusedAtFixBase<T extends AtElementSingle>
    implements LocalQuickFix, IntentionAction {

  protected final String name;

  RemoveUnusedAtFixBase(String name) {
    this.name = name;
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return true;
  }

  @Override
  public void applyFix(@NotNull Project project,
      @NotNull ProblemDescriptor descriptor) {
    final PsiElement element = descriptor.getPsiElement();
    if (!(element instanceof AtElementSingle)) {
      return;
    }

    // We expect the right concrete type here.
    runFix((T) element);
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file)
      throws IncorrectOperationException {
    int offset = editor.getCaretModel().getOffset();
    PsiElement psiElement = file.findElementAt(offset);
    if (psiElement == null || !psiElement.isValid()) {
      return;
    }
    if (psiElement instanceof PsiWhiteSpace && offset > 0) {
      // The caret might be located right after the closing brace and before a newline, in which
      // case the current PsiElement is PsiWhiteSpace.
      psiElement = file.findElementAt(offset - 1);
    }
    AtElementSingle atElementSingle = PsiTreeUtil
        .getParentOfType(psiElement, AtElementSingle.class);
    if (atElementSingle == null) {
      return;
    }
    runFix((T) atElementSingle);
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  abstract void runFix(T element);

  static Document getContainingDocument(PsiElement element) {
    return PsiDocumentManager.getInstance(element.getProject())
        .getDocument(element.getContainingFile());
  }

  static void deleteElement(PsiElement element, Document document) {
    ASTNode atParamNode = element.getNode();
    PsiElement nextMeaningfulNode = PsiTreeUtil
        .skipSiblingsForward(element, PsiWhiteSpace.class);
    nextMeaningfulNode = nextMeaningfulNode == null ? null
        : WhitespaceUtils.getFirstNonWhitespaceChild(nextMeaningfulNode);
    TextRange rangeToDelete = computeRangeToDelete(element, atParamNode, nextMeaningfulNode);
    document.deleteString(rangeToDelete.getStartOffset(), rangeToDelete.getEndOffset());
  }

  @NotNull
  private static TextRange computeRangeToDelete(PsiElement element, ASTNode atParamNode,
      PsiElement nextMeaningfulNode) {
    if (nextMeaningfulNode != null) {
      return new TextRange(atParamNode.getStartOffset(),
          nextMeaningfulNode.getNode().getStartOffset());
    }
    PsiElement prevMeaningfulNode = PsiTreeUtil
        .skipSiblingsBackward(element, PsiWhiteSpace.class);
    return new TextRange(
        prevMeaningfulNode != null ? prevMeaningfulNode.getTextRange().getEndOffset() :
            atParamNode.getStartOffset(), atParamNode.getTextRange().getEndOffset());
  }
}
