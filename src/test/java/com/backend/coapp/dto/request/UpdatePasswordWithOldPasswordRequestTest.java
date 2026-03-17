package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import org.junit.jupiter.api.Test;

class UpdatePasswordWithOldPasswordRequestTest {
  @Test
  void getterMethod_expectInitValues() {
    UpdatePasswordWithOldPasswordRequest request =
        new UpdatePasswordWithOldPasswordRequest("oldPassword", "newPassword");

    assertEquals("newPassword", request.getNewPassword());
    assertEquals("oldPassword", request.getOldPassword());
  }

  @Test
  void validateRequest_whenOldPassword_expectException() {
    UpdatePasswordWithOldPasswordRequest requestBlank =
        new UpdatePasswordWithOldPasswordRequest("", "newPassword");
    assertThrows(InvalidRequestException.class, requestBlank::validateRequest);

    UpdatePasswordWithOldPasswordRequest requestNull =
        new UpdatePasswordWithOldPasswordRequest(null, "newPassword");
    assertThrows(InvalidRequestException.class, requestNull::validateRequest);
  }

  @Test
  void validateRequest_whenNewPassword_expectException() {
    UpdatePasswordWithOldPasswordRequest requestBlank =
        new UpdatePasswordWithOldPasswordRequest("oldPassword", "");
    assertThrows(InvalidRequestException.class, requestBlank::validateRequest);

    UpdatePasswordWithOldPasswordRequest requestNull =
        new UpdatePasswordWithOldPasswordRequest("oldPassword", null);
    assertThrows(InvalidRequestException.class, requestNull::validateRequest);
  }
}
