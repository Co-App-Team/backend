package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GenAIUsageConstantsTest {
  @Test
  void testConstants() {
    assertEquals(20, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT);
  }
}
