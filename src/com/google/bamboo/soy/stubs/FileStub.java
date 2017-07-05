package com.google.bamboo.soy.stubs;


import com.google.bamboo.soy.SoyLanguage;
import com.google.bamboo.soy.file.SoyFile;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.*;
import com.intellij.psi.tree.IStubFileElementType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class FileStub extends PsiFileStubImpl<SoyFile> {
  public static final Type TYPE = new Type("SoyFile", SoyLanguage.INSTANCE);

  public FileStub(SoyFile file) {
    super(file);
  }

  @Override
  public IStubFileElementType getType() {
    return TYPE;
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
        protected StubElement createStubForFile(@NotNull PsiFile file) {
          return new FileStub((SoyFile) file);
        }
      };
    }

    @Override
    public int getStubVersion() {
      return VERSION;
    }

    @Override
    public void serialize(@NotNull FileStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    }

    @NotNull
    @Override
    public FileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
      return new FileStub(null);
    }

    @NotNull
    @Override
    public String getExternalId() {
      return "SoyFile";
    }
  }
}
