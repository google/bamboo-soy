package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.parser.SoyBeginFor;
import org.jetbrains.annotations.NotNull;

public interface ForStatementElement extends Scope, TagBlockElement, StatementElement {

  @NotNull
  SoyBeginFor getBeginFor();
}
