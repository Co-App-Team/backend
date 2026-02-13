package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ReviewServiceFailExceptionTest {

  @Test
  public void constructor_whenInitMessage_expectWithMessage() {
    ReviewServiceFailException exception =
        new ReviewServiceFailException("Database connection failed");
    assertNotNull(exception);
    assertEquals("Database connection failed", exception.getMessage());
    assertNull(exception.getCause());
  }
}
