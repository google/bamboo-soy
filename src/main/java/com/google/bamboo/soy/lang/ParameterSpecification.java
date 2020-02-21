// Copyright 2020 Google Inc.
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

package com.google.bamboo.soy.lang;

import com.google.bamboo.soy.parser.SoyParamSpecificationIdentifier;

/**
 * Wraps a param specification PsiElement in a template call and the respective parameter name for
 * easier processing.
 */
public class ParameterSpecification {
  private final String name;
  private final SoyParamSpecificationIdentifier identifier;

  public ParameterSpecification(SoyParamSpecificationIdentifier identifier) {
    this.name = identifier.getText();
    this.identifier = identifier;
  }

  public String name() {
    return name;
  }

  public SoyParamSpecificationIdentifier identifier() {
    return identifier;
  }
}
