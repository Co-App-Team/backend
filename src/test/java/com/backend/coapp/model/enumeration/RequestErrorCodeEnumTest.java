package com.backend.coapp.model.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class RequestErrorCodeEnumTest {
  @Test
  public void RequestErrorCodeEnum_expectContain() {
    RequestErrorCodeEnum requestHasNullOrEmptyField =
        RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD;
    assertNotNull(requestHasNullOrEmptyField);
  }

  @Test
  public void RequestErrorCodeEnum_expectNameShouldMatch() {
    RequestErrorCodeEnum requestHasNullOrEmptyField =
        RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD;
    assertEquals("REQUEST_HAS_NULL_OR_EMPTY_FIELD", requestHasNullOrEmptyField.name());
  }
}
