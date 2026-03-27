package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import org.springframework.http.HttpStatus;

/**
 * This exception will be thrown if a client tries to verify an account that have been already
 * verified.
 */
public class AuthAccountAlreadyVerifyException extends ApiException {
  public AuthAccountAlreadyVerifyException() {
    super("Account has been verified.");
  }

  public AuthAccountAlreadyVerifyException(String message) {
    super("Account has been verified. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.METHOD_NOT_ALLOWED;
  }

  @Override
  public Object getErrorCode() {
    return AuthErrorCode.ACCOUNT_ALREADY_VERIFIED;
  }
}
