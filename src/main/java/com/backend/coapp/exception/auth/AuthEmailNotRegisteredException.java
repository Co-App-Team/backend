package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import org.springframework.http.HttpStatus;

/**
 * This exception will be thrown when client tries to access information with unregistered email.
 */
public class AuthEmailNotRegisteredException extends ApiException {
  public AuthEmailNotRegisteredException() {
    super("Email is not yet registered.");
  }

  public AuthEmailNotRegisteredException(String message) {
    super("Email is not yet registered. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return AuthErrorCode.EMAIL_NOT_REGISTERED;
  }
}
