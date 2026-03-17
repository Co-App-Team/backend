package com.backend.coapp.exception.auth;

/** This exception will be thrown when JWT is invalid. */
public class JwtInvalidTokenException extends RuntimeException {
  public JwtInvalidTokenException() {
    super("Invalid token. Please log in again to obtain new token.");
  }
}
