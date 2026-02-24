package com.backend.coapp.exception;

/** This exception will be thrown when user make more than 2 requests at the same time */
public class ConcurrencyException extends RuntimeException {
  public ConcurrencyException(String message) {
    super(message);
  }
}
