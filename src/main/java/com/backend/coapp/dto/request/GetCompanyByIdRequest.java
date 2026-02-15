package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.util.PaginationConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetCompanyByIdRequest implements IRequest {

  private String companyId;
  private Integer page = PaginationConstants.REVIEW_DEFAULT_PAGE;
  private Integer size = PaginationConstants.REVIEW_DEFAULT_SIZE;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.companyId == null || this.companyId.isBlank()) {
      throw new InvalidRequestException("Company ID cannot be null or empty.");
    }

    if (this.page == null || this.page < 0) {
      this.page = PaginationConstants.REVIEW_DEFAULT_PAGE;
    }

    if (this.size == null || this.size < 1) {
      this.size = PaginationConstants.REVIEW_DEFAULT_SIZE;
    }
    if (this.size > PaginationConstants.REVIEW_MAX_SIZE) {
      this.size = PaginationConstants.REVIEW_MAX_SIZE;
    }
  }
}
