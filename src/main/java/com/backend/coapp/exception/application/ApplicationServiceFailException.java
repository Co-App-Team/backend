package com.backend.coapp.exception.application;

public class ApplicationServiceFailException extends RuntimeException {
  public ApplicationServiceFailException(String message) {
    super(message);
  }
}
