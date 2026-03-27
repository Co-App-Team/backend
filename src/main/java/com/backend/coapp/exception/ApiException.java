package com.backend.coapp.exception;

import org.springframework.http.HttpStatus;

/** Interface for exception from API call */
public abstract class ApiException extends RuntimeException {
  /**
   * getStatus
   *
   * @return HTTPS Status
   */
  public abstract HttpStatus getStatus();

  /**
   * getErrorCode
   *
   * @return custom error code
   */
  public abstract Object getErrorCode();

  protected ApiException(String message) {
    super(message);
  }
}
