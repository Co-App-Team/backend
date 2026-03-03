package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ApplicationServiceFailExceptionTest {

  @Test
  public void constructor_whenInitMessage_expectWithMessage() {
    String message = "Service layer failure";
    ApplicationServiceFailException exception = new ApplicationServiceFailException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }
}
