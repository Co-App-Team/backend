package com.backend.coapp.exception.application;

public class NoChangesDetectedException extends RuntimeException {
  public NoChangesDetectedException(String message) {
    super(message);
  }
}
