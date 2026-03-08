package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.util.ApplicationConstants;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ApplicationPaginationRequest extends PaginationRequest  implements IRequest {

  public ApplicationPaginationRequest(Integer page, Integer size) {
    setPage(page != null ? page : ApplicationConstants.APPLICATION_DEFAULT_PAGE);
    setSize(size != null ? size : ApplicationConstants.APPLICATION_DEFAULT_SIZE);
  }

  @Override
  public void validateRequest() throws InvalidRequestException {
    super.validatePagination(
      ApplicationConstants.APPLICATION_DEFAULT_PAGE,
      ApplicationConstants.APPLICATION_DEFAULT_SIZE,
      ApplicationConstants.APPLICATION_MAX_SIZE);
  }
}
