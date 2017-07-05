package com.google.bamboo.soy.stubs.index;

import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class TemplateDefinitionIndex extends StringStubIndexExtension<SoyTemplateDefinitionIdentifier> {
  public static final StubIndexKey<String, SoyTemplateDefinitionIdentifier> KEY = StubIndexKey
      .createIndexKey("SoyTemplateDefinitionIdentifier");
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
