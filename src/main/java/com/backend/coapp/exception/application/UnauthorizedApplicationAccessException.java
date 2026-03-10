package com.backend.coapp.exception.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UnauthorizedApplicationAccessException extends RuntimeException {
  public UnauthorizedApplicationAccessException(String message) {
    super(message);
  }
}
