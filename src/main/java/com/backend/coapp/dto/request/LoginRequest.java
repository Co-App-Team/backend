package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import lombok.Getter;

/** DTO Request for log in. */
@Getter
public class LoginRequest implements IRequest {
  private String email;
  private String password;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.email == null || this.email.isBlank()) {
      throw new InvalidRequestException("Email can NOT be null or empty.");
    }

    if (this.password == null || this.password.isBlank()) {
      throw new InvalidRequestException("Password can NOT be null or empty.");
    }
  }
}
