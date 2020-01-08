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

package com.google.bamboo.soy.file;

import com.google.bamboo.soy.SoyLanguage;
import com.google.bamboo.soy.parser.SoyNamespaceDeclarationIdentifier;
import com.google.bamboo.soy.stubs.FileStub;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class SoyFile extends PsiFileBase {
  public SoyFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, SoyLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return SoyFileType.INSTANCE;
  }

  @Override
  public FileStub getStub() {
    return (FileStub) super.getStub();
  }

  @Override
  public String toString() {
    return "Closure Template File";
  }

  public String getNamespace() {
    if (getStub() != null) {
      return getStub().getNamespace();
    }
    SoyNamespaceDeclarationIdentifier namespaceDeclaration =
        PsiTreeUtil.findChildOfType(this, SoyNamespaceDeclarationIdentifier.class);
    return namespaceDeclaration == null ? "" : namespaceDeclaration.getName();
  }
}
