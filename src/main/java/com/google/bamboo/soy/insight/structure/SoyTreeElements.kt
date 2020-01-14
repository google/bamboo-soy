// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.bamboo.soy.insight.structure

import com.google.bamboo.soy.elements.AtElementSingle
import com.google.bamboo.soy.elements.CallStatementElement
import com.google.bamboo.soy.elements.ParamElement
import com.google.bamboo.soy.elements.TagBlockElement
import com.google.bamboo.soy.file.SoyFile
import com.google.bamboo.soy.icons.SoyIcons
import com.google.bamboo.soy.parser.SoyAtInjectSingle
import com.google.bamboo.soy.parser.SoyAtParamSingle
import com.google.bamboo.soy.parser.SoyAtStateSingle
import com.google.bamboo.soy.parser.SoyLetCompoundStatement
import com.google.bamboo.soy.parser.SoyLetSingleStatement
import com.google.bamboo.soy.parser.SoyMsgStatement
import com.google.bamboo.soy.parser.SoyNamespaceBlock
import com.google.bamboo.soy.parser.SoyParamListElement
import com.google.bamboo.soy.parser.SoyTemplateBlock
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.NotNull
import javax.swing.Icon


/**
 * Builds a subtree corresponding to a given [PsiElement]
 */
fun getTreeElement(psiElement: PsiElement): PsiTreeElementBase<PsiElement> =
  when (psiElement) {
    is CallStatementElement -> CallTreeElement(psiElement)
    is SoyLetCompoundStatement -> LetCompoundTreeElement(psiElement)
    is SoyLetSingleStatement -> LetSingleTreeElement(psiElement)
    is ParamElement -> ParamTreeElement(psiElement)
    is SoyTemplateBlock -> TemplateTreeElement(psiElement)
    is SoyFile -> FileTreeElement(psiElement)
    is SoyMsgStatement -> MsgTreeElement(psiElement)
    is AtElementSingle -> AtTreeElement(psiElement)
    else -> BaseTreeElement(psiElement)
  }


/**
 * Returns a presentable name for a [PsiElement] or null if the element should not be seen in the
 * structure view.
 */
private fun getPresentableName(psiElement: PsiElement): String? =
  when (psiElement) {
    is TagBlockElement -> psiElement.tagName
    is SoyNamespaceBlock -> "namespace"
    is SoyLetSingleStatement -> "let"
    is ParamElement -> "param"
    is SoyMsgStatement -> "msg"
    is SoyAtInjectSingle -> "@inject"
    is SoyAtParamSingle -> "@param"
    is SoyAtStateSingle -> "@state"
    else -> null
  }

/**
 * A base class for all Soy TreeElements. The descendants would usually override the methods
 * relevant for presentation.
 */
private open class BaseTreeElement(psiElement: PsiElement)
  : PsiTreeElementBase<PsiElement>(psiElement) {
  override fun getChildrenBase(): Collection<StructureViewTreeElement> =
      getChildren(value)

  override fun getPresentableText(): String? = getPresentableName(value)

  override fun getIcon(open: Boolean): Icon? = super.getIcon(open) ?: SoyIcons.FILE

  protected fun getChildren(psiElement: PsiElement): Collection<PsiTreeElementBase<PsiElement>> =
      psiElement.children.map { child ->
        if (getPresentableName(child) != null)
          listOf(getTreeElement(child))
        else
          getChildren(child)
      }.flatten()

  private val MAX_TEXT_LENGTH = 50
  protected fun shortenTextIfLong(@NotNull text: String): String {
    if (text.length <= MAX_TEXT_LENGTH) {
      return text
    }

    var index = MAX_TEXT_LENGTH
    while (index > MAX_TEXT_LENGTH - 20) {
      if (!Character.isLetter(text[index])) {
        break
      }
      index--
    }

    return text.substring(0, if (Character.isLetter(index)) MAX_TEXT_LENGTH else index) + "\u2026"
  }
}

/**
 * A TreeElement for call statements.
 */
private class CallTreeElement(val psiElement: CallStatementElement) : BaseTreeElement(psiElement) {
  override fun getPresentableText(): String? =
      getPresentableName(psiElement) + " ${psiElement.templateName}"
}

/**
 * A TreeElement for the [SoyFile].
 */
private class FileTreeElement(val psiFile: SoyFile) : BaseTreeElement(psiFile) {
  override fun getPresentableText(): String? = psiFile.name
}

/**
 * A TreeElement for the [SoyLetCompoundStatement].
 */
private class LetCompoundTreeElement(val psiElement: SoyLetCompoundStatement)
  : BaseTreeElement(psiElement) {
  override fun getPresentableText(): String? =
      getPresentableName(psiElement) + " ${psiElement.beginLet.variableDefinitionIdentifier?.name}"
}

/**
 * A TreeElement for the [SoyLetSingleStatement].
 */
private class LetSingleTreeElement(val psiElement: SoyLetSingleStatement)
  : BaseTreeElement(psiElement) {
  override fun getPresentableText(): String? =
      getPresentableName(psiElement) + " ${psiElement.variableDefinitionIdentifier.name}"

  override fun getLocationString(): String? =
      if (psiElement.expr != null) ": ${psiElement.expr?.text}" else null
}

/**
 * A TreeElement for the [SoyMsgStatement].
 */
private class MsgTreeElement(val psiElement: SoyMsgStatement)
  : BaseTreeElement(psiElement) {
  override fun getLocationString(): String = shortenTextIfLong(psiElement.description ?: "")
}

/**
 * A TreeElement for the [SoyParamListElement].
 */
private class ParamTreeElement(val psiElement: ParamElement) : BaseTreeElement(psiElement) {
  override fun getPresentableText(): String? =
      getPresentableName(psiElement) + " ${psiElement.paramName}"

  override fun getLocationString(): String? =
      if (psiElement.inlinedValue != null) ": ${psiElement.inlinedValue}" else ""
}

/**
 * A TreeElement for [SoyAtInjectSingle], [SoyAtParamSingle] and [SoyAtStateSingle].
 */
private class AtTreeElement(val psiElement: AtElementSingle) : BaseTreeElement(psiElement) {
  override fun getPresentableText(): String? =
      if (psiElement.paramDefinitionIdentifier != null)
        shortenTextIfLong(atElementSinglePresentableText(psiElement))
      else null


  private fun atElementSinglePresentableText(psiElement: AtElementSingle): String {
    return "${getPresentableName(psiElement)} ${psiElement.name}" +
        buildTypeAndDefaultValue(psiElement)
  }

  private fun buildTypeAndDefaultValue(psiElement: AtElementSingle): String {
    var type = ""
    var defaultValue = ""
    if (psiElement is SoyAtParamSingle) {
      type = psiElement.type
      defaultValue = psiElement.defaultInitializerExpr?.text ?: ""
    }
    if (psiElement is SoyAtStateSingle) {
      type = psiElement.type
      defaultValue = psiElement.defaultInitializerExpr?.text ?: ""
    }
    if (type.isEmpty()) {
      return if (defaultValue.isEmpty()) "" else " := $defaultValue"
    }
    return if (defaultValue.isEmpty()) ": $type" else ": $type = $defaultValue"
  }
}

/**
 * A TreeElement for the template blocks.
 */
private class TemplateTreeElement(val psiElement: SoyTemplateBlock) : BaseTreeElement(psiElement) {
  override fun getPresentableText(): String? =
      getPresentableName(psiElement) + " ${psiElement.name}"
}
