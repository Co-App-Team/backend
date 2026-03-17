package com.backend.coapp.exception.auth;

/** This exception will be thrown when user update password with old password. */
public class UserInvalidPasswordChangeException extends RuntimeException {
  public UserInvalidPasswordChangeException() {
    super("New password must be different from old password");
  }
}
