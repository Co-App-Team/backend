package com.backend.coapp.exception.auth;

/**
 * This exception will be thrown when user provide incorrect verification code for verifying
 * account/updating password
 */
public class IncorrectCodeException extends RuntimeException {
  public IncorrectCodeException() {
    super("The code provided is incorrect. Please check your email again.");
  }

  public IncorrectCodeException(String message) {
    super("The code provided is incorrect. Please check your email again. " + message);
  }
}
