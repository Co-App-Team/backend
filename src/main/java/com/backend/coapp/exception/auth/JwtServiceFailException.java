package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when there is unknown JWT service */
public class JwtServiceFailException extends ApiException {
  public JwtServiceFailException(String message) {
    super("JWT Service failure." + message);
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
