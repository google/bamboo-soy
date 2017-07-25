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

package com.google.bamboo.soy.format;

import com.google.bamboo.soy.parser.SoyTypes;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.xml.HtmlPolicy;
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyFormattingModelBuilder extends TemplateLanguageFormattingModelBuilder {

  @Override
  public TemplateLanguageBlock createTemplateLanguageBlock(
      @NotNull ASTNode node,
      @Nullable Wrap wrap,
      @Nullable Alignment alignment,
      @Nullable List<DataLanguageBlockWrapper> foreignChildren,
      @NotNull CodeStyleSettings codeStyleSettings) {
    final FormattingDocumentModelImpl documentModel =
        FormattingDocumentModelImpl.createOn(node.getPsi().getContainingFile());
    return new SoyBlock(
        this,
        codeStyleSettings,
        node,
        foreignChildren,
        new HtmlPolicy(codeStyleSettings, documentModel));
  }

  @NotNull
  public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
    final PsiFile file = element.getContainingFile();

    if (element.getNode().getElementType() == SoyTypes.OTHER) {
      return new SimpleTemplateLanguageFormattingModelBuilder().createModel(element, settings);
    } else {
      return new DocumentBasedFormattingModel(
          getRootBlock(file, file.getViewProvider(), settings),
          element.getProject(),
          settings,
          file.getFileType(),
          file);
    }
  }

  @Override
  public boolean dontFormatMyModel() {
    return false;
  }
}
