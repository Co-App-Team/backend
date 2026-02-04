package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ReviewConstantsTest {

  @Test
  public void constants_expectCorrectValues() {
    assertEquals(1, ReviewConstants.MIN_RATING);
    assertEquals(5, ReviewConstants.MAX_RATING);
    assertEquals(2000, ReviewConstants.MAX_COMMENT_LENGTH);
  }
}
