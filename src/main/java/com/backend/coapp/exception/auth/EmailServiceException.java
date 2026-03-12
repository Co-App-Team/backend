package com.backend.coapp.exception.auth;

/** This exception will be thrown if there is a failure related to JavaMailSender service. */
public class EmailServiceException extends RuntimeException {

  public EmailServiceException() {
    super("JavaMailSender Service Failure.");
  }

  public EmailServiceException(String message) {
    super("JavaMailSender Service Failure. " + message);
  }
}
