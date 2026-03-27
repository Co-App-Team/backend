package com.backend.coapp.exception.company;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.CompanyErrorCode;
import org.springframework.http.HttpStatus;

public class CompanyNotFoundException extends ApiException {

  public CompanyNotFoundException() {
    super("Company with this companyId does not exist.");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public Object getErrorCode() {
    return CompanyErrorCode.COMPANY_NOT_FOUND;
  }
}
