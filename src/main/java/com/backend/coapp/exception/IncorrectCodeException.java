package com.backend.coapp.exception;

public class IncorrectCodeException extends RuntimeException {
  public IncorrectCodeException() {
    super("The code provided is incorrect. Please check your email again.");
  }

  public IncorrectCodeException(String message) {
    super("The code provided is incorrect. Please check your email again. " + message);
  }
}
