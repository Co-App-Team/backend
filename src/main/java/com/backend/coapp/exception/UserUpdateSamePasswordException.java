package com.backend.coapp.exception;

/** This exception will be thrown when user update password with old password. */
public class UserUpdateSamePasswordException extends RuntimeException {
  public UserUpdateSamePasswordException() {
    super("New password must be different from old password");
  }
}
