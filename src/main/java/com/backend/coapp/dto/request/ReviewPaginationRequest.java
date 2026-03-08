package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.util.PaginationConstants;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReviewPaginationRequest extends PaginationRequest implements IRequest {

  public ReviewPaginationRequest(Integer page, Integer size) {
    setPage(page != null ? page : PaginationConstants.REVIEW_DEFAULT_PAGE);
    setSize(size != null ? size : PaginationConstants.REVIEW_DEFAULT_SIZE);
  }

  @Override
  public void validateRequest() throws InvalidRequestException {
    super.normalizePagination(
        PaginationConstants.REVIEW_DEFAULT_PAGE,
        PaginationConstants.REVIEW_DEFAULT_SIZE,
        PaginationConstants.REVIEW_MAX_SIZE);
  }
}
