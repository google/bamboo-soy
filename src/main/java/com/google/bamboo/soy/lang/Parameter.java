package com.google.bamboo.soy.lang;

import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import org.jetbrains.annotations.NotNull;

public class Parameter extends Variable {
  public final boolean isOptional;

  public Parameter(
      String name, String type, boolean isOptional, @NotNull SoyParamDefinitionIdentifier element) {
    super(name, type, element);
    this.isOptional = isOptional;
  }
}
