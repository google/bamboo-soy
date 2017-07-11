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

package com.google.bamboo.soy.parser;

import com.google.bamboo.soy.file.SoyFile;
import com.google.bamboo.soy.lexer.SoyLexer;
import com.google.bamboo.soy.stubs.FileStub;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class SoyParserDefinition implements ParserDefinition {
  public static final TokenSet WHITE_SPACES =
      TokenSet.create(TokenType.WHITE_SPACE, SoyTypes.HORIZONTAL_SPACE, SoyTypes.LINE_TERMINATOR);

  private static final TokenSet COMMENTS =
      TokenSet.create(SoyTypes.COMMENT_BLOCK, SoyTypes.DOC_COMMENT_BLOCK);

  private static final TokenSet STRINGS = TokenSet.create(SoyTypes.OTHER);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new SoyLexer();
  }

  @NotNull
  public TokenSet getWhitespaceTokens() {
    return WHITE_SPACES;
  }

  @NotNull
  public TokenSet getCommentTokens() {
    return COMMENTS;
  }

  @NotNull
  public TokenSet getStringLiteralElements() {
    return STRINGS;
  }

  @NotNull
  public PsiParser createParser(final Project project) {
    return new SoyParser();
  }

  @Override
  public IStubFileElementType<FileStub> getFileNodeType() {
    return FileStub.TYPE;
  }

  public PsiFile createFile(FileViewProvider viewProvider) {
    return new SoyFile(viewProvider);
  }

  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @NotNull
  public PsiElement createElement(ASTNode node) {
    return SoyTypes.Factory.createElement(node);
  }
}
