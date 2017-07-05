package com.google.bamboo.soy.elements;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public abstract class SoyStubBasedPsiElementBase<T extends StubElement> extends StubBasedPsiElementBase<T> {
  public SoyStubBasedPsiElementBase(@NotNull T stub,
      @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public SoyStubBasedPsiElementBase(@NotNull ASTNode node) {
    super(node);
  }

  public SoyStubBasedPsiElementBase(T stub, IElementType nodeType, ASTNode node) {
    super(stub, nodeType, node);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + getNode().getElementType().toString() + ")";
  }
}
