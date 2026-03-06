package com.backend.coapp.exception;

public class OverCharacterLimitException extends RuntimeException {
  public OverCharacterLimitException(String message) {
    super(message);
  }
}
