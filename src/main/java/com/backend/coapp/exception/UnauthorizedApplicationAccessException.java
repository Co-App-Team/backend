package com.backend.coapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedApplicationAccessException extends RuntimeException {
  public UnauthorizedApplicationAccessException(String message) {
    super(message);
  }
}
