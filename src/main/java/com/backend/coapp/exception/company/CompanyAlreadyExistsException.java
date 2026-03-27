package com.backend.coapp.exception.company;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.CompanyErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** thrown when attempting to create a company that already exists */
@Getter
public class CompanyAlreadyExistsException extends ApiException {

  private final String existingCompanyId;

  public CompanyAlreadyExistsException(String existingCompanyId) {
    super("A company with this name already exists.");
    this.existingCompanyId = existingCompanyId;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public Object getErrorCode() {
    return CompanyErrorCode.COMPANY_ALREADY_EXISTS;
  }
}
