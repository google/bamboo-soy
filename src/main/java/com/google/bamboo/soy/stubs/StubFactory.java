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

package com.google.bamboo.soy.stubs;

import com.google.common.collect.ImmutableMap;
import com.intellij.psi.stubs.IStubElementType;

public abstract class StubFactory {
  private static final ImmutableMap<String, IStubElementType> stubTypeByElement =
      ImmutableMap.of(
          "TEMPLATE_BLOCK", TemplateBlockStub.TYPE,
          "TEMPLATE_DEFINITION_IDENTIFIER", TemplateDefinitionStub.TYPE,
          "NAMESPACE_DECLARATION_IDENTIFIER", NamespaceDeclarationStub.TYPE,
          "AT_PARAM_SINGLE", AtParamStub.TYPE,
          "AT_STATE_SINGLE", AtStateStub.TYPE);

  public static IStubElementType<?, ?> getType(String elementName) {
    return stubTypeByElement.get(elementName);
  }
}
