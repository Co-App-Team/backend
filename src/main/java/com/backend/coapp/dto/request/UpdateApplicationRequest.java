package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.util.ApplicationConstants;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UpdateApplicationRequest {

  private String companyId;
  private String jobTitle;

  @Size(
      max = ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH,
      message = "Description cannot exceed 2000 characters")
  private String jobDescription;

  @Size(
      max = ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH,
      message = "Notes cannot exceed 2000 characters")
  private String notes;

  @URL(message = "Source link must be a valid URL")
  private String sourceLink;

  public void validateRequest() {
    if (this.companyId == null || this.companyId.isBlank()) {
      throw new InvalidRequestException("Company ID cannot be blank.");
    }
    if (this.jobTitle == null || this.jobTitle.isBlank()) {
      throw new InvalidRequestException("Job Title cannot be blank.");
    }
  }
}
