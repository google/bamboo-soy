package com.google.bamboo.soy.lang;

import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.intellij.psi.PsiNamedElement;

public class Parameter extends Variable {
  public final boolean isOptional;

  public Parameter(String name, String type, boolean isOptional, PsiNamedElement element) {
    super(name, type, element);
    assert element instanceof SoyParamDefinitionIdentifier;
    this.isOptional = isOptional;
  }
}
