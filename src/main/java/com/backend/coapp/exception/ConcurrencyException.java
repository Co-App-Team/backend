package com.backend.coapp.exception;

public class ConcurrencyException extends RuntimeException {
  public ConcurrencyException(String message) {
    super(message);
  }
}
