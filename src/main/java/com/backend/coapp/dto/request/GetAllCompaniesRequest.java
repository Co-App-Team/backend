package com.backend.coapp.dto.request;

import com.backend.coapp.util.PaginationConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO for query parameters when getting all companies */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllCompaniesRequest {

  private String search;
  private Integer page = PaginationConstants.COMPANY_DEFAULT_PAGE;
  private Integer size = PaginationConstants.COMPANY_DEFAULT_SIZE;
  private Boolean usePagination = PaginationConstants.DEFAULT_USE_PAGINATION;
  /* Validates and sanitizes the query parameters for getting all companies */
  public void validateRequest() {
    if (this.page == null) {
      this.page = PaginationConstants.COMPANY_DEFAULT_PAGE;
    }
    if (this.size == null) {
      this.size = PaginationConstants.COMPANY_DEFAULT_SIZE;
    }
    if (this.usePagination == null) {
      this.usePagination = false;
    }

    if (this.page < PaginationConstants.COMPANY_DEFAULT_PAGE) {
      this.page = PaginationConstants.COMPANY_DEFAULT_PAGE;
    }

    if (this.size > PaginationConstants.COMPANY_MAX_SIZE) {
      this.size = PaginationConstants.COMPANY_MAX_SIZE;
    }
    if (this.size < PaginationConstants.COMPANY_MIN_SIZE) {
      this.size = PaginationConstants.COMPANY_DEFAULT_SIZE;
    }
  }
}
