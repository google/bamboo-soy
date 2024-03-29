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
import com.google.bamboo.soy.file.SoyFile;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import org.jetbrains.annotations.NotNull;

public class FileStub extends PsiFileStubImpl<SoyFile> {

  public static final Type TYPE = new Type("SoyFile", SoyLanguage.INSTANCE);

  public FileStub(SoyFile file) {
    super(file);
  }

  @Override
  @NotNull
  public IStubFileElementType getType() {
    return TYPE;
  }

  // May only be called when the stub tree is fully constructed.
  public String getNamespace() {
    NamespaceDeclarationStub namespaceDeclaration =
        findChildStubByType(NamespaceDeclarationStub.TYPE);
    return namespaceDeclaration == null ? "" : namespaceDeclaration.getName();
  }

  static class Type extends IStubFileElementType<FileStub> {

    public static final int VERSION = 4;

    public Type(String debugName, Language language) {
      super(debugName, language);
    }

    @Override
    public StubBuilder getBuilder() {
      return new DefaultStubBuilder() {
        @Override
        @NotNull
        protected StubElement<SoyFile> createStubForFile(@NotNull PsiFile file) {
          return new FileStub((SoyFile) file);
        }
      };
    }

    @Override
    public int getStubVersion() {
      return VERSION;
    }

    @Override
    public void serialize(@NotNull FileStub stub, @NotNull StubOutputStream dataStream) {
    }

    @Override
    @NotNull
    public FileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) {
      return new FileStub(null);
    }

    @Override
    @NotNull
    public String getExternalId() {
      return "SoyFile";
    }
  }
}
