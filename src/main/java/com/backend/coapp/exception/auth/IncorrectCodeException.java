package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import org.springframework.http.HttpStatus;

/**
 * This exception will be thrown when user provide incorrect verification code for verifying
 * account/updating password
 */
public class IncorrectCodeException extends ApiException {
  public IncorrectCodeException() {
    super("The code provided is incorrect. Please check your email again.");
  }

  public IncorrectCodeException(String message) {
    super("The code provided is incorrect. Please check your email again. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return AuthErrorCode.INVALID_CONFIRMATION_CODE;
  }
}
