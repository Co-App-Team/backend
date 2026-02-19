package com.backend.coapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CompanyNotFoundException extends RuntimeException {
  public CompanyNotFoundException(String companyId) {
    super(String.format("Could not find company with ID: %s", companyId));
  }

  public CompanyNotFoundException() {
    super("Company with the provided id does not exist.");
  }
}
