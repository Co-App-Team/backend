package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import org.springframework.http.HttpStatus;

/**
 * This exception will be thrown if client tries to register a new account with an email that has
 * been used.
 */
public class AuthEmailAlreadyUsedException extends ApiException {
  public AuthEmailAlreadyUsedException() {
    super("An account with that email already exists.");
  }

  public AuthEmailAlreadyUsedException(String message) {
    super("An account with that email already exists. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public Object getErrorCode() {
    return AuthErrorCode.EXIST_ACCOUNT_WITH_EMAIL;
  }
}
