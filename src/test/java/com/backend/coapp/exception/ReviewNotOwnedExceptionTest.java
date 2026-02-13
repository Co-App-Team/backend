package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ReviewNotOwnedExceptionTest {

  @Test
  public void constructor_whenInitWithAction_expectMessageWithAction() {
    ReviewNotOwnedException exception = new ReviewNotOwnedException("update");
    assertNotNull(exception);
    assertEquals("You can only update your own reviews.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  public void constructor_whenInitWithDeleteAction_expectDeleteMessage() {
    ReviewNotOwnedException exception = new ReviewNotOwnedException("delete");
    assertNotNull(exception);
    assertEquals("You can only delete your own reviews.", exception.getMessage());
    assertNull(exception.getCause());
  }
}
