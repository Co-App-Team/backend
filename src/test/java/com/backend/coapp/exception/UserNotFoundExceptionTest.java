package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserNotFoundExceptionTest {

  @Test
  public void constructor_whenInitWithId_expectFormattedMessage() {
    String userId = "user-999";
    UserNotFoundException exception = new UserNotFoundException(userId);

    assertNotNull(exception);
    assertEquals("Could not find user with ID: user-999", exception.getMessage());
  }
}
