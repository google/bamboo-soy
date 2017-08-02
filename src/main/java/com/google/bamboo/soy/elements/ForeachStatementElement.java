package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.parser.SoyBeginForeach;
import org.jetbrains.annotations.NotNull;

public interface ForeachStatementElement extends Scope, TagBlockElement, StatementElement {

  @NotNull
  SoyBeginForeach getBeginForeach();
}
