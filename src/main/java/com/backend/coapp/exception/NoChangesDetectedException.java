package com.backend.coapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoChangesDetectedException extends RuntimeException {
  public NoChangesDetectedException(String message) {
    super(message);
  }
}
