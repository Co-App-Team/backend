package com.backend.coapp.exception.application;

public class ApplicationNotOwnedException extends RuntimeException {
  public ApplicationNotOwnedException() {
    super("Application is not owned by user. No operation is allowed.");
  }
}
