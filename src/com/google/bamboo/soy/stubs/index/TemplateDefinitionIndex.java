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

package com.google.bamboo.soy.stubs.index;

import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class TemplateDefinitionIndex
    extends StringStubIndexExtension<SoyTemplateDefinitionIdentifier> {
  public static final StubIndexKey<String, SoyTemplateDefinitionIdentifier> KEY =
      StubIndexKey.createIndexKey("SoyTemplateDefinitionIdentifier");
  public static final TemplateDefinitionIndex INSTANCE = new TemplateDefinitionIndex();

  @NotNull
  @Override
  public StubIndexKey<String, SoyTemplateDefinitionIdentifier> getKey() {
    return KEY;
  }

  @NotNull
  @Override
  public Collection<String> getAllKeys(Project project) {
    try {
      return super.getAllKeys(project);
    } catch (ProcessCanceledException e) {
      return new ArrayList<>();
    }
  }
}
