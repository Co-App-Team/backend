package com.backend.coapp.exception;

/** This exception will be thrown when JWT token is expired. */
public class JwtExpiredException extends RuntimeException {
  public JwtExpiredException() {
    super("Token is expired. Please log in again.");
  }
}
