package com.backend.coapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateApplicationException extends RuntimeException {
  public DuplicateApplicationException(String userId, String jobTitle, String companyId) {
    super(
        String.format(
            "User %s has already created an application for '%s' at company %s",
            userId, jobTitle, companyId));
  }
}
