package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ApplicationNotFoundExceptionTest {

  @Test
  public void constructor_whenInitWithId_expectFormattedMessage() {
    String applicationId = "app-123";
    ApplicationNotFoundException exception = new ApplicationNotFoundException(applicationId);

    assertNotNull(exception);
    assertEquals("Could not find application with ID: app-123", exception.getMessage());
  }
}
