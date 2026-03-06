package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import com.backend.coapp.util.UrlValidator;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO request for creating a new application. Required: companyId, jobTitle, status,
 * applicationDeadline, dateApplied. Optional: jobDescription, numPositions, sourceLink, notes.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateApplicationRequest implements IRequest {

  private String userId;
  private String companyId;
  private String jobTitle;
  private ApplicationStatus status;
  private LocalDate applicationDeadline;

  private String jobDescription;
  private Integer numPositions;
  private String sourceLink;
  private LocalDate dateApplied;
  private String notes;

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  @Override
  public void validateRequest() {

    // Required
    if (isBlank(userId)) {
      throw new InvalidRequestException("User id cannot be null or empty.");
    }
    if (isBlank(companyId)) {
      throw new InvalidRequestException("Company id cannot be null or empty.");
    }
    if (isBlank(jobTitle)) {
      throw new InvalidRequestException("Job title cannot be null or empty.");
    }

    if (jobTitle.length() > ApplicationConstants.MAX_JOB_TITLE_LENGTH) {
      throw new InvalidRequestException(
          "Job title cannot exceed %s characters."
              .formatted(ApplicationConstants.MAX_JOB_TITLE_LENGTH));
    }

    if (status == null) {
      throw new InvalidRequestException("Status cannot be null.");
    }
    if (applicationDeadline == null) {
      throw new InvalidRequestException("Application deadline cannot be null.");
    }

    // Optional fields
    if (jobDescription != null
        && jobDescription.length() > ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH) {
      throw new InvalidRequestException(
          "Job description cannot exceed %s characters."
              .formatted(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH));
    }
    if (notes != null && notes.length() > ApplicationConstants.MAX_JOB_NOTES_LENGTH) {
      throw new InvalidRequestException(
          "Notes cannot exceed %s characters."
              .formatted(ApplicationConstants.MAX_JOB_NOTES_LENGTH));
    }
    if (numPositions != null && numPositions < 0) {
      throw new InvalidRequestException("Number of positions cannot be negative.");
    }
    if (dateApplied != null && dateApplied.isAfter(applicationDeadline)) {
      throw new InvalidRequestException("Date applied cannot be after application deadline.");
    }
    if (sourceLink != null && !isBlank(sourceLink) && !UrlValidator.isValidUrl(sourceLink.trim())) {
      throw new InvalidRequestException("Source link is not a valid URL.");
    }
  }
}
