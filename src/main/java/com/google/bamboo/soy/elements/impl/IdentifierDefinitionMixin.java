// Copyright 2020 Google Inc.
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

package com.google.bamboo.soy.elements.impl;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class IdentifierDefinitionMixin extends SoyIdentifierOwnerMixin {
  IdentifierDefinitionMixin(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public String getName() {
    if (getNameIdentifier() == null) {
      return "";
    }
    String name = getNameIdentifier().getText();
    return name.startsWith("$") ? name.substring(1) : name;
  }
}
