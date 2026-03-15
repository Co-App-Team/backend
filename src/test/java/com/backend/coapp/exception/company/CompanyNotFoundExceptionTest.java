package com.backend.coapp.exception.company;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CompanyNotFoundExceptionTest {

  @Test
  void constructor_whenNoArgs_expectDefaultMessage() {
    CompanyNotFoundException exception = new CompanyNotFoundException();

    assertNotNull(exception);
    assertEquals("Company with this companyId does not exist.", exception.getMessage());
  }
}
