package com.backend.coapp.exception;

import lombok.Getter;

/** This exception will be thrown when attempting to create a company that already exists. */
@Getter
public class CompanyAlreadyExistsException extends RuntimeException {

  private final String existingCompanyId;

  public CompanyAlreadyExistsException(String existingCompanyId) {
    super("A company with this name already exists.");
    this.existingCompanyId = existingCompanyId;
  }
}
