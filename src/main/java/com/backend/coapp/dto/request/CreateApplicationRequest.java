package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.UrlValidator;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** DTO request for creating a new application */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateApplicationRequest implements IRequest {

  private String companyId;
  private String jobTitle;
  private ApplicationStatus status;
  private LocalDate applicationDeadline;

  private String jobDescription;
  private Integer numPositions;
  private String sourceLink;
  private LocalDate dateApplied;
  private String notes;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.companyId == null || this.companyId.isBlank()) {
      throw new InvalidRequestException("Company id cannot be null or empty.");
    } else if (this.jobTitle == null || this.jobTitle.isBlank()) {
      throw new InvalidRequestException("Job title cannot be null or empty.");
    } else if (this.status == null) {
      throw new InvalidRequestException("Status cannot be null.");
    } else if (this.applicationDeadline == null) {
      throw new InvalidRequestException("Application deadline cannot be null.");
    }

    if (sourceLink != null && !sourceLink.isBlank() && !UrlValidator.isValidUrl(this.sourceLink)) {
      throw new InvalidRequestException("Source link is not a valid URL.");
    }
  }
}
