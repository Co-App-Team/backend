package com.backend.coapp.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CompanyNotFoundExceptionTest {

  @Test
  public void constructor_whenInitNoArgs_expectDefaultMessage() {
    CompanyNotFoundException exception = new CompanyNotFoundException();
    assertNotNull(exception);
    assertEquals("Company with this companyId does not exist.", exception.getMessage());
    assertNull(exception.getCause());
  }
}
