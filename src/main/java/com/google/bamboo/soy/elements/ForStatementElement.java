package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.lang.Variable;
import com.google.bamboo.soy.parser.SoyBeginFor;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ForStatementElement extends Scope, TagBlockElement, StatementElement {

  @NotNull
  SoyBeginFor getBeginFor();

  @Nullable
  @Override
  default Scope getParentScope() {
    return Scope.getScope(this);
  }

  @NotNull
  @Override
  default List<Variable> getLocalVariables() {
    return getBeginFor().getVariableDefinitionIdentifier() != null
        ? ImmutableList.of(getBeginFor().getVariableDefinitionIdentifier().toVariable())
        : ImmutableList.of();
  }
}
