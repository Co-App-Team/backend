package com.backend.coapp.exception;

/**
 * This exception will be thrown when client tries to access information with unregistered email.
 */
public class AuthEmailNotRegisteredException extends RuntimeException {
  public AuthEmailNotRegisteredException() {
    super("Email is not yet registered.");
  }

  public AuthEmailNotRegisteredException(String message) {
    super("Email is not yet registered. " + message);
  }
}
