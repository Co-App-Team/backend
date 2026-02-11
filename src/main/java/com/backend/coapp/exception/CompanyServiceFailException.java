package com.backend.coapp.exception;

/** This exception will be thrown when company service operations fail. */
public class CompanyServiceFailException extends RuntimeException {

  public CompanyServiceFailException() {
    super("An unexpected error occurred while processing your request.");
  }

  public CompanyServiceFailException(String message) {
    super(message);
  }
}
