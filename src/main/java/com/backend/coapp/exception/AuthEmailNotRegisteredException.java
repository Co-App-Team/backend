package com.backend.coapp.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * This exception will be thrown when client tries to access information with unregistered email.
 */
public class AuthEmailNotRegisteredException extends UsernameNotFoundException {
  public AuthEmailNotRegisteredException() {
    super("Email is not yet registered.");
  }

  public AuthEmailNotRegisteredException(String message) {
    super("Email is not yet registered. " + message);
  }
}
