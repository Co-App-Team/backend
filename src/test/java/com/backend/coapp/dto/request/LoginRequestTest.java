package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backend.coapp.exception.global.InvalidRequestException;
import org.junit.jupiter.api.Test;

class LoginRequestTest {
  @Test
  void getMethod_expectInitValue() {
    LoginRequest request = new LoginRequest("foo@mail.com", "password");
    assertEquals("foo@mail.com", request.getEmail());
    assertEquals("password", request.getPassword());
  }

  @Test
  void validateRequest_whenValidRequest_expectNoException() {
    LoginRequest request = new LoginRequest("foo@mail.com", "password");
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenInvalidEmail_expectException() {
    LoginRequest requestEmailNull = new LoginRequest(null, "password");
    assertThrows(InvalidRequestException.class, requestEmailNull::validateRequest);

    LoginRequest requestEmailBlank = new LoginRequest("", "password");
    assertThrows(InvalidRequestException.class, requestEmailBlank::validateRequest);
  }

  @Test
  void validateRequest_whenInvalidPassword_expectException() {
    LoginRequest requestEmailNull = new LoginRequest("foo@mail.com", "");
    assertThrows(InvalidRequestException.class, requestEmailNull::validateRequest);

    LoginRequest requestEmailBlank = new LoginRequest("foo@mail.com", null);
    assertThrows(InvalidRequestException.class, requestEmailBlank::validateRequest);
  }
}
