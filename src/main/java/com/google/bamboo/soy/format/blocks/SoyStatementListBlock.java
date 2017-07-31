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

package com.google.bamboo.soy.format.blocks;

import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.HtmlPolicy;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyStatementListBlock extends SoyBlock {

  SoyStatementListBlock(
      @NotNull TemplateLanguageBlockFactory blockFactory,
      @NotNull CodeStyleSettings settings,
      @NotNull ASTNode node,
      @Nullable List<DataLanguageBlockWrapper> foreignChildren,
      HtmlPolicy htmlPolicy) {
    super(blockFactory, settings, node, foreignChildren, htmlPolicy);
  }
}
