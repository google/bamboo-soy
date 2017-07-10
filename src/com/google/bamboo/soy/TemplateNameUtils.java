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

import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.parser.SoyAliasBlock;
import com.google.bamboo.soy.parser.SoyTemplateBlock;
import com.google.bamboo.soy.stubs.index.NamespaceDeclarationIndex;
import com.google.bamboo.soy.stubs.index.TemplateBlockIndex;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A helper class for efficient template and namespace lookups.
 *
 * <p>Performed operations are based on the stub trees.
 */
public class TemplateNameUtils {
  /* Finds the only SoyTemplateBlock by its exact name. */
  public static SoyTemplateBlock findTemplateDeclaration(
      PsiElement element, String templateIdentifier) {
    List<SoyTemplateBlock> declarations = findTemplateDeclarations(element, templateIdentifier);
    return declarations.size() >= 1 ? declarations.get(0) : null;
  }

  /* Finds the matching SoyTemplateBlock by their exact name. */
  public static List<SoyTemplateBlock> findTemplateDeclarations(
      PsiElement element, String identifier) {
    if (identifier.startsWith(".")) {
      identifier = ((SoyFile) element.getContainingFile()).getNamespace() + identifier;
    } else {
      Map<String, String> aliases = getNamespaceAliases(element.getContainingFile());
      identifier = normalizeIdentifier(aliases, identifier);
    }

    Project project = element.getProject();
    return TemplateBlockIndex.INSTANCE
        .get(identifier, project, GlobalSearchScope.allScope(project))
        .stream()
        .filter((block) -> block.getDefinitionIdentifier() != null)
        .collect(Collectors.toList());
  }

  /* Finds all local template names in the given file. */
  public static List<String> findLocalTemplateNames(PsiElement element) {
    PsiFile file = element.getContainingFile();
    return TemplateBlockIndex.INSTANCE
        .getAllKeys(file.getProject())
        .stream()
        .flatMap(
            (key) ->
                TemplateBlockIndex.INSTANCE
                    .get(
                        key, file.getProject(), GlobalSearchScope.fileScope(file.getOriginalFile()))
                    .stream()
                    .filter((block) -> !block.isDelegate())
                    .map(SoyTemplateBlock::getName))
        .collect(Collectors.toList());
  }

  /* Finds all namespace names starting with the given prefix */
  public static List<Fragment> getTemplateNamespaceFragments(Project project, String prefix) {
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
  public static Collection<Fragment> getPossibleNextIdentifierFragments(
      Project project, PsiElement identifierElement, String identifier, boolean isDelegate) {
    Map<String, String> aliases = getNamespaceAliases(identifierElement.getContainingFile());
    Pattern aliasedNamespacesRegex = getPrefixesRegex(aliases.keySet());

    GlobalSearchScope scope =
        isDelegate
            ? GlobalSearchScope.allScope(project)
            : GlobalSearchScope.allScope(project)
                .intersectWith(
                    GlobalSearchScope.notScope(
                        GlobalSearchScope.fileScope(
                            identifierElement.getContainingFile().getOriginalFile())));

    return TemplateBlockIndex.INSTANCE
        .getAllKeys(project)
        .stream()

        // Filter out private templates, assuming those end with "_".
        .filter((key) -> !key.endsWith("_"))

        // Filter out deltemplates or normal templates based on `isDelegate`.
        .filter(
            (key) -> {
              return TemplateBlockIndex.INSTANCE
                  .get(key, project, scope)
                  .stream()
                  .anyMatch((block) -> block.isDelegate() == isDelegate);
            })

        // Project matches into denormalized key space.
        .flatMap((key) -> denormalizeIdentifier(aliasedNamespacesRegex, aliases, key))

        // Ensure that once denormalized the template identifiers still match.
        .filter((key) -> key.startsWith(identifier))

        // Collect next fragments.
        .map((key) -> getNextFragment(key, identifier))
        .collect(Collectors.toList());
  }

  private static Stream<String> denormalizeIdentifier(
      Pattern prefixesRegex, Map<String, String> aliases, String identifier) {
    List<String> identifiers = new ArrayList<>();

    identifiers.add(identifier);
    if (prefixesRegex.asPredicate().test(identifier)) {
      for (Map.Entry<String, String> entry : aliases.entrySet()) {
        if (identifier.startsWith(entry.getKey())) {
          identifiers.add(identifier.replace(entry.getKey(), entry.getValue()));
        }
      }
    }
    return identifiers.stream();
  }

  private static String normalizeIdentifier(Map<String, String> aliases, String identifier) {
    if (identifier.startsWith(".")) {
      return identifier;
    }

    for (String aliasesNamespace : aliases.keySet()) {
      String alias = aliases.get(aliasesNamespace);
      if (identifier.startsWith(alias)) {
        return identifier.replace(alias, aliasesNamespace);
      }
    }

    return identifier;
  }

  private static Pattern getPrefixesRegex(Collection<String> prefixes) {
    return Pattern.compile(
        "^("
            + prefixes
                .stream()
                .map((prefix) -> prefix.replace(".", "\\."))
                .collect(Collectors.joining("|"))
            + ")");
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

  private static Fragment getNextFragment(final String name, final String beginning) {
    String[] fragments = name.substring(beginning.length()).split("\\.");
    return new Fragment(beginning + fragments[0], fragments.length == 1);
  }

  public static class Fragment {
    public final String text;
    public final boolean isFinalFragment;

    Fragment(String text, boolean isFinalFragment) {
      this.text = text;
      this.isFinalFragment = isFinalFragment;
    }
  }
}
