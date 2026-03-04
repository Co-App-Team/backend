package com.backend.coapp.exception;

/** This exception will be thrown when the request containing invalid input(s). */
public class InvalidRequestException extends RuntimeException {

  public InvalidRequestException() {
    super("Invalid inputs of the request.");
  }

  public InvalidRequestException(String message) {
    super("Invalid inputs of the request. " + message);
  }
}
