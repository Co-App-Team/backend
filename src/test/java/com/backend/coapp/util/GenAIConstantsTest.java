package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GenAIConstantsTest {
  @Test
  void testConstants() {
    assertEquals(13000, GenAIConstants.MAX_TOTAL_CHARACTERS);
    assertEquals(5000, GenAIConstants.MAX_PROMPT_CHARACTERS);
    assertEquals(3100, GenAIConstants.MAX_EXPERIENCE_SUMMARY_CHARACTER);
  }
}
