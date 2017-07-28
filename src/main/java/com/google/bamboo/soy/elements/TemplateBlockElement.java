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

package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.lang.Parameter;
import com.google.bamboo.soy.lang.Scope;
import com.google.bamboo.soy.parser.SoyAtInjectSingle;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.stubs.TemplateBlockStub;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The PSI element that represents the template block definition.
 */
public interface TemplateBlockElement
    extends StubBasedPsiElement<TemplateBlockStub>, PsiNamedElement, TagBase, Scope {

  @NotNull
  List<SoyAtInjectSingle> getAtInjectSingleList();

  @NotNull
  List<SoyAtParamSingle> getAtParamSingleList();

  @Nullable
  SoyTemplateDefinitionIdentifier getDefinitionIdentifier();

  boolean isDelegate();

  @NotNull
  List<Parameter> getParameters();
}
