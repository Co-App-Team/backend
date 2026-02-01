package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backend.coapp.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

public class UpdatePasswordRequestTest {
  @Test
  public void getMethod_expectInitValue() {
    UpdatePasswordRequest request = new UpdatePasswordRequest("foo@mail.com", 123, "newPassword");
    assertEquals("foo@mail.com", request.getEmail());
    assertEquals(123, request.getVerifyCode());
    assertEquals("newPassword", request.getNewPassword());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    UpdatePasswordRequest request = new UpdatePasswordRequest("foo@mail.com", 123, "newPassword");
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidEmail_expectException() {
    UpdatePasswordRequest requestEmailNull = new UpdatePasswordRequest(null, 123, "newPassword");
    assertThrows(InvalidRequestException.class, requestEmailNull::validateRequest);

    UpdatePasswordRequest requestEmailBlank = new UpdatePasswordRequest("", 123, "newPassword");
    assertThrows(InvalidRequestException.class, requestEmailBlank::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidNewPassword_expectException() {
    UpdatePasswordRequest requestEmailNull = new UpdatePasswordRequest("foo@mail.com", 123, "");
    assertThrows(InvalidRequestException.class, requestEmailNull::validateRequest);

    UpdatePasswordRequest requestEmailBlank = new UpdatePasswordRequest("foo@mail.com", 123, null);
    assertThrows(InvalidRequestException.class, requestEmailBlank::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidVerifyCode_expectException() {
    UpdatePasswordRequest requestVerificationNull =
        new UpdatePasswordRequest("foo@mail.com", null, "newPassword");
    assertThrows(InvalidRequestException.class, requestVerificationNull::validateRequest);
  }
}
