package com.backend.coapp.exception.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AuthEmailNotRegisteredExceptionTest {
  @Test
  void constructor_whenInitNoArgs_expectDefaultMessage() {
    AuthEmailNotRegisteredException exception = new AuthEmailNotRegisteredException();

    assertNotNull(exception);
    assertEquals("Email is not yet registered.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    AuthEmailNotRegisteredException exception = new AuthEmailNotRegisteredException("foo");

    assertNotNull(exception);
    assertEquals("Email is not yet registered. foo", exception.getMessage());
    assertNull(exception.getCause());
  }
}
