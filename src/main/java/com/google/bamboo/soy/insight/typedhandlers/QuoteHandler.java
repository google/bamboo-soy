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

package com.google.bamboo.soy.insight.typedhandlers;

import com.google.bamboo.soy.file.SoyFileType;
import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * Inserts a matching closing character (quote, parenthesis, bracket) when typing one.
 */
public class QuoteHandler extends TypedHandlerDelegate {

  private final Set<Pair<Character, Character>> matchingCharacters = ImmutableSet
      .of(Pair.create('"', '"'), Pair.create('\'', '\''), Pair.create('(', ')'),
          Pair.create('[', ']'));
  private final Set<Character> alwaysCloseCharacters = ImmutableSet.of('(', '[');
  private final Set<String> allowedPreviousCharacters = ImmutableSet
      .of("\n", " ", "[", "(", "=");
  private final Set<String> allowedNextCharacters = ImmutableSet.of("\n", " ", "]", ")", "}");

  @Override
  public Result beforeCharTyped(char charTyped, final Project project, final Editor editor,
      final PsiFile file, final FileType fileType) {
    if (file.getFileType() != SoyFileType.INSTANCE && file.getFileType() != HtmlFileType.INSTANCE) {
      return Result.CONTINUE;
    }

    Document document = editor.getDocument();
    int caretOffset = editor.getCaretModel().getOffset();

    String prevChar = getPreviousChar(document, caretOffset);
    String nextChar = getNextChar(document, caretOffset);

    int lineNumber = document.getLineNumber(caretOffset);
    String textBeforeCaret =
        document.getText(new TextRange(document.getLineStartOffset(lineNumber),
            caretOffset));
    String textAfterCaret =
        document.getText(new TextRange(caretOffset,
            document.getLineEndOffset(lineNumber)));

    Pair<Character, Character> matchingPairReverse = getMatchingPair(charTyped,
        p -> p.getSecond());
    if (matchingPairReverse != null && nextChar.equals(charTyped + "")) {
      boolean pairOfEqualChars = (matchingPairReverse.first == matchingPairReverse.second);

      // Number of opens on the left of the caret.
      int countLeft = computeCount(textBeforeCaret, matchingPairReverse.first,
          matchingPairReverse.second);

      // Number of closes on the right of the caret.
      int countRight = computeCount(textAfterCaret, matchingPairReverse.second,
          matchingPairReverse.first);

      // When the pair is made of equal characters (like quotes) then only trigger if there is
      // a balance of 1-1 around the caret, which means that the quote is already closed and
      // inserting a new quote would create an imbalance.
      if (((!pairOfEqualChars && countLeft <= countRight) || (pairOfEqualChars
          && countLeft == countRight)) && countRight > 0) {
        editor.getCaretModel().moveToOffset(caretOffset + 1);
        return Result.STOP;
      }
    } else {
      Pair<Character, Character> matchingPair = getMatchingPair(charTyped,
          p -> p.getFirst());
      if (matchingPair != null) {
        if ((alwaysCloseCharacters.contains(matchingPair.first) || (allowedPreviousCharacters
            .contains(prevChar)) && allowedNextCharacters.contains(nextChar)
            && !nextChar.equals(matchingPair.second + ""))) {
          document.insertString(editor.getCaretModel().getOffset(), matchingPair.second + "");

          if (editor.getProject() != null) {
            PsiDocumentManager.getInstance(editor.getProject()).commitDocument(document);
          }
        }
      }
    }

    return Result.CONTINUE;
  }

  /**
   * Returns the count of the number of opens or closes in the given {@code text}.
   */
  private int computeCount(String text, char first, char second) {
    boolean pairOfEqualChars = (first == second);

    int count = 0;
    for (String c : text.split("")) {
      if (c.equals(first + "")) {
        if (pairOfEqualChars && count > 0) {
          count--;
        } else {
          count++;
        }
      } else if (c.equals(second + "") && count > 0) {
        count--;
      }
    }
    return count;
  }

  private Pair<Character, Character> getMatchingPair(Character charTyped,
      Function<Pair<Character, Character>, Character> getCharacter) {
    for (Pair<Character, Character> charPair : matchingCharacters) {
      if (getCharacter.apply(charPair).equals(charTyped)) {
        return charPair;
      }
    }

    return null;
  }

  private String getPreviousChar(Document document, int offset) {
    return offset <= 0 ? " " : document.getText(new TextRange(offset - 1, offset));
  }

  private String getNextChar(Document document, int offset) {
    return offset >= document.getTextLength()
        ? " "
        : document.getText(new TextRange(offset, offset + 1));
  }
}
