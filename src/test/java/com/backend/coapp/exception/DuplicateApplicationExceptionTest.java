package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DuplicateApplicationExceptionTest {

  @Test
  public void constructor_whenInitParams_expectFormattedMessage() {
    String userId = "user1";
    String jobTitle = "Dev";
    String companyId = "comp1";

    DuplicateApplicationException exception =
        new DuplicateApplicationException(userId, jobTitle, companyId);

    assertNotNull(exception);
    String expected = "User user1 has already created an application for 'Dev' at company comp1";
    assertEquals(expected, exception.getMessage());
  }
}
