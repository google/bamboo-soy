package com.google.bamboo.soy.insight.quickfix;

import com.google.bamboo.soy.elements.ParamElement;
import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.file.SoyFileType;
import com.google.bamboo.soy.insight.folding.SoyRecursiveElementVisitor;
import com.google.bamboo.soy.parser.SoyAtParamSingle;
import com.google.bamboo.soy.parser.SoyParamDefinitionIdentifier;
import com.google.bamboo.soy.parser.SoyParamSpecificationIdentifier;
import com.google.common.collect.ImmutableList;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class RemoveUnusedParameterFix extends RemoveUnusedAtFixBase<SoyAtParamSingle> {

  public RemoveUnusedParameterFix(String paramName) {
    super(paramName);
  }

  @NotNull
  @Override
  public String getText() {
    return "Remove unused @param " + name + " and its specifications";
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return "Remove unused param";
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return true;
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  @Override
  void runFix(SoyAtParamSingle element) {
    final Document document = getContainingDocument(element);
    if (document == null) {
      return;
    }
    SoyParamDefinitionIdentifier paramDefinitionIdentifier =
        element.getParamDefinitionIdentifier();
    if (paramDefinitionIdentifier == null) {
      return;
    }
    deleteParamSpecifications(paramDefinitionIdentifier);
    deleteElement(element, document);
  }

  private static void deleteParamSpecifications(
      SoyParamDefinitionIdentifier paramDefinitionIdentifier) {
    List<SoyParamSpecificationIdentifier> paramSpecifications =
        getReferencingParamSpecificationIdentifiers(paramDefinitionIdentifier);
    for (SoyParamSpecificationIdentifier paramSpecification : paramSpecifications) {
      ParamElement paramElement =
          PsiTreeUtil.getParentOfType(paramSpecification, ParamElement.class);
      if (paramElement != null) {
        deleteElement(paramElement, getContainingDocument(paramElement));
      }
    }
  }

  private static List<SoyParamSpecificationIdentifier> getReferencingParamSpecificationIdentifiers(
      SoyParamDefinitionIdentifier paramDefinitionIdentifier) {
    Project project = paramDefinitionIdentifier.getProject();
    final ImmutableList.Builder<SoyParamSpecificationIdentifier> result = ImmutableList.builder();
    Collection<VirtualFile> virtualFiles =
        FileTypeIndex.getFiles(SoyFileType.INSTANCE, GlobalSearchScope.allScope(project));
    for (VirtualFile virtualFile : virtualFiles) {
      SoyFile soyFile = (SoyFile) PsiManager.getInstance(project).findFile(virtualFile);
      if (soyFile != null) {
        soyFile.accept(new SoyRecursiveElementVisitor() {
          @Override
          public void visitParamSpecificationIdentifier(
              @NotNull SoyParamSpecificationIdentifier identifier) {
            super.visitParamSpecificationIdentifier(identifier);
            PsiReference reference = identifier.getReference();
            if (reference != null && paramDefinitionIdentifier.equals(reference.resolve())) {
              result.add(identifier);
            }
          }
        });
      }
    }
    return result.build();
  }
}
