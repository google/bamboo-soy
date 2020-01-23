package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.lang.Variable;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.PsiElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ListComprehensionExprElement extends Scope, PsiElement {

  @Nullable
  SoyVariableDefinitionIdentifier getVariableDefinitionIdentifier();

  @Nullable
  @Override
  default Scope getParentScope() {
    return Scope.getScope(this);
  }

  @NotNull
  @Override
  default List<Variable> getLocalVariables() {
    return getVariableDefinitionIdentifier() != null
        ? ImmutableList.of(getVariableDefinitionIdentifier().toVariable())
        : ImmutableList.of();
  }
}
