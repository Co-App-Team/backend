package com.backend.coapp.exception.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ApplicationServiceFailException extends RuntimeException {
  public ApplicationServiceFailException(String message) {
    super(message);
  }
}
