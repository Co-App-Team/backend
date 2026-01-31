package com.backend.coapp.model.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SystemErrorCodeEnumTest {
  @Test
  public void SystemErrorCodeEnum_expectContain() {
    SystemErrorCodeEnum internalError = SystemErrorCodeEnum.INTERNAL_ERROR;
    assertNotNull(internalError);
  }

  @Test
  public void SystemErrorCodeEnum_expectNameShouldMatch() {
    SystemErrorCodeEnum internalError = SystemErrorCodeEnum.INTERNAL_ERROR;
    assertEquals("INTERNAL_ERROR", internalError.name());
  }
}
