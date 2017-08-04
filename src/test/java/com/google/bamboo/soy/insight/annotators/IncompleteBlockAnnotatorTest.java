package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;
import com.google.bamboo.soy.elements.TagElement;

public class IncompleteBlockAnnotatorTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/insight/annotators";
  }

  public void testAnnotator() {
    myFixture.configureByFile("IncompleteBlock.soy");
    myFixture.checkHighlighting(false, false, true, true);
  }
}
