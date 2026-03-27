package com.backend.coapp.exception.application;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.ApplicationErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when no changed detected in updated application */
public class NoChangesDetectedException extends ApiException {
  public NoChangesDetectedException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return ApplicationErrorCode.NO_CHANGE_DETECTED_TO_UPDATE;
  }
}
