package com.backend.coapp.exception.application;

public class UnauthorizedApplicationAccessException extends RuntimeException {
  public UnauthorizedApplicationAccessException(String message) {
    super(message);
  }
}
