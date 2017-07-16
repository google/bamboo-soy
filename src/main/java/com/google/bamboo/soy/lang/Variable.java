package com.google.bamboo.soy.lang;

import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyVariableDefinitionIdentifier;
import com.intellij.psi.PsiNamedElement;

public class Variable {
  public final String name;
  public final String type;
  public final PsiNamedElement element;

  public Variable(String name, String type, PsiNamedElement element) {
    assert element instanceof SoyParamDefinitionIdentifier
        || element instanceof SoyVariableDefinitionIdentifier;

    this.name = name.replaceFirst("^\\$", "");
    this.type = type;
    this.element = element;
  }
}
