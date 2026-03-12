package com.backend.coapp.exception.auth;

public class UserServiceFailException extends RuntimeException {
  public UserServiceFailException(String message) {
    super("User service fail. " + message);
  }
}
