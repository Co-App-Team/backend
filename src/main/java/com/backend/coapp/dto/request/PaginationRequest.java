package com.backend.coapp.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class PaginationRequest {

  private Integer page;
  private Integer size;

  protected void normalizePagination(int defaultPage, int defaultSize, int maxSize) {
    if (this.page == null || this.page < 0) {
      this.page = defaultPage;
    }
    if (this.size == null || this.size < 1) {
      this.size = defaultSize;
    }
    if (this.size > maxSize) {
      this.size = maxSize;
    }
  }
}
