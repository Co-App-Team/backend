package com.backend.coapp.exception.genai;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.GenAIErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when user make more than 2 requests at the same time */
public class ConcurrencyException extends ApiException {
  public ConcurrencyException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public Object getErrorCode() {
    return GenAIErrorCode.OTHER_REQUEST_IN_PROGRESS;
  }
}
