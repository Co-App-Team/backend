package com.backend.coapp.exception.company;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CompanyAlreadyExistsExceptionTest {

  @Test
  void constructor_whenInitWithId_expectMessageAndId() {
    CompanyAlreadyExistsException exception = new CompanyAlreadyExistsException("123");
    assertNotNull(exception);
    assertEquals("A company with this name already exists.", exception.getMessage());
    assertEquals("123", exception.getExistingCompanyId());
    assertNull(exception.getCause());
  }
}
