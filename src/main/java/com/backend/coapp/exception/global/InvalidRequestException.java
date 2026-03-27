package com.backend.coapp.exception.global;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.RequestErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when the request containing invalid input(s). */
public class InvalidRequestException extends ApiException {

  public InvalidRequestException() {
    super("Invalid inputs of the request.");
  }

  public InvalidRequestException(String message) {
    super("Invalid inputs of the request. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return RequestErrorCode.REQUEST_HAS_NULL_OR_EMPTY_FIELD;
  }
}
