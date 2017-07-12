package com.google.bamboo.soy.scope;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Scope {
  Scope EMPTY = new Scope() {
    public List<Variable> getLocalVariables() {
      return new ArrayList<>();
    }

    public Scope getParentScope() {
      return null;
    }
  };

  @NotNull
  List<Variable> getLocalVariables();

  @Nullable
  Scope getParentScope();

  @NotNull
  default List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<>();
    variables.addAll(getLocalVariables());
    Scope parent = getParentScope();
    if (parent != null) {
      variables.addAll(parent.getVariables());
    }
    return variables;
  }

  @Nullable
  static Scope getScope(PsiElement element) {
    return (Scope)
        PsiTreeUtil.findFirstParent(element, true, psiElement -> psiElement instanceof Scope);
  }

  @NotNull
  static Scope getScopeOrEmpty(PsiElement element) {
    Scope scope = getScope(element);
    return scope != null ? scope : EMPTY;
  }
}
