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

import com.google.bamboo.soy.lang.Parameter;
import com.google.bamboo.soy.SoyLanguage;
import com.google.bamboo.soy.lang.StateVariable;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.google.bamboo.soy.parser.impl.SoyTemplateBlockImpl;
import com.google.bamboo.soy.stubs.index.TemplateBlockIndex;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class TemplateBlockStub extends StubBase<SoyTemplateBlock> {
  static final Type TYPE = new Type();
  public final boolean isDelegate;

  TemplateBlockStub(StubElement parent, boolean isDelegate) {
    super(parent, TYPE);
    this.isDelegate = isDelegate;
  }

  // May only be called when the stub tree is fully constructed.
  public String getFullyQualifiedName() {
    TemplateDefinitionStub templateDefinition =
        (TemplateDefinitionStub) findChildStubByType(TemplateDefinitionStub.TYPE);
    return templateDefinition == null ? "" : templateDefinition.getFullyQualifiedName();
  }

  // May only be called when the stub tree is fully constructed.
  public String getName() {
    TemplateDefinitionStub templateDefinition =
        (TemplateDefinitionStub) findChildStubByType(TemplateDefinitionStub.TYPE);
    return templateDefinition == null ? "" : templateDefinition.getName();
  }

  // May only be called when the stub tree is fully constructed.
  public List<Parameter> getParameters() {
    return getChildrenStubs()
        .stream()
        .filter(stub -> stub instanceof AtParamStub)
        .map(stub -> ((AtParamStub) stub).getPsi().toParameter())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  // May only be called when the stub tree is fully constructed.
  public List<StateVariable> getStates() {
    return getChildrenStubs()
        .stream()
        .filter(stub -> stub instanceof AtStateStub)
        .map(stub -> ((AtStateStub) stub).getPsi().toStateVariable())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  static class Type extends IStubElementType<TemplateBlockStub, SoyTemplateBlock> {
    Type() {
      super("TEMPLATE_BLOCK", SoyLanguage.INSTANCE);
    }

    @Override
    public SoyTemplateBlock createPsi(@NotNull TemplateBlockStub stub) {
      return new SoyTemplateBlockImpl(stub, this);
    }

    @NotNull
    @Override
    public TemplateBlockStub createStub(@NotNull SoyTemplateBlock psi, StubElement parentStub) {
      return new TemplateBlockStub(parentStub, psi.isDelegate());
    }

    @NotNull
    @Override
    public String getExternalId() {
      return "TEMPLATE_BLOCK";
    }

    @Override
    public void serialize(@NotNull TemplateBlockStub stub, @NotNull StubOutputStream dataStream)
        throws IOException {
      dataStream.writeBoolean(stub.isDelegate);
    }

    @NotNull
    @Override
    public TemplateBlockStub deserialize(
        @NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
      return new TemplateBlockStub(parentStub, dataStream.readBoolean());
    }

    @Override
    public void indexStub(@NotNull TemplateBlockStub stub, @NotNull IndexSink sink) {
      sink.occurrence(TemplateBlockIndex.KEY, stub.getFullyQualifiedName());
    }
  }
}
