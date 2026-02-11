package com.backend.coapp.exception;

/** This exception will be thrown when a company is not found. */
public class CompanyNotFoundException extends RuntimeException {

  public CompanyNotFoundException() {
    super("Company with this companyId does not exist.");
  }

  public CompanyNotFoundException(String message) {
    super(message);
  }
}
