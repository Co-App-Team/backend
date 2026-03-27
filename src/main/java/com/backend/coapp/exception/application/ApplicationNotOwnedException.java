package com.backend.coapp.exception.application;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.ApplicationErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown will the application is not belong to a user. */
public class ApplicationNotOwnedException extends ApiException {
  public ApplicationNotOwnedException() {
    super("Application is not owned by user. No operation is allowed.");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.FORBIDDEN;
  }

  @Override
  public Object getErrorCode() {
    return ApplicationErrorCode.APPLICATION_NOT_OWN;
  }
}
