package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import org.junit.jupiter.api.Test;

class ResetVerificationRequestTest {
  @Test
  void getterMethod_expectInitValues() {
    ResetVerificationRequest request = new ResetVerificationRequest("foo@mail.com");

    assertEquals("foo@mail.com", request.getEmail());
  }

  @Test
  void validateRequest_whenInvalidEmail_expectException() {
    ResetVerificationRequest requestBlank = new ResetVerificationRequest("");
    assertThrows(InvalidRequestException.class, requestBlank::validateRequest);

    ResetVerificationRequest requestNull = new ResetVerificationRequest(null);
    assertThrows(InvalidRequestException.class, requestNull::validateRequest);
  }

  @Test
  void validateRequest_whenValidEmail_expectNoException() {
    ResetVerificationRequest request = new ResetVerificationRequest("foo@mail.com");
    assertDoesNotThrow(request::validateRequest);
  }
}
