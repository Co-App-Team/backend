package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

public class VerifyEmailRequestTest {
  @Test
  public void getMethod_expectInitValue() {
    VerifyEmailRequest request = new VerifyEmailRequest("foo@mail.com", 123);
    assertEquals("foo@mail.com", request.getEmail());
    assertEquals(123, request.getVerifyCode());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    VerifyEmailRequest request = new VerifyEmailRequest("foo@mail.com", 123);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidEmail_expectException() {
    VerifyEmailRequest requestEmailNull = new VerifyEmailRequest(null, 123);
    assertThrows(InvalidRequestException.class, requestEmailNull::validateRequest);

    VerifyEmailRequest requestEmailBlank = new VerifyEmailRequest("", 123);
    assertThrows(InvalidRequestException.class, requestEmailBlank::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidVerifyCode_expectException() {
    VerifyEmailRequest requestVerificationNull = new VerifyEmailRequest("foo@mail.com", null);
    assertThrows(InvalidRequestException.class, requestVerificationNull::validateRequest);
  }
}
