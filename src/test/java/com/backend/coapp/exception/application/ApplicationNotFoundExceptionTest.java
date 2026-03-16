package com.backend.coapp.exception.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApplicationNotFoundExceptionTest {

  @Test
  void noArgsConstructor_expectDefaultMessage() {
    ApplicationNotFoundException exception = new ApplicationNotFoundException();

    assertNotNull(exception);
    assertEquals("Could not find application", exception.getMessage());
  }
}
