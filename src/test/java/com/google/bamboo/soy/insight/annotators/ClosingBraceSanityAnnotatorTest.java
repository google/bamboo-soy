package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.google.bamboo.soy.elements.TagElement;

public class ClosingBraceSanityAnnotatorTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/insight/annotators";
  }

  public void testAllTags() {
    for (Class clazz : ClosingBraceSanityAnnotator.mustCloseRBraceTags) {
      assertTrue(clazz.getName(), TagElement.class.isAssignableFrom(clazz));
    }
  }

  public void testAnnotator() {
    myFixture.configureByFile("ClosingBraceSanity.soy");
    myFixture.checkHighlighting(false, false, true, true);
  }
}
