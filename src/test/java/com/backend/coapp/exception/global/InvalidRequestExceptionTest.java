package com.backend.coapp.exception.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class InvalidRequestExceptionTest {
  @Test
  void constructor_whenInitNoArgs_expectDefaultMessage() {
    InvalidRequestException exception = new InvalidRequestException();

    assertNotNull(exception);
    assertEquals("Invalid inputs of the request.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    InvalidRequestException exception = new InvalidRequestException("foo");

    assertNotNull(exception);
    assertEquals("Invalid inputs of the request. foo", exception.getMessage());
    assertNull(exception.getCause());
  }
}
