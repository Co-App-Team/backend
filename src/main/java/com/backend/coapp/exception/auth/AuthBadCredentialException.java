package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when a user tries to log in with invalid credential. */
public class AuthBadCredentialException extends ApiException {
  public AuthBadCredentialException() {
    super("Incorrect email or password");
  }

  public AuthBadCredentialException(String message) {
    super("Bad credential. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }

  @Override
  public Object getErrorCode() {
    return AuthErrorCode.INVALID_EMAIL_OR_PASSWORD;
  }
}
