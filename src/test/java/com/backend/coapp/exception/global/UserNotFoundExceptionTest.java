package com.backend.coapp.exception.global;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserNotFoundExceptionTest {

  @Test
  void constructor_whenNoArgConstructor_expectDefaultMessage() {
    UserNotFoundException exception = new UserNotFoundException();

    assertNotNull(exception);
    assertEquals("Could not find user", exception.getMessage());
  }
}
