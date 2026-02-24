package com.backend.coapp.exception;

public class UserServiceFailException extends RuntimeException {
  public UserServiceFailException(String message) {
    super("User service fail. " + message);
  }
}
