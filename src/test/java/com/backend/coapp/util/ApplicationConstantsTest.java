package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ApplicationConstantsTest {

  @Test
  public void testConstants() {
    assertEquals(2000, ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH);
    assertEquals(2000, ApplicationConstants.MAX_JOB_NOTES_LENGTH);

    assertEquals(0, ApplicationConstants.APPLICATION_DEFAULT_PAGE);
    assertEquals(20, ApplicationConstants.APPLICATION_DEFAULT_SIZE);
    assertEquals(100, ApplicationConstants.APPLICATION_MAX_SIZE);

    assertEquals("dateApplied", ApplicationConstants.SORT_BY_DATE_APPLIED);
    assertEquals("desc", ApplicationConstants.DEFAULT_SORT_ORDER);

    assertNotNull(new ApplicationConstants());
  }
}
