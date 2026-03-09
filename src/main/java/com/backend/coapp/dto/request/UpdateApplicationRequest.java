package com.backend.coapp.dto.request;

import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import com.backend.coapp.util.UrlValidator;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO request for creating a new application. Optional: companyId, jobTitle, status,
 * applicationDeadline, dateApplied, jobDescription, numPositions, sourceLink, notes.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateApplicationRequest implements IRequest {

  // optional
  private String companyId;
  private String jobTitle;
  private ApplicationStatus status;
  private LocalDate applicationDeadline;
  private String jobDescription;
  private Integer numPositions;
  private String sourceLink;
  private LocalDate dateApplied;
  private String notes;

  private boolean hasAtLeastOneField() {
    return companyId != null
        || jobTitle != null
        || status != null
        || applicationDeadline != null
        || jobDescription != null
        || numPositions != null
        || sourceLink != null
        || dateApplied != null
        || notes != null;
  }

  public void validateRequest() {
    if (!hasAtLeastOneField()) {
      throw new InvalidRequestException(
          "At least one field must be provided to update the application request.");
    }
    if (companyId != null && companyId.isBlank()) {
      throw new InvalidRequestException("Company Id cannot be blank.");
    }

    if (jobTitle != null && jobTitle.isBlank()) {
      throw new InvalidRequestException("Job Title cannot be blank.");
    }

    if (jobDescription != null
        && jobDescription.length() > ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH) {
      throw new InvalidRequestException("Description cannot exceed 2000 characters");
    }
    if (notes != null && notes.length() > ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH) {
      throw new InvalidRequestException("Notes cannot exceed 2000 characters");
    }
    if (numPositions != null && numPositions < 0) {
      throw new InvalidRequestException("Number of positions cannot be negative.");
    }

    if (sourceLink != null && !UrlValidator.isValidUrl(sourceLink.trim())) {
      throw new InvalidRequestException("Website must be a valid URL");
    }

    if (dateApplied != null
        && applicationDeadline != null
        && dateApplied.isAfter(applicationDeadline)) {
      throw new InvalidRequestException("Date applied cannot be after application deadline.");
    }
  }
}
