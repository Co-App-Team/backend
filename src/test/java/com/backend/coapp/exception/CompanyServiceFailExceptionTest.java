package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CompanyServiceFailExceptionTest {

  @Test
  public void constructor_whenInitMessage_expectWithMessage() {
    CompanyServiceFailException exception =
        new CompanyServiceFailException("Database connection failed");
    assertNotNull(exception);
    assertEquals("Database connection failed", exception.getMessage());
    assertNull(exception.getCause());
  }
}
