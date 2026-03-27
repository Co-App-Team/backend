package com.backend.coapp.exception.application;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.ApplicationErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when the user tries to access the unauthorized resource. */
public class UnauthorizedApplicationAccessException extends ApiException {
  public UnauthorizedApplicationAccessException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.FORBIDDEN;
  }

  @Override
  public Object getErrorCode() {
    return ApplicationErrorCode.UNAUTHORIZED_APPLICATION_ACCESS;
  }
}
