package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Variable;
import com.google.bamboo.soy.parser.SoyLetCompoundStatement;
import com.google.bamboo.soy.parser.SoyLetSingleStatement;
import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.intellij.psi.PsiElement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StatementListElement extends Scope, PsiElement {
  @NotNull
  List<SoyLetCompoundStatement> getLetCompoundStatementList();

  @NotNull
  List<SoyLetSingleStatement> getLetSingleStatementList();

  @Nullable
  @Override
  default Scope getParentScope() {
    return Scope.getScope(this);
  }

  @NotNull
  @Override
  default List<Variable> getLocalVariables() {
    return Stream.concat(
        getLetSingleStatementList()
            .stream()
            .map(SoyLetSingleStatement::getVariableDefinitionIdentifier),
        getLetCompoundStatementList()
            .stream()
            .map((let) -> let.getBeginLet().getVariableDefinitionIdentifier()))
        .map(SoyVariableDefinitionIdentifier::toVariable)
        .collect(Collectors.toList());
  }
}
