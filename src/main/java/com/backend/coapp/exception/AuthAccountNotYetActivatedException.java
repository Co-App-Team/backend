package com.backend.coapp.exception;

/**
 * This exception will be thrown if client tries to request service with an account that not yet
 * activate
 */
public class AuthAccountNotYetActivatedException extends RuntimeException {
  public AuthAccountNotYetActivatedException() {
    super(
        "Account has not been activated yet. Please use verification code to activate the account.");
  }

  public AuthAccountNotYetActivatedException(String message) {
    super(
        "Account has not been activated yet. Please use verification code to activate the account. "
            + message);
  }
}
