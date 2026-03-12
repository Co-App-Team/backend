package com.backend.coapp.exception.review;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ReviewNotFoundExceptionTest {

  @Test
  public void constructor_whenInitNoArgs_expectDefaultMessage() {
    ReviewNotFoundException exception = new ReviewNotFoundException();
    assertNotNull(exception);
    assertEquals(
        "Review with the provided id does not exist for this company.", exception.getMessage());
    assertNull(exception.getCause());
  }
}
