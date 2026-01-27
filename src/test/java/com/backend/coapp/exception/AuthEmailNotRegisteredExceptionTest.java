package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class AuthEmailNotRegisteredExceptionTest {
  @Test
  public void constructor_whenInitNoArgs_expectDefaultMessage() {
    AuthEmailNotRegisteredException exception = new AuthEmailNotRegisteredException();

    assertNotNull(exception);
    assertEquals("Email is not yet registered.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  public void constructor_whenInitMessage_expectWithMessage() {
    AuthEmailNotRegisteredException exception = new AuthEmailNotRegisteredException("foo");

    assertNotNull(exception);
    assertEquals("Email is not yet registered. foo", exception.getMessage());
    assertNull(exception.getCause());
  }
}
