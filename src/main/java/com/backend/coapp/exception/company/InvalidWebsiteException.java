package com.backend.coapp.exception.company;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.CompanyErrorCode;
import org.springframework.http.HttpStatus;

/** thrown when a website URL is invalid */
public class InvalidWebsiteException extends ApiException {

  public InvalidWebsiteException() {
    super("The website URL is not valid.");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return CompanyErrorCode.INVALID_WEBSITE;
  }
}
