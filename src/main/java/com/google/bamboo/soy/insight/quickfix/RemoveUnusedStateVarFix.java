package com.google.bamboo.soy.insight.quickfix;

import com.google.bamboo.soy.parser.SoyAtStateSingle;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.intellij.openapi.editor.Document;
import org.jetbrains.annotations.NotNull;

public class RemoveUnusedStateVarFix extends RemoveUnusedAtFixBase<SoyAtStateSingle> {

  public RemoveUnusedStateVarFix(String stateName) {
    super(stateName);
  }

  @NotNull
  @Override
  public String getText() {
    return "Remove unused @state " + name;
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return "Remove unused state var";
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  @Override
  void runFix(SoyAtStateSingle element) {
    final Document document = getContainingDocument(element);
    if (document == null) {
      return;
    }
    SoyParamDefinitionIdentifier paramDefinitionIdentifier =
        element.getParamDefinitionIdentifier();
    if (paramDefinitionIdentifier == null) {
      return;
    }
    deleteElement(element, document);
  }
}
