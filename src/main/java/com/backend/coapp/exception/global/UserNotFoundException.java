package com.backend.coapp.exception.global;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super("Could not find user");
  }
}
