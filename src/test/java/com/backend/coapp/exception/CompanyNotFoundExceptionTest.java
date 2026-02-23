package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CompanyNotFoundExceptionTest {

  @Test
  public void constructor_whenInitWithId_expectFormattedMessage() {
    String companyId = "comp-456";
    CompanyNotFoundException exception = new CompanyNotFoundException(companyId);

    assertNotNull(exception);
    assertEquals("Could not find company with ID: comp-456", exception.getMessage());
  }

  @Test
  public void constructor_whenNoArgs_expectDefaultMessage() {
    CompanyNotFoundException exception = new CompanyNotFoundException();

    assertNotNull(exception);
    assertEquals("Company with this companyId does not exist.", exception.getMessage());
  }
}
