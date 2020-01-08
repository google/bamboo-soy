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

package com.google.bamboo.soy.insight.structure

import com.google.bamboo.soy.file.SoyFile
import com.google.bamboo.soy.file.SoyFileType
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.structureView.impl.StructureViewComposite
import com.intellij.ide.structureView.impl.TemplateLanguageStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.psi.PsiFile

class SoyStructureViewFactory : PsiStructureViewFactory {
  override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
    return object : TemplateLanguageStructureViewBuilder(psiFile) {
      override fun createMainBuilder(mainFile: PsiFile): TreeBasedStructureViewBuilder? {
        if (!psiFile.isValid) return null

        return object : TreeBasedStructureViewBuilder() {
          override fun createStructureViewModel(editor: Editor?): StructureViewModel {
            return SoyStructureViewModel(psiFile as SoyFile, editor)
          }
        }
      }
    }
  }
}
