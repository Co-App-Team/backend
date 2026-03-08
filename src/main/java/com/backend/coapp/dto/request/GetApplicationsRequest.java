package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import com.backend.coapp.util.ApplicationValidSearchParameters;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * all query params for GET/api/application extends ApplicationPaginationRequest for page / size
 * handling.
 */
@Getter
@Setter
public class GetApplicationsRequest extends ApplicationPaginationRequest {

  // case-insensitive partial match search for company name
  private String search;

  // comma-separated ApplicationStatus names.
  private String status;

  // Field to sort by
  private String sortBy;

  // Sort direction
  private String sortOrder;

  private List<ApplicationStatus> parsedStatuses;

  /**
   * Validates every query parameter and populates the parsed fields.
   *
   * @throws InvalidRequestException if any parameter contains an invalid value.
   */
  public void validateAndParse() throws InvalidRequestException {
    validateStatus();
    validateSortBy();
    validateSortOrder();
    validateRequest();
  }

  /**
   * Parses the comma-separated status string into parsedStatuses. Each part is matched against
   * ApplicationStatus by name.
   */
  private void validateStatus() {
    if (status == null || status.isBlank()) {
      return;
    }

    parsedStatuses = new ArrayList<>();
    for (String raw : status.split(",")) {
      String trimmed = raw.trim();
      try {
        parsedStatuses.add(ApplicationStatus.valueOf(trimmed));
      } catch (IllegalArgumentException e) {
        throw new InvalidRequestException("Invalid value for status: '" + trimmed + "'.");
      }
    }
  }

  private void validateSortBy() {
    if (sortBy == null || sortBy.isBlank()) {
      sortBy = ApplicationConstants.SORT_BY_DATE_APPLIED;
      return;
    }

    if (!ApplicationValidSearchParameters.VALID_SORT_BY_VALUES.contains(sortBy)) {
      throw new InvalidRequestException("Invalid value for sortBy: '" + sortBy + "'.");
    }
  }

  // normalizes to lower-case
  private void validateSortOrder() {
    if (sortOrder == null || sortOrder.isBlank()) {
      sortOrder = ApplicationConstants.DEFAULT_SORT_ORDER;
      return;
    }

    String normalised = sortOrder.toLowerCase();
    if (!ApplicationValidSearchParameters.VALID_SORT_ORDER_VALUES.contains(normalised)) {
      throw new InvalidRequestException("Invalid value for sortOrder: '" + sortOrder + "'.");
    }
    sortOrder = normalised;
  }
}
