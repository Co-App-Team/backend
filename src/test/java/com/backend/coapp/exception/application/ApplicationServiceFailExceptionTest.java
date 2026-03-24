package com.backend.coapp.exception.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApplicationServiceFailExceptionTest {

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    String message = "Service layer failure";
    ApplicationServiceFailException exception = new ApplicationServiceFailException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }
}
