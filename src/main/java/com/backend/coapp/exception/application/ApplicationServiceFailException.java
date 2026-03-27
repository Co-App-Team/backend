package com.backend.coapp.exception.application;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when application service fail (internal) */
public class ApplicationServiceFailException extends ApiException {
  public ApplicationServiceFailException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public Object getErrorCode() {
    return SystemErrorCode.INTERNAL_ERROR;
  }
}
