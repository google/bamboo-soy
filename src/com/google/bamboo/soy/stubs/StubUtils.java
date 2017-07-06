package com.google.bamboo.soy.stubs;

import com.intellij.psi.stubs.StubElement;

public abstract class StubUtils {
  public static FileStub getContainingStubFile(StubElement e) {
    StubElement parent = e.getParentStub();
    while (parent != null) {
      if (parent instanceof FileStub) {
        return (FileStub) parent;
      }
      parent = parent.getParentStub();
    }
    return null;
  }
}
