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

package com.google.bamboo.soy;

import com.google.bamboo.soy.cache.TemplateCache;
import com.google.bamboo.soy.elements.TemplateDefinitionElement;
import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.parser.SoyAliasBlock;
import com.google.bamboo.soy.parser.SoyDelegateTemplateBlock;
import com.google.bamboo.soy.parser.SoyIdentifier;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public class TemplateNameUtils {
  public static TemplateDefinitionElement findTemplateDefinition(
      PsiElement element, String templateIdentifier) {
    List<TemplateDefinitionElement> definitions =
        findTemplateDefinitions(element, templateIdentifier);
    return definitions.size() >= 1 ? definitions.get(0) : null;
  }

  public static List<TemplateDefinitionElement> findTemplateDefinitions(
      PsiElement element, String templateIdentifier) {
    if (templateIdentifier.startsWith(".")) {
      return findLocalTemplateDefinitions(element)
          .stream()
          .filter(elt -> elt.getName().equals(templateIdentifier))
          .collect(Collectors.toList());
    } else {
      String normalizedIdentifier =
          normalizeTemplateIdentifier(element.getContainingFile(), templateIdentifier);
      return findTemplateDefinitions(element.getProject(), normalizedIdentifier);
    }
  }

  private static List<TemplateDefinitionElement> findTemplateDefinitions(
      Project project, String fullyQualifiedIdentifier) {
    List<TemplateDefinitionElement> matchedTemplates = new ArrayList<>();

    SoyProjectComponent component =
        project.getComponent(SoyProjectComponent.class);
    for (List<TemplateCache.TemplateCacheEntry> templatesForFile :
        component.templateCache.getTemplatesByFile().values()) {
      for (TemplateCache.TemplateCacheEntry template : templatesForFile) {
        if (template.fullyQualifiedIdentifier.equals(fullyQualifiedIdentifier)) {
          matchedTemplates.add(template.element);
        }
      }
    }
    return matchedTemplates;
  }

  public static Collection<TemplateDefinitionElement> findLocalTemplateDefinitions(
      PsiElement element) {
    PsiElement file =
        PsiTreeUtil.findFirstParent(
            element, psiElement -> psiElement instanceof SoyFile);

    if (file == null) {
      return new ArrayList<>();
    } else {
      Collection<PsiElement> templateBlocks =
          PsiTreeUtil.findChildrenOfAnyType(
              file, SoyTemplateBlock.class, SoyDelegateTemplateBlock.class);

      List<TemplateDefinitionElement> templates = new ArrayList<>();
      for (PsiElement templateBlock : templateBlocks) {
        TemplateDefinitionElement identifier =
            PsiTreeUtil.findChildOfType(templateBlock, TemplateDefinitionElement.class);
        if (identifier == null) continue;
        templates.add(identifier);
      }
      return templates;
    }
  }

  // TODO(thso): Simplify implementation instead of piggybacking on the complete template identifier
  // cache.
  public static Collection<String> getTemplateNamespaceFragments(
      Project project, String identifier) {
    List<String> possibleCompletions = new ArrayList<>();

    SoyProjectComponent component =
        project.getComponent(SoyProjectComponent.class);

    List<String> templates = new ArrayList<>();
    for (List<TemplateCache.TemplateCacheEntry> templatesForFile :
        component.templateCache.getTemplatesByFile().values()) {
      for (TemplateCache.TemplateCacheEntry template : templatesForFile) {
        templates.add(template.fullyQualifiedIdentifier);
      }
    }

    for (String template : templates) {
      if (template.startsWith(identifier)) {
        String rest = template.replaceFirst(identifier, "");
        if (rest.contains(".")) {
          possibleCompletions.add(identifier + rest.split("\\.")[0]);
        }
      }
    }

    return possibleCompletions;
  }

  public static Collection<String> getTemplateNameIdentifiersFragments(
      Project project, PsiElement identifierElement, String identifier) {
    List<String> possibleCompletions = new ArrayList<>();

    SoyProjectComponent component =
        project.getComponent(SoyProjectComponent.class);

    List<String> templates = new ArrayList<>();
    for (List<TemplateCache.TemplateCacheEntry> templatesForFile :
        component.templateCache.getTemplatesByFile().values()) {
      for (TemplateCache.TemplateCacheEntry template : templatesForFile) {
        templates.add(template.fullyQualifiedIdentifier);
      }
    }

    Map<String, String> aliases = getNamespaceAliases(identifierElement.getContainingFile());
    templates.addAll(aliases.values());

    String normalizedIdentifier =
        normalizeTemplateIdentifier(identifierElement.getContainingFile(), identifier);
    for (String template : templates) {
      if (template.startsWith(normalizedIdentifier)) {
        String rest = template.replaceFirst(normalizedIdentifier, "");
        possibleCompletions.add(identifier + rest.split("\\.")[0]);
      }
    }

    return possibleCompletions;
  }

  @Nullable
  public static String getNamespaceIdentifier(PsiElement element) {
    SoyIdentifier identifier =
        PsiTreeUtil.findChildOfType(element, SoyIdentifier.class);
    if (identifier != null) {
      return identifier.getText();
    } else {
      return null;
    }
  }

  private static String normalizeTemplateIdentifier(PsiFile file, String templateIdentifier) {
    if (templateIdentifier.startsWith(".")) {
      return templateIdentifier;
    } else {
      Map<String, String> aliases = getNamespaceAliases(file);
      for (String aliasesNamespace : aliases.keySet()) {
        String alias = aliases.get(aliasesNamespace);
        if (templateIdentifier.startsWith(alias)) {
          templateIdentifier = templateIdentifier.replace(alias, aliasesNamespace);
        }
      }
      return templateIdentifier;
    }
  }

  private static Map<String, String> getNamespaceAliases(PsiFile file) {
    Collection<SoyAliasBlock> aliasElements =
        PsiTreeUtil.findChildrenOfType(file, SoyAliasBlock.class);

    Map<String, String> aliases = new HashMap<>();

    aliasElements.forEach(
        alias -> {
          if (alias.getNamespaceIdentifier() != null) {
            String namespaceIdentifier = alias.getNamespaceIdentifier().getText();

            if (alias.getAliasIdentifier() != null) {
              aliases.put(namespaceIdentifier, alias.getAliasIdentifier().getText());
            } else {
              String[] namespaceFragments = namespaceIdentifier.split("\\.");
              String aliasIdentifier = namespaceFragments[namespaceFragments.length - 1];
              aliases.put(namespaceIdentifier, aliasIdentifier);
            }
          }
        });
    return aliases;
  }
}
