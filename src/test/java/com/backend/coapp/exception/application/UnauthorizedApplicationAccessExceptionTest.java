package com.backend.coapp.exception.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UnauthorizedApplicationAccessExceptionTest {

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    String message = "User does not own this application.";
    UnauthorizedApplicationAccessException exception =
        new UnauthorizedApplicationAccessException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }
}
