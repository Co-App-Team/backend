package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown if there is a failure related to JavaMailSender service. */
public class EmailServiceException extends ApiException {

  public EmailServiceException() {
    super("JavaMailSender Service Failure.");
  }

  public EmailServiceException(String message) {
    super("JavaMailSender Service Failure. " + message);
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
