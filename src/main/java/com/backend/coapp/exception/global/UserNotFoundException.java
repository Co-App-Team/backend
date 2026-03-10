package com.backend.coapp.exception.global;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super(String.format("Could not find user"));
  }
}
