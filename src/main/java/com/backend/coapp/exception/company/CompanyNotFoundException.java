package com.backend.coapp.exception.company;

public class CompanyNotFoundException extends RuntimeException {

  public CompanyNotFoundException() {
    super("Company with this companyId does not exist.");
  }
}
