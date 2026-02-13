package com.backend.coapp.exception;

/** thrown when company service operations fail */
public class CompanyServiceFailException extends RuntimeException {

  public CompanyServiceFailException(String message) {
    super(message);
  }
}
