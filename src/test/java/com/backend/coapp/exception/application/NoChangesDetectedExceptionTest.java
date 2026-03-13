package com.backend.coapp.exception.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NoChangesDetectedExceptionTest {

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    String message = "No fields were updated.";
    NoChangesDetectedException exception = new NoChangesDetectedException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }
}
