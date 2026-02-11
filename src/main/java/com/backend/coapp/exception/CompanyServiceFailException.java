package com.backend.coapp.exception;

/** This exception will be thrown when company service operations fail. */
public class CompanyServiceFailException extends RuntimeException {

  public CompanyServiceFailException(String message) {
    super(message);
  }
}
