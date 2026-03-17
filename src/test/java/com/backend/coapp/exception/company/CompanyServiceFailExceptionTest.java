package com.backend.coapp.exception.company;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CompanyServiceFailExceptionTest {

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    CompanyServiceFailException exception =
        new CompanyServiceFailException("Database connection failed");
    assertNotNull(exception);
    assertEquals("Database connection failed", exception.getMessage());
    assertNull(exception.getCause());
  }
}
