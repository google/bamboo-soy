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

package com.google.bamboo.soy.structure

import com.google.bamboo.soy.file.SoyFile
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.openapi.editor.Editor


class SoyStructureViewModel(val file: SoyFile,
                            editor: Editor?) : TextEditorBasedStructureViewModel(editor,
    file),
    StructureViewModel.ElementInfoProvider {
  override fun getRoot(): StructureViewTreeElement = getTreeElement(file)

  override fun isAlwaysShowsPlus(p0: StructureViewTreeElement?): Boolean = false

  override fun isAlwaysLeaf(p0: StructureViewTreeElement?): Boolean = false
}
