package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.parser.SoyBeginForeach;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface ForeachStatementElement extends Scope, PsiElement {
  @NotNull
  SoyBeginForeach getBeginForeach();
}
