package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ApplicationConstantsTest {

  @Test
  public void testConstants() {
    assertEquals(2000, ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH);
    assertEquals(2000, ApplicationConstants.MAX_JOB_NOTES_LENGTH);

    assertNotNull(new ApplicationConstants());
  }
}
