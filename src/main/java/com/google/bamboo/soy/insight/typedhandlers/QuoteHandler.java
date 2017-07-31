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

import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * Inserts a matching closing character (quote, parenthesis, bracket) when typing one.
 */
public class QuoteHandler implements TypedActionHandler {

  private final Set<Pair<Character, Character>> matchingCharacters = ImmutableSet
      .of(Pair.create('"', '"'), Pair.create('\'', '\''), Pair.create('(', ')'),
          Pair.create('[', ']'));
  private final Set<Character> alwaysCloseCharacters = ImmutableSet.of('(', '[');
  private final Set<String> allowedPreviousCharacters = ImmutableSet
      .of("\n", " ", "[", "]", "(", ")", "=");
  private final Set<String> allowedNextCharacters = ImmutableSet.of("\n", " ", "]", ")", "}");
  private final TypedActionHandler myOriginalHandler;

  public QuoteHandler(TypedActionHandler originalHandler) {
    myOriginalHandler = originalHandler;
  }

  public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
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
      int countLeft = 0;
      int countRight = 0;

      // Number of opens on the left
      for (String c : textBeforeCaret.split("")) {
        if (c.equals(matchingPairReverse.first + "")) {
          countLeft++;
        } else if (c.equals(matchingPairReverse.second + "") && countLeft > 0) {
          countLeft--;
        }
      }

      // Number of closes on the right
      for (String c : textAfterCaret.split("")) {
        if (c.equals(matchingPairReverse.second + "")) {
          countRight++;
        } else if (c.equals(matchingPairReverse.first + "") && countRight > 0) {
          countRight--;
        }
      }

      if (countLeft <= countRight) {
        editor.getCaretModel().moveToOffset(caretOffset + 1);
        return;
      }
    } else {
      Pair<Character, Character> matchingPair = getMatchingPair(charTyped,
          p -> p.getFirst());
      if (matchingPair != null) {
        if ((alwaysCloseCharacters.contains(matchingPair.first) || allowedPreviousCharacters
            .contains(prevChar)) && allowedNextCharacters.contains(nextChar)
            && !nextChar.equals(matchingPair.second + "")) {
          document.insertString(editor.getCaretModel().getOffset(), matchingPair.second + "");

          if (editor.getProject() != null) {
            PsiDocumentManager.getInstance(editor.getProject()).commitDocument(document);
          }
        }
      }
    }

    myOriginalHandler.execute(editor, charTyped, dataContext);
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
