package com.backend.coapp.exception.company;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CompanyNotFoundException extends RuntimeException {

  public CompanyNotFoundException() {
    super("Company with this companyId does not exist.");
  }
}
