package com.backend.coapp.exception;

/**
 * This exception will be thrown when a user tries to log in with invalid credential.
 */

public class AuthBadCredentialException extends RuntimeException {
  public AuthBadCredentialException() {
    super("Incorrect email or password");
  }
}
