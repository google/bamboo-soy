package com.google.bamboo.soy.scope;

import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.intellij.psi.PsiNamedElement;

public class Variable {
  public final String name;
  public final String type;
  public final boolean isOptional;
  public final PsiNamedElement element;

  public Variable(String name, String type, boolean isOptional, PsiNamedElement element) {
    assert element instanceof SoyParamDefinitionIdentifier
        || element instanceof SoyVariableDefinitionIdentifier;

    this.name = name.replaceFirst("^\\$", "");
    this.type = type;
    this.isOptional = isOptional;
    this.element = element;
  }
}
