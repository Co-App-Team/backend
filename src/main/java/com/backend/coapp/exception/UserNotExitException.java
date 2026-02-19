package com.backend.coapp.exception;

/** This exception will be thrown when user doesn't exist in the database. */
public class UserNotExitException extends RuntimeException {
  public UserNotExitException() {
    super("User does NOT exist.");
  }
}
