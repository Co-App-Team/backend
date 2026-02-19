package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class NoChangesDetectedExceptionTest {

  @Test
  public void constructor_whenInitMessage_expectWithMessage() {
    String message = "No fields were updated.";
    NoChangesDetectedException exception = new NoChangesDetectedException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }
}
