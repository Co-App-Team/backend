package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

public class InvalidQueryParameterExceptionTest {

  @Test
  public void constructor_whenInitParams_expectCorrectMessage() {
    InvalidQueryParameterException exception =
        new InvalidQueryParameterException(
            "status", "INVALID_STATUS", List.of("APPLIED", "REJECTED"));

    assertNotNull(exception);
    assertEquals("Invalid query parameter", exception.getMessage());
  }

  @Test
  public void constructor_whenInitParams_expectCorrectParameter() {
    InvalidQueryParameterException exception =
        new InvalidQueryParameterException(
            "status", "INVALID_STATUS", List.of("APPLIED", "REJECTED"));

    assertEquals("status", exception.getParameter());
  }

  @Test
  public void constructor_whenInitParams_expectCorrectInvalidValue() {
    InvalidQueryParameterException exception =
        new InvalidQueryParameterException(
            "status", "INVALID_STATUS", List.of("APPLIED", "REJECTED"));

    assertEquals("INVALID_STATUS", exception.getInvalidValue());
  }

  @Test
  public void constructor_whenInitParams_expectCorrectValidValues() {
    List<String> validValues = List.of("APPLIED", "REJECTED");

    InvalidQueryParameterException exception =
        new InvalidQueryParameterException("status", "INVALID_STATUS", validValues);

    assertEquals(validValues, exception.getValidValues());
  }
}
