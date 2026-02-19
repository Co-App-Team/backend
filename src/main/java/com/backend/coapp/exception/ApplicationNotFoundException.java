package com.backend.coapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ApplicationNotFoundException extends RuntimeException {
  public ApplicationNotFoundException(String applicationId) {
    super(String.format("Could not find application with ID: %s", applicationId));
  }
}
