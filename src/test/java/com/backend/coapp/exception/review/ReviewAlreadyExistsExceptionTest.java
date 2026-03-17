package com.backend.coapp.exception.review;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReviewAlreadyExistsExceptionTest {

  @Test
  void constructor_whenInitWithId_expectMessageAndId() {
    ReviewAlreadyExistsException exception = new ReviewAlreadyExistsException();
    assertNotNull(exception);
    assertEquals("You have already submitted a review for this company.", exception.getMessage());
    assertNull(exception.getCause());
  }
}
