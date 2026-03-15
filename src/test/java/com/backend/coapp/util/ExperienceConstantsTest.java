package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExperienceConstantsTest {
  @Test
  void testConstants() {
    assertEquals(1000, ExperienceConstants.MAX_EXPERIENCE_DESCRIPTION_LENGTH);
  }
}
