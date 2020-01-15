package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;

public class UnusedParametersAnnotatorTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/insight/annotators";
  }

  public void testAnnotator() {
    myFixture.configureByFile("UnusedParameters.soy");
    myFixture.checkHighlighting(false, false, false, false);
  }
}
