package com.backend.coapp.exception.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ApplicationNotFoundException extends RuntimeException {
  public ApplicationNotFoundException() {
    super("Could not find application");
  }
}
