package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.util.ApplicationConstants;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
public class ApplicationPaginationRequest extends PaginationRequest implements IRequest {

  @Override
  public void validateRequest() throws InvalidRequestException {
    super.normalizePagination(
        ApplicationConstants.APPLICATION_DEFAULT_PAGE,
        ApplicationConstants.APPLICATION_DEFAULT_SIZE,
        ApplicationConstants.APPLICATION_MAX_SIZE);
  }
}
