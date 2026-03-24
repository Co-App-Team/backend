package com.backend.coapp.exception.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DuplicateApplicationExceptionTest {

  @Test
  void constructor_whenInitParams_expectFormattedMessage() {
    String jobTitle = "Dev";
    String companyId = "comp1";

    DuplicateApplicationException exception =
        new DuplicateApplicationException(jobTitle, companyId);

    assertNotNull(exception);
    String expected = "User has already created an application for 'Dev' at company comp1";
    assertEquals(expected, exception.getMessage());
  }
}
