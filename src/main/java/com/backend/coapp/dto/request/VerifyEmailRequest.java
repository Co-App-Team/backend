package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** DTO request for verify email and activate new account. */
@Getter
@AllArgsConstructor
public class VerifyEmailRequest implements IRequest {
  // JSON request keys
  private String email;
  private Integer verifyCode;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.email == null || this.email.isBlank() || this.verifyCode == null) {
      throw new InvalidRequestException("Email and verification code can NOT be null or empty.");
    }
  }
}
