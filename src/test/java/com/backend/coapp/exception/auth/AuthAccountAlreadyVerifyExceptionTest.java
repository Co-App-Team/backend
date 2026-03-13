package com.backend.coapp.exception.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AuthAccountAlreadyVerifyExceptionTest {
  @Test
  void constructor_whenInitNoArgs_expectDefaultMessage() {
    AuthAccountAlreadyVerifyException exception = new AuthAccountAlreadyVerifyException();

    assertNotNull(exception);
    assertEquals("Account has been verified.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    AuthAccountAlreadyVerifyException exception = new AuthAccountAlreadyVerifyException("foo");

    assertNotNull(exception);
    assertEquals("Account has been verified. foo", exception.getMessage());
    assertNull(exception.getCause());
  }
}
