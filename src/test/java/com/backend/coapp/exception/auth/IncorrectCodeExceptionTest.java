package com.backend.coapp.exception.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class IncorrectCodeExceptionTest {
  @Test
  void constructor_whenInitNoArgs_expectDefaultMessage() {
    IncorrectCodeException exception = new IncorrectCodeException();

    assertNotNull(exception);
    assertEquals(
        "The code provided is incorrect. Please check your email again.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    IncorrectCodeException exception = new IncorrectCodeException("foo");

    assertNotNull(exception);
    assertEquals(
        "The code provided is incorrect. Please check your email again. foo",
        exception.getMessage());
    assertNull(exception.getCause());
  }
}
