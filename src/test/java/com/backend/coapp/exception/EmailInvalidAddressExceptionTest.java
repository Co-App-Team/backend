package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class EmailInvalidAddressExceptionTest {
  @Test
  public void constructor_whenInitNoArgs_expectDefaultMessage() {
    EmailInvalidAddressException exception = new EmailInvalidAddressException();

    assertNotNull(exception);
    assertEquals("Invalid email or email not exit.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  public void constructor_whenInitMessage_expectWithMessage() {
    EmailInvalidAddressException exception = new EmailInvalidAddressException("foo");

    assertNotNull(exception);
    assertEquals("Invalid email or email not exit. foo", exception.getMessage());
    assertNull(exception.getCause());
  }
}
