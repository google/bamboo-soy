package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.parser.SoyLetCompoundStatement;
import com.google.bamboo.soy.parser.SoyLetSingleStatement;
import com.google.bamboo.soy.lang.Scope;
import com.intellij.psi.PsiElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface StatementListElement extends Scope, PsiElement {
  @NotNull
  List<SoyLetCompoundStatement> getLetCompoundStatementList();

  @NotNull
  List<SoyLetSingleStatement> getLetSingleStatementList();
}
