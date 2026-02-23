package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ExperienceConstantsTest {
  @Test
  public void testConstants() {
    assertEquals(2000, ExperienceConstants.MAX_EXPERIENCE_DESCRIPTION_LENGTH);
    assertNotNull(new ApplicationConstants());
  }
}
