package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GenAIUsageConstantsTest {
  @Test
  public void testConstants() {
    assertEquals(20, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT);
  }
}
