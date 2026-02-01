package com.backend.coapp.exception;

/**
 * This exception will be thrown if a client tries to verify an account that have been already
 * verified.
 */
public class AuthAccountAlreadyVerifyException extends RuntimeException {
  public AuthAccountAlreadyVerifyException() {
    super("Account has been verified.");
  }

  public AuthAccountAlreadyVerifyException(String message) {
    super("Account has been verified. " + message);
  }
}
