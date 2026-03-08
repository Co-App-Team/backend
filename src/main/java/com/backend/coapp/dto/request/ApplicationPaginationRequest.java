package com.backend.coapp.dto.request;

import com.backend.coapp.util.ApplicationConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationPaginationRequest {

  private Integer page = ApplicationConstants.APPLICATION_DEFAULT_PAGE;
  private Integer size = ApplicationConstants.APPLICATION_DEFAULT_SIZE;

  protected void validatePagination() {
    if (this.page == null || this.page < 0) {
      this.page = ApplicationConstants.APPLICATION_DEFAULT_PAGE;
    }

    if (this.size == null || this.size < 1) {
      this.size = ApplicationConstants.APPLICATION_DEFAULT_SIZE;
    }
    if (this.size > ApplicationConstants.APPLICATION_MAX_SIZE) {
      this.size = ApplicationConstants.APPLICATION_MAX_SIZE;
    }
  }
}
