package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ApplicationNotFoundExceptionTest {

  @Test
  public void noArgsConstructor_expectDefaultMessage() {
    ApplicationNotFoundException exception = new ApplicationNotFoundException();

    assertNotNull(exception);
    assertEquals("Could not find application", exception.getMessage());
  }
}
