package com.backend.coapp.exception;

/** This exception will be thrown when there is unknown JWT service */
public class JwtServiceFailException extends RuntimeException {
  public JwtServiceFailException(String message) {
    super("JWT Service failure." + message);
  }
}
