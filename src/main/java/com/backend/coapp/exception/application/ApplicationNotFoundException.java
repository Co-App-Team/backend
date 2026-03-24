package com.backend.coapp.exception.application;

public class ApplicationNotFoundException extends RuntimeException {
  public ApplicationNotFoundException() {
    super("Could not find application");
  }
}
