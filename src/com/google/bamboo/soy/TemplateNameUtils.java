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

import com.google.bamboo.soy.elements.TemplateDefinitionElement;
import com.google.bamboo.soy.parser.*;
import com.google.bamboo.soy.stubs.index.TemplateDefinitionIndex;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;
import java.util.stream.Collectors;

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

  private static ImmutableList<TemplateDefinitionElement> findTemplateDefinitions(
      Project project, String fullyQualifiedIdentifier) {
    return ImmutableList.copyOf(TemplateDefinitionIndex.INSTANCE.get(
        fullyQualifiedIdentifier,
        project,
        GlobalSearchScope.allScope(project)));
  }

  public static Collection<TemplateDefinitionElement> findLocalTemplateDefinitions(
      PsiElement element) {
    PsiFile file = element.getContainingFile();
    return TemplateDefinitionIndex.INSTANCE.getAllKeys(file.getProject())
        .stream()
        .flatMap((key) -> TemplateDefinitionIndex.INSTANCE
            .get(key, file.getProject(), GlobalSearchScope.fileScope(file))
            .stream())
        .collect(
            Collectors.toList());
  }

  // TODO(thso): Simplify implementation instead of piggybacking on the complete template identifier
  // cache.
  public static Collection<String> getTemplateNamespaceFragments(
      Project project, String identifier) {
    List<String> possibleCompletions = new ArrayList<>();

    TemplateDefinitionIndex.INSTANCE.getAllKeys(project)
        .forEach((key) -> {
          if (key.startsWith(identifier)) {
            String rest = key.substring(identifier.length());
            if (rest.contains(".")) {
              possibleCompletions.add(identifier + rest.split("\\.")[0]);
            }
          }
        });
    return possibleCompletions;
  }

  public static Collection<String> getTemplateNameIdentifiersFragments(
      Project project, PsiElement identifierElement, String identifier) {
    List<String> possibleCompletions = new ArrayList<>();

    List<String> templates = new ArrayList<>();
    templates.addAll(TemplateDefinitionIndex.INSTANCE.getAllKeys(project));

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
