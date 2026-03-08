package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.util.PaginationConstants;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
public class ReviewPaginationRequest extends PaginationRequest implements IRequest {

  @Override
  public void validateRequest() throws InvalidRequestException {
    super.normalizePagination(
        PaginationConstants.REVIEW_DEFAULT_PAGE,
        PaginationConstants.REVIEW_DEFAULT_SIZE,
        PaginationConstants.REVIEW_MAX_SIZE);
  }
}
