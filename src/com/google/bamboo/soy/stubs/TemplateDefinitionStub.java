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

import com.google.bamboo.soy.SoyLanguage;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.parser.impl.SoyTemplateDefinitionIdentifierImpl;
import com.google.bamboo.soy.stubs.index.TemplateDefinitionIndex;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class TemplateDefinitionStub extends NamedStubBase<SoyTemplateDefinitionIdentifier> {
  static final Type TYPE = new Type();

  TemplateDefinitionStub(StubElement parent, String name) {
    super(parent, TYPE, name);
  }

  // May only be called when the stub tree is fully constructed.
  String getFullyQualifiedName() {
    return getName().startsWith(".") ? getNamespace() + getName() : getName();
  }

  // May only be called when the stub tree is fully constructed.
  String getNamespace() {
    return StubUtils.getContainingStubFile(this).getNamespace();
  }

  static class Type
      extends IStubElementType<TemplateDefinitionStub, SoyTemplateDefinitionIdentifier> {
    Type() {
      super("TEMPLATE_DEFINITION_IDENTIFIER", SoyLanguage.INSTANCE);
    }

    @Override
    public SoyTemplateDefinitionIdentifier createPsi(@NotNull TemplateDefinitionStub stub) {
      return new SoyTemplateDefinitionIdentifierImpl(stub, this);
    }

    @NotNull
    @Override
    public TemplateDefinitionStub createStub(
        @NotNull SoyTemplateDefinitionIdentifier psi, StubElement parentStub) {
      return new TemplateDefinitionStub(parentStub, psi.getName());
    }

    @NotNull
    @Override
    public String getExternalId() {
      return "TEMPLATE_DEFINITION_IDENTIFIER";
    }

    @Override
    public void serialize(
        @NotNull TemplateDefinitionStub stub, @NotNull StubOutputStream dataStream)
        throws IOException {
      dataStream.writeName(stub.getName());
    }

    @NotNull
    @Override
    public TemplateDefinitionStub deserialize(
        @NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
      final StringRef ref = dataStream.readName();
      return new TemplateDefinitionStub(parentStub, ref.getString());
    }

    @Override
    public void indexStub(@NotNull TemplateDefinitionStub stub, @NotNull IndexSink sink) {
      sink.occurrence(TemplateDefinitionIndex.KEY, stub.getFullyQualifiedName());
    }
  }
}
