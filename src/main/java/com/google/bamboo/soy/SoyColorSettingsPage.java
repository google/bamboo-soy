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

package com.google.bamboo.soy;

import static com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter.BUILTIN_TYPE;
import static com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter.COMMENT;
import static com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter.KEYWORD;
import static com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter.NUMBER;
import static com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter.OPERATOR_LITERAL;
import static com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter.STRING;
import static com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter.VARIABLE;

import com.google.bamboo.soy.icons.SoyIcons;
import com.google.bamboo.soy.insight.highlight.SoySyntaxHighlighter;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import java.util.Map;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyColorSettingsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] DESCRIPTORS =
      new AttributesDescriptor[]{
          new AttributesDescriptor("Numeric and boolean literals", NUMBER),
          new AttributesDescriptor("Built-in types", BUILTIN_TYPE),
          new AttributesDescriptor("Comments", COMMENT),
          new AttributesDescriptor("Tag names", KEYWORD),
          new AttributesDescriptor("Operators words", OPERATOR_LITERAL),
          new AttributesDescriptor("String literals", STRING),
          new AttributesDescriptor("Variables", VARIABLE),
      };

  private static final ImmutableMap<String, TextAttributesKey> ADDITIONAL_HIGHLIGHTING_TAG_MAP =
      ImmutableMap.of(
          "paramDef", VARIABLE,
          "varDef", VARIABLE,
          "varRef", VARIABLE);

  @Nullable
  @Override
  public Icon getIcon() {
    return SoyIcons.FILE;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new SoySyntaxHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    return Joiner.on('\n').join(
        "{namespace foo.bar}",
        "",
        "{template .localExample}",
        "  {@param <paramDef>foo</paramDef>: int}",
        "{/template}",
        "",
        "// Foo",
        "/* Bar */",
        "{template .bar}",
        "  {@param <paramDef>bar</paramDef>: string}",
        "  {let <varDef>$bar</varDef>: 'default' /}",
        "  {call .localExample}",
        "    {param <paramDef>foo</paramDef>: true /}",
        "  {/call}",
        "  {if <varRef>$foo</varRef> and 3}",
        "    {<varRef>$foo</varRef> ?: <varRef>$bar</varRef>}",
        "  {/if}",
        "  <!-- -->",
        "{/template}");
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ADDITIONAL_HIGHLIGHTING_TAG_MAP;
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Closure Template";
  }
}
