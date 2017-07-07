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
import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.parser.SoyAliasBlock;
import com.google.bamboo.soy.parser.SoyTemplateDefinitionIdentifier;
import com.google.bamboo.soy.stubs.index.NamespaceDeclarationIndex;
import com.google.bamboo.soy.stubs.index.TemplateDefinitionIndex;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateNameUtils {
  /* Finds the only TemplateDefinition by its exact name. */
  public static TemplateDefinitionElement findTemplateDefinition(
      PsiElement element, String templateIdentifier) {
    List<TemplateDefinitionElement> definitions =
        findTemplateDefinitions(element, templateIdentifier);
    return definitions.size() >= 1 ? definitions.get(0) : null;
  }

  /* Finds the matching TemplateDefinition by their exact name. */
  public static List<TemplateDefinitionElement> findTemplateDefinitions(
      PsiElement element, String identifier) {
    if (identifier.startsWith(".")) {
      identifier = ((SoyFile) element.getContainingFile()).getNamespace() + identifier;
    } else {
      Map<String, String> aliases = getNamespaceAliases(element.getContainingFile());
      identifier = normalizeTemplateIdentifier(aliases, identifier);
    }

    Project project = element.getProject();
    return ImmutableList.copyOf(
        TemplateDefinitionIndex.INSTANCE.get(
            identifier, project, GlobalSearchScope.allScope(project)));
  }

  /* Finds all template names in the given file. */
  public static List<String> findLocalTemplateNames(PsiElement element) {
    PsiFile file = element.getContainingFile();
    return TemplateDefinitionIndex.INSTANCE
        .getAllKeys(file.getProject())
        .stream()
        .flatMap(
            (key) ->
                TemplateDefinitionIndex.INSTANCE
                    .get(
                        key, file.getProject(), GlobalSearchScope.fileScope(file.getOriginalFile()))
                    .stream()
                    .map(SoyTemplateDefinitionIdentifier::getName))
        .collect(Collectors.toList());
  }

  /* Finds all namespace names starting with the given prefix */
  public static List<String> getTemplateNamespaceFragments(Project project, String prefix) {
    return NamespaceDeclarationIndex.INSTANCE
        .getAllKeys(project)
        .stream()
        .filter((key) -> key.startsWith(prefix))
        .map((name) -> getNextFragment(name, prefix))
        .collect(Collectors.toList());
  }

  /*
   * Finds all fully qualified template names starting with a given prefix with respect to
   * aliases and template visibility.
   * */
  public static Collection<String> getTemplateNameIdentifiersFragments(
      Project project, PsiElement identifierElement, String identifier) {
    Map<String, String> aliases = getNamespaceAliases(identifierElement.getContainingFile());
    return denormalizeTemplateNames(
            aliases,
            TemplateDefinitionIndex.INSTANCE
                .getAllKeys(project)
                .stream()
                // Assuming that private templates are those whose name ends with _
                .filter((key) -> !key.endsWith("_")))
        .filter((key) -> key.startsWith(identifier))
        .map((name) -> getNextFragment(name, identifier))
        .collect(Collectors.toList());
  }

  private static Stream<String> denormalizeTemplateNames(
      Map<String, String> aliases, Stream<String> templateNames) {
    return templateNames.flatMap(
        (name) -> {
          List<String> variants = new ArrayList<>();
          variants.add(name);
          for (Map.Entry<String, String> entry : aliases.entrySet()) {
            if (name.startsWith(entry.getKey())) {
              variants.add(name.replace(entry.getKey(), entry.getValue()));
            }
          }
          return variants.stream();
        });
  }

  private static String normalizeTemplateIdentifier(
      Map<String, String> aliases, String templateIdentifier) {
    if (templateIdentifier.startsWith(".")) {
      return templateIdentifier;
    } else {
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
            String aliasIdentifier;
            if (alias.getAliasIdentifier() != null) {
              aliasIdentifier = alias.getAliasIdentifier().getText();
            } else {
              String[] namespaceFragments = namespaceIdentifier.split("\\.");
              aliasIdentifier = namespaceFragments[namespaceFragments.length - 1];
            }
            // Adding dots to prevent in-token matching.
            aliases.put(namespaceIdentifier + ".", aliasIdentifier + ".");
          }
        });
    return aliases;
  }

  private static String getNextFragment(final String name, final String beginning) {
    return beginning + name.substring(beginning.length()).split("\\.")[0];
  }
}
