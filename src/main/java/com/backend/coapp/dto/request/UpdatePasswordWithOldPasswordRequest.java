package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdatePasswordWithOldPasswordRequest implements IRequest {
  private String oldPassword;
  private String newPassword;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.oldPassword == null || this.oldPassword.isBlank()) {
      throw new InvalidRequestException("Old password can NOT be null or empty.");
    }

    if (this.newPassword == null || this.newPassword.isBlank()) {
      throw new InvalidRequestException("New password can NOT be null or empty.");
    }
  }
}
