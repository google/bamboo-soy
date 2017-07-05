package com.google.bamboo.soy.stubs;

import com.intellij.psi.stubs.IStubElementType;

public abstract class StubFactory {
  public static IStubElementType<?,?> getType(String elementName) {
    switch(elementName) {
      case "TEMPLATE_DEFINITION_IDENTIFIER":
        return TemplateDefinitionStub.TYPE;
      default:
        return null;
    }
  }
}
