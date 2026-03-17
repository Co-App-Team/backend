package com.backend.coapp.exception.company;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InvalidWebsiteExceptionTest {

  @Test
  void constructor_whenInitNoArgs_expectDefaultMessage() {
    InvalidWebsiteException exception = new InvalidWebsiteException();
    assertNotNull(exception);
    assertEquals("The website URL is not valid.", exception.getMessage());
    assertNull(exception.getCause());
  }
}
