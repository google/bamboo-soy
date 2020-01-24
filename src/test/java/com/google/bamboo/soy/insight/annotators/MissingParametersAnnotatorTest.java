package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;

public class MissingParametersAnnotatorTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/insight/annotators";
  }

  public void testAnnotator() {
    myFixture.configureByFile("MissingParameters.soy");
    myFixture.checkHighlighting(false, false, true, true);
  }
}
