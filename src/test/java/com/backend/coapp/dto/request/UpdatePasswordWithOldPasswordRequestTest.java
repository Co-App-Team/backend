package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

public class UpdatePasswordWithOldPasswordRequestTest {
  @Test
  public void getterMethod_expectInitValues() {
    UpdatePasswordWithOldPasswordRequest request =
        new UpdatePasswordWithOldPasswordRequest("oldPassword", "newPassword");

    assertEquals("newPassword", request.getNewPassword());
    assertEquals("oldPassword", request.getOldPassword());
  }

  @Test
  public void validateRequest_whenOldPassword_expectException() {
    UpdatePasswordWithOldPasswordRequest requestBlank =
        new UpdatePasswordWithOldPasswordRequest("", "newPassword");
    assertThrows(InvalidRequestException.class, requestBlank::validateRequest);

    UpdatePasswordWithOldPasswordRequest requestNull =
        new UpdatePasswordWithOldPasswordRequest(null, "newPassword");
    assertThrows(InvalidRequestException.class, requestNull::validateRequest);
  }

  @Test
  public void validateRequest_whenNewPassword_expectException() {
    UpdatePasswordWithOldPasswordRequest requestBlank =
        new UpdatePasswordWithOldPasswordRequest("oldPassword", "");
    assertThrows(InvalidRequestException.class, requestBlank::validateRequest);

    UpdatePasswordWithOldPasswordRequest requestNull =
        new UpdatePasswordWithOldPasswordRequest("oldPassword", null);
    assertThrows(InvalidRequestException.class, requestNull::validateRequest);
  }
}
