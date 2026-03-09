package com.backend.coapp.exception.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.backend.coapp.exception.genai.GenAIUsageManagementServiceException;
import org.junit.jupiter.api.Test;

public class GenAIUsageManagementServiceExceptionTest {
  @Test
  public void constructor_whenInitNoArgs_expectDefaultMessage() {
    GenAIUsageManagementServiceException exception = new GenAIUsageManagementServiceException();

    assertNotNull(exception);
    assertEquals("GenAI usage management failure.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  public void constructor_whenInitMessage_expectWithMessage() {
    GenAIUsageManagementServiceException exception =
        new GenAIUsageManagementServiceException("foo");

    assertNotNull(exception);
    assertEquals("GenAI usage management failure. foo", exception.getMessage());
    assertNull(exception.getCause());
  }
}
