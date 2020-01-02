package com.google.bamboo.soy.lang;

import com.google.bamboo.soy.parser.SoyStatementList;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple scope abstraction that presumes hierarchy and up-down variable propagation.
 *
 * <p>Limitations:
 *
 * <p>1. Impossibility to restrict up-down propagation (for example, inside {let}...{/let}).
 *
 * <p>2. Ignorance of the statement order inside a single StatementList.
 */
public interface Scope {

  Scope EMPTY =
      new Scope() {
        public List<Variable> getLocalVariables() {
          return Collections.emptyList();
        }

        public Scope getParentScope() {
          return null;
        }
      };

  @Nullable
  static PsiElement getFirstScopeParent(PsiElement element) {
    return PsiTreeUtil.findFirstParent(element, true, psiElement -> psiElement instanceof Scope);
  }

  /**
   * The most concrete scope containing the given [element] or null if none found.
   */
  @Nullable
  static Scope getScope(PsiElement element) {
    PsiElement scope = getFirstScopeParent(element);
    // If the first scope is not a StatementList or a TemplateBlock, we must be in
    // a statement opening tag.
    if (!(scope instanceof SoyStatementList) && !(scope instanceof SoyTemplateBlock)) {
      scope = getFirstScopeParent(element);
    }
    return (Scope) scope;
  }

  /**
   * The most concrete scope containing the given [element] or Scope.EMPTY if none found.
   */
  @NotNull
  static Scope getScopeOrEmpty(PsiElement element) {
    Scope scope = getScope(element);
    return scope != null ? scope : EMPTY;
  }

  /**
   * Variables defined in the Scope (not in one of its parents).
   */
  @NotNull
  List<Variable> getLocalVariables();

  /**
   * All variables visible in the Scope.
   */
  @NotNull
  default List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<>(getLocalVariables());
    Scope parent = getParentScope();
    if (parent != null) {
      variables.addAll(parent.getVariables());
    }
    return variables;
  }

  @Nullable
  Scope getParentScope();
}
