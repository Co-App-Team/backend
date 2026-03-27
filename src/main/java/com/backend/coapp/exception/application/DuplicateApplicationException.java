package com.backend.coapp.exception.application;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.ApplicationErrorCode;
import org.springframework.http.HttpStatus;

/** This application will be thrown when the application already exist */
public class DuplicateApplicationException extends ApiException {
  public DuplicateApplicationException(String jobTitle, String companyId) {
    super(
        String.format(
            "User has already created an application for '%s' at company %s", jobTitle, companyId));
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public Object getErrorCode() {
    return ApplicationErrorCode.DUPLICATE_APPLICATION;
  }
}
