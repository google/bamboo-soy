// Copyright 2019 Google Inc.
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
import com.google.bamboo.soy.parser.SoyAtStateSingle;
import com.google.bamboo.soy.parser.impl.SoyAtStateSingleImpl;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class AtStateStub extends NamedStubBase<SoyAtStateSingle> {

  public static final Type TYPE = new Type();
  public final String type;

  AtStateStub(StubElement parent, String name, String type) {
    super(parent, TYPE, name);
    this.type = type;
  }

  static class Type extends IStubElementType<AtStateStub, SoyAtStateSingle> {

    Type() {
      super("AT_STATE", SoyLanguage.INSTANCE);
    }

    @Override
    public SoyAtStateSingle createPsi(@NotNull AtStateStub stub) {
      return new SoyAtStateSingleImpl(stub, this);
    }

    @NotNull
    @Override
    public AtStateStub createStub(@NotNull SoyAtStateSingle psi, StubElement parentStub) {
      return new AtStateStub(parentStub, psi.getName(), psi.getType());
    }

    @NotNull
    @Override
    public String getExternalId() {
      return "AT_STATE";
    }

    @Override
    public void serialize(@NotNull AtStateStub stub, @NotNull StubOutputStream dataStream)
        throws IOException {
      dataStream.writeName(stub.getName());
      dataStream.writeName(stub.type);
    }

    @NotNull
    @Override
    public AtStateStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub)
        throws IOException {
      final StringRef ref = dataStream.readName();
      final StringRef ref2 = dataStream.readName();
      return new AtStateStub(
          parentStub, ref.getString(), ref2.getString());
    }

    @Override
    public void indexStub(@NotNull AtStateStub stub, @NotNull IndexSink sink) {
    }
  }
}
