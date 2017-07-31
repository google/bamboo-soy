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

import com.google.bamboo.soy.elements.TagElement.TagName;
import com.google.bamboo.soy.parser.SoyBeginCall;
import com.google.bamboo.soy.parser.SoyParamListElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CallStatementBase extends TagBlockElement, StatementBase {

  @NotNull
  List<SoyParamListElement> getParamListElementList();

  @NotNull
  SoyBeginCall getBeginCall();

  @NotNull
  default boolean isDelegate() {
    return getTagName() == TagName.DELCALL;
  }

  @Nullable
  default String getTemplateName() {
    try {
      return getBeginCall().getTemplateReferenceIdentifier().getText();
    } catch (NullPointerException e) {
      return null;
    }
  }
}
