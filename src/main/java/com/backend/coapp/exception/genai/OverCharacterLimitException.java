package com.backend.coapp.exception.genai;

public class OverCharacterLimitException extends RuntimeException {
  public OverCharacterLimitException(String message) {
    super(message);
  }
}
