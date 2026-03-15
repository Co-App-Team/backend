package com.backend.coapp.exception.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AuthAccountNotYetActivatedExceptionTest {
  @Test
  void constructor_whenInitNoArgs_expectDefaultMessage() {
    AuthAccountNotYetActivatedException exception = new AuthAccountNotYetActivatedException();

    assertNotNull(exception);
    assertEquals(
        "Account has not been activated yet. Please use verification code to activate the account.",
        exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    AuthAccountNotYetActivatedException exception = new AuthAccountNotYetActivatedException("foo");

    assertNotNull(exception);
    assertEquals(
        "Account has not been activated yet. Please use verification code to activate the account. foo",
        exception.getMessage());
    assertNull(exception.getCause());
  }
}
