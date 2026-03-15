package com.backend.coapp.exception.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class EmailServiceExceptionTest {
  @Test
  void constructor_whenInitNoArgs_expectDefaultMessage() {
    EmailServiceException exception = new EmailServiceException();

    assertNotNull(exception);
    assertEquals("JavaMailSender Service Failure.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void constructor_whenInitMessage_expectWithMessage() {
    EmailServiceException exception = new EmailServiceException("foo");

    assertNotNull(exception);
    assertEquals("JavaMailSender Service Failure. foo", exception.getMessage());
    assertNull(exception.getCause());
  }
}
