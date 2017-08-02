package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.lang.Variable;
import com.google.bamboo.soy.parser.SoyBeginForeach;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ForeachStatementElement extends Scope, TagBlockElement, StatementElement {

  @NotNull
  SoyBeginForeach getBeginForeach();

  @Nullable
  @Override
  default Scope getParentScope() {
    return Scope.getScope(this);
  }

  @NotNull
  @Override
  default List<Variable> getLocalVariables() {
    return getBeginForeach().getVariableDefinitionIdentifier() != null
        ? ImmutableList.of(getBeginForeach().getVariableDefinitionIdentifier().toVariable())
        : ImmutableList.of();
  }
}
