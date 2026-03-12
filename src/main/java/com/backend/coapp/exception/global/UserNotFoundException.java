package com.backend.coapp.exception.global;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super(String.format("Could not find user"));
  }
}
