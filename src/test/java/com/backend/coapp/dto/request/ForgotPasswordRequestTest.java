package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import org.junit.jupiter.api.Test;

public class ForgotPasswordRequestTest {
  @Test
  public void getterMethod_expectInitValues() {
    ForgotPasswordRequest request = new ForgotPasswordRequest("foo@mail.com");

    assertEquals("foo@mail.com", request.getEmail());
  }

  @Test
  public void validateRequest_whenInvalidEmail_expectException() {
    ForgotPasswordRequest requestBlank = new ForgotPasswordRequest("");
    assertThrows(InvalidRequestException.class, requestBlank::validateRequest);

    ForgotPasswordRequest requestNull = new ForgotPasswordRequest(null);
    assertThrows(InvalidRequestException.class, requestNull::validateRequest);
  }

  @Test
  public void validateRequest_whenValidEmail_expectNoException() {
    ForgotPasswordRequest request = new ForgotPasswordRequest("foo@mail.com");
    assertDoesNotThrow(request::validateRequest);
  }
}
