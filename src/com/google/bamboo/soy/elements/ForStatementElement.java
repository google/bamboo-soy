package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.parser.SoyBeginFor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface ForStatementElement extends Scope, PsiElement {
  @NotNull
  SoyBeginFor getBeginFor();
}
