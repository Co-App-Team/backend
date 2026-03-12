package com.backend.coapp.exception.application;

public class DuplicateApplicationException extends RuntimeException {
  public DuplicateApplicationException(String jobTitle, String companyId) {
    super(
        String.format(
            "User has already created an application for '%s' at company %s", jobTitle, companyId));
  }
}
