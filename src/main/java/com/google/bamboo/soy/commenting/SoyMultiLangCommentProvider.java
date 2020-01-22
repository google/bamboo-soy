package com.google.bamboo.soy.commenting;

import com.google.bamboo.soy.SoyLanguage;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageCommenters;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider;
import org.jetbrains.annotations.Nullable;

/**
 * CommentByBlockCommentHandler does a bad job of selecting the <b>base</b> language (Soy) for
 * commenting even if the current selection language is the <b>template</b> language (HTML). This
 * provider makes sure that:
 * <ol>
 *   <li>A commenter is chosen ONLY if the start/end of the current selection belong to the same
 *       language. NB: This is <b>always</b> the case with the current IntelliJ implementation
 *       of block-commenting, which is wrong.</li>
 *   <li>A commenter for the specific language is selected if 1. above holds true.</li>
 * </ol>
 */
public class SoyMultiLangCommentProvider implements MultipleLangCommentProvider {

  @Nullable
  @Override
  public Commenter getLineCommenter(
      PsiFile file, Editor editor, Language lineStartLanguage, Language lineEndLanguage) {
    if (lineStartLanguage != null && lineStartLanguage == lineEndLanguage) {
      return LanguageCommenters.INSTANCE.forLanguage(lineStartLanguage);
    }
    return null;
  }

  @Override
  public boolean canProcess(PsiFile file, FileViewProvider viewProvider) {
    return file.getViewProvider().getLanguages().contains(SoyLanguage.INSTANCE);
  }
}
