package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ReviewAlreadyExistsExceptionTest {

  @Test
  public void constructor_whenInitWithId_expectMessageAndId() {
    ReviewAlreadyExistsException exception = new ReviewAlreadyExistsException("review123");
    assertNotNull(exception);
    assertEquals("You have already submitted a review for this company.", exception.getMessage());
    assertEquals("review123", exception.getExistingReviewId());
    assertNull(exception.getCause());
  }
}
