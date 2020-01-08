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

package com.google.bamboo.soy.elements;

import com.google.bamboo.soy.parser.SoyBeginCall;
import com.google.bamboo.soy.parser.SoyEndTag;
import com.google.bamboo.soy.parser.SoyParamListElement;
import com.google.bamboo.soy.parser.SoyTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CallStatementElement extends TagBlockElement, StatementElement {

  @NotNull
  List<SoyParamListElement> getParamListElementList();

  @NotNull
  SoyBeginCall getBeginCall();

  @NotNull
  default boolean isDelegate() {
    return getTagNameTokenType() == SoyTypes.DELCALL;
  }

  @Nullable
  default String getTemplateName() {
    try {
      return getBeginCall().getTemplateReferenceIdentifier().getText();
    } catch (NullPointerException e) {
      return null;
    }
  }

  @Override
  default boolean isIncomplete() {
    return !getBeginCall().isSelfClosed() && !(getLastChild() instanceof SoyEndTag);
  }
}
