package com.google.bamboo.soy.icons;

import com.google.bamboo.soy.elements.AtElementSingle;
import com.google.bamboo.soy.parser.SoyElementType;
import com.google.bamboo.soy.parser.SoyTypes;
import com.google.bamboo.soy.stubs.AtParamStub;
import com.google.bamboo.soy.stubs.AtStateStub;
import com.google.bamboo.soy.stubs.TemplateDefinitionStub;
import com.google.common.collect.ImmutableMap;
import com.intellij.ide.IconProvider;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class SoyElementIconProvider extends IconProvider {

  private static final Map<IElementType, Icon> ICONS_BY_ELEMENT_TYPE =
      ImmutableMap.<IElementType, Icon>builder()
          .put(SoyTypes.AT_INJECT_SINGLE, SoyIcons.CODE_INJECT)
          .put(SoyTypes.AT_PARAM, SoyIcons.CODE_PARAMETER)
          .put(SoyTypes.AT_PARAM_SINGLE, SoyIcons.CODE_PARAMETER)
          .put(SoyTypes.AT_STATE, SoyIcons.CODE_STATE)
          .put(SoyTypes.AT_STATE_SINGLE, SoyIcons.CODE_STATE)
          .put(SoyTypes.DEL_CALL_STATEMENT, SoyIcons.CODE_CALL)
          .put(SoyTypes.DIRECT_CALL_STATEMENT, SoyIcons.CODE_CALL)
          .put(SoyTypes.LET_COMPOUND_STATEMENT, SoyIcons.CODE_VARIABLE)
          .put(SoyTypes.LET_SINGLE_STATEMENT, SoyIcons.CODE_VARIABLE)
          .put(SoyTypes.NAMESPACE_BLOCK, SoyIcons.CODE_NAMESPACE)
          .put(SoyTypes.PARAM_DEFINITION_IDENTIFIER, SoyIcons.CODE_PARAMETER)
          .put(SoyTypes.PARAM_SPECIFICATION_IDENTIFIER, SoyIcons.CODE_PARAMETER)
          .put(SoyTypes.TEMPLATE_BLOCK, SoyIcons.CODE_TEMPLATE)
          .put(SoyTypes.TEMPLATE_DEFINITION_IDENTIFIER, SoyIcons.CODE_TEMPLATE)
          .put(SoyTypes.TEMPLATE_REFERENCE_IDENTIFIER, SoyIcons.CODE_CALL)
          .put(SoyTypes.VARIABLE_DEFINITION_IDENTIFIER, SoyIcons.CODE_PARAMETER)
          .put(SoyTypes.VARIABLE_REFERENCE_IDENTIFIER, SoyIcons.CODE_PARAMETER)
          .build();

  private static final Map<IStubElementType<? extends StubElement<?>, ? extends PsiElement>, Icon>
      ICONS_BY_STUB_ELEMENT_TYPE =
          ImmutableMap
              .<IStubElementType<? extends StubElement<?>, ? extends PsiElement>, Icon>builder()
              .put(AtParamStub.TYPE, SoyIcons.CODE_PARAMETER)
              .put(AtStateStub.TYPE, SoyIcons.CODE_STATE)
              .put(TemplateDefinitionStub.TYPE, SoyIcons.CODE_TEMPLATE)
              .build();

  @Nullable
  @Override
  public Icon getIcon(@NotNull PsiElement element, int flags) {
    if (element instanceof StubBasedPsiElement) {
      return ICONS_BY_STUB_ELEMENT_TYPE.get(
          ((StubBasedPsiElement<? extends StubElement<?>>) element).getElementType());
    }
    ASTNode node = element.getNode();
    if (node == null) {
      return null;
    }
    IElementType elementType = node.getElementType();
    if (!(elementType instanceof SoyElementType)) {
      return null;
    }
    if (elementType == SoyTypes.VARIABLE_DEFINITION_IDENTIFIER) {
      elementType = maybeGetVariableDefinitionElementType(element, elementType);
    }
    Icon icon = ICONS_BY_ELEMENT_TYPE.get(elementType);
    return icon == null ? SoyIcons.FILE : icon;
  }

  private IElementType maybeGetVariableDefinitionElementType(
      @NotNull PsiElement element, IElementType elementType) {
    PsiElement parent = PsiTreeUtil.getParentOfType(element, AtElementSingle.class);
    if (parent != null) {
      elementType =
          parent.getNode() == null
              ? SoyTypes.VARIABLE_DEFINITION_IDENTIFIER
              : parent.getNode().getElementType();
    }
    return elementType;
  }
}
