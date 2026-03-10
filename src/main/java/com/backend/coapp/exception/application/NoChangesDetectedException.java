package com.backend.coapp.exception.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class NoChangesDetectedException extends RuntimeException {
  public NoChangesDetectedException(String message) {
    super(message);
  }
}
