package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import org.springframework.http.HttpStatus;

/**
 * This exception will be thrown if client tries to request service with an account that not yet
 * activate
 */
public class AuthAccountNotYetActivatedException extends ApiException {
  public AuthAccountNotYetActivatedException() {
    super(
        "Account has not been activated yet. Please use verification code to activate the account.");
  }

  public AuthAccountNotYetActivatedException(String message) {
    super(
        "Account has not been activated yet. Please use verification code to activate the account. "
            + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }

  @Override
  public Object getErrorCode() {
    return AuthErrorCode.ACCOUNT_NOT_ACTIVATED;
  }
}
