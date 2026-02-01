package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** DTO request for new user account registration. */
@Getter
@AllArgsConstructor
public class UpdatePasswordRequest implements IRequest {
  // JSON request keys
  private String email;
  private Integer verifyCode;
  private String newPassword;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.email == null
        || this.email.isBlank()
        || this.newPassword == null
        || this.newPassword.isBlank()
        || this.verifyCode == null) {
      throw new InvalidRequestException(
          "Email, new password and verifyCode can NOT be null or empty");
    }
  }
}
