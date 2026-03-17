package com.backend.coapp.exception.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthEmailAlreadyUsedExceptionTest {

  @Test
  void constructor_whenInitNoArgs_expectDefaultMessage() {
    AuthEmailAlreadyUsedException exception = new AuthEmailAlreadyUsedException();

    assertNotNull(exception);
    assertEquals("An account with that email already exists.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    AuthEmailAlreadyUsedException exception = new AuthEmailAlreadyUsedException("foo");

    assertNotNull(exception);
    assertEquals("An account with that email already exists. foo", exception.getMessage());
    assertNull(exception.getCause());
  }
}
