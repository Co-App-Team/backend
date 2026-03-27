package com.backend.coapp.exception.application;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.ApplicationErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when application not found. */
public class ApplicationNotFoundException extends ApiException {
  public ApplicationNotFoundException() {
    super("Could not find application");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public Object getErrorCode() {
    return ApplicationErrorCode.APPLICATION_NOT_FOUND;
  }
}
