package com.backend.coapp.exception.review;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import org.springframework.http.HttpStatus;

/** thrown when review service operations fail */
public class ReviewServiceFailException extends ApiException {

  public ReviewServiceFailException(String message) {
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
