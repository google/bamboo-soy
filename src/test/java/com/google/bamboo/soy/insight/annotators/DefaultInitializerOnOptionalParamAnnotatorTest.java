package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.SoyCodeInsightFixtureTestCase;

public class DefaultInitializerOnOptionalParamAnnotatorTest extends SoyCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/insight/annotators";
  }

  public void testAnnotator() {
    myFixture.configureByFile("DefaultInitializerOnOptionalParam.soy");
    myFixture.checkHighlighting(false, false, true, true);
  }
}
