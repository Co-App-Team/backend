package com.backend.coapp.exception.auth;

/** This exception will be thrown when user doesn't exist in the database. */
public class UserNotExistException extends RuntimeException {
  public UserNotExistException() {
    super("User does NOT exist.");
  }
}
