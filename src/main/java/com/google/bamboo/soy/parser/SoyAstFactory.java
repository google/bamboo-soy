package com.google.bamboo.soy.parser;

import com.google.bamboo.soy.file.SoyFileViewProvider;
import com.intellij.lang.ASTFactory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.templateLanguages.OuterLanguageElementImpl;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyAstFactory extends ASTFactory {
  @Override
  public @Nullable LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
    if (type == SoyFileViewProvider.OUTER_ELEMENT_TYPE) {
      return new OuterLanguageElementImpl(type, text);
    }
    return super.createLeaf(type, text);
  }
}
