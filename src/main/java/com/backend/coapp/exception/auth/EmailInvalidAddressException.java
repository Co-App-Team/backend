package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown if the provided email is invalid. */
public class EmailInvalidAddressException extends ApiException {
  public EmailInvalidAddressException() {
    super("Invalid email or email not exit.");
  }

  public EmailInvalidAddressException(String message) {
    super("Invalid email or email not exit. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return AuthErrorCode.INVALID_EMAIL;
  }
}
