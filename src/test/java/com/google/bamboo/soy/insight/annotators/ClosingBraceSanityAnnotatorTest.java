package com.google.bamboo.soy.insight.annotators;

import com.google.bamboo.soy.elements.TagElement;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClosingBraceSanityAnnotatorTest extends TestCase {

  @Test
  public void testAllTags() {
    for (Class clazz : ClosingBraceSanityAnnotator.mustCloseRBraceTags) {
      assertTrue(clazz.getName(), TagElement.class.isAssignableFrom(clazz));
    }
  }
}
