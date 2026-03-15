package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PaginationConstantsTest {

  @Test
  void constants_expectCorrectIntegerValues() {
    assertEquals(0, PaginationConstants.COMPANY_DEFAULT_PAGE);
    assertEquals(20, PaginationConstants.COMPANY_DEFAULT_SIZE);
    assertEquals(100, PaginationConstants.COMPANY_MAX_SIZE);
    assertEquals(1, PaginationConstants.COMPANY_MIN_SIZE);
  }

  @Test
  void constants_expectCorrectStringValues() {
    assertEquals("0", PaginationConstants.COMPANY_DEFAULT_PAGE_STR);
    assertEquals("20", PaginationConstants.COMPANY_DEFAULT_SIZE_STR);
    assertEquals("false", PaginationConstants.DEFAULT_USE_PAGINATION_STR);
  }
}
