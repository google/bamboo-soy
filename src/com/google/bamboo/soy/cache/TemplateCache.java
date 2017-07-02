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

package com.google.bamboo.soy.cache;

import com.google.bamboo.soy.TemplateNameUtils;
import com.google.bamboo.soy.elements.TemplateDefinitionElement;
import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.file.SoyFileType;
import com.google.bamboo.soy.parser.SoyNamespaceBlock;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateCache {
  private final Project project;
  private boolean isProjectIndex = false;
  private final Map<String, List<TemplateCacheEntry>> templatesByFile;

  public TemplateCache(Project project) {
    this.project = project;
    templatesByFile = new HashMap<>();
  }

  public Map<String, List<TemplateCacheEntry>> getTemplatesByFile() {
    if (!isProjectIndex) {
      indexProjectTemplates(project);
      isProjectIndex = true;
    }
    return templatesByFile;
  }

  public void indexProjectTemplates(Project project) {
    Collection<VirtualFile> virtualFiles =
        FileBasedIndex.getInstance()
            .getContainingFiles(
                FileTypeIndex.NAME,
                SoyFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
    for (VirtualFile virtualFile : virtualFiles) {
      indexFile(project, virtualFile);
    }
  }

  public void indexFile(Project project, VirtualFile virtualFile) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
    if (!(psiFile instanceof SoyFile)) {
      return;
    }

    SoyFile closureTemplateFile = (SoyFile) psiFile;
    if (closureTemplateFile.getVirtualFile() != null) {
      SoyNamespaceBlock namespaceBlock =
          PsiTreeUtil.findChildOfType(closureTemplateFile, SoyNamespaceBlock.class);
      String fileNamespace = TemplateNameUtils.getNamespaceIdentifier(namespaceBlock);

      if (fileNamespace == null) return;

      Collection<TemplateDefinitionElement> identifiers =
          TemplateNameUtils.findLocalTemplateDefinitions(namespaceBlock);
      templatesByFile.put(
          closureTemplateFile.getVirtualFile().getUrl(),
          identifiers
              .stream()
              .map(element -> new TemplateCacheEntry(fileNamespace, element.getName(), element))
              .collect(Collectors.toList()));
    }
  }

  public void removeFileFromIndex(VirtualFile virtualFile) {
    templatesByFile.remove(virtualFile.getCanonicalPath());
  }

  public static class TemplateCacheEntry {
    public final String namespace;
    public final String identifier;
    public final String fullyQualifiedIdentifier;
    public final TemplateDefinitionElement element;

    TemplateCacheEntry(String namespace, String identifier, TemplateDefinitionElement element) {
      this.namespace = namespace;
      this.identifier = identifier;
      this.element = element;

      if (identifier.startsWith(".")) {
        this.fullyQualifiedIdentifier = namespace + identifier;
      } else {
        this.fullyQualifiedIdentifier = identifier;
      }
    }
  }
}
