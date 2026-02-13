package com.backend.coapp.exception;

/** thrown when a company is not found */
public class CompanyNotFoundException extends RuntimeException {

  public CompanyNotFoundException() {
    super("Company with this companyId does not exist.");
  }
}
