package com.backend.coapp.dto.request;

import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.util.ExperienceConstants;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request DTO for UserExperience */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserExperienceRequest implements IRequest {
  // JSON request keys
  private String companyId;
  private String roleTitle;
  private String roleDescription;
  private LocalDate startDate;
  private LocalDate endDate;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (companyId == null || companyId.isBlank()) {
      throw new InvalidRequestException("Company ID must not be NULL or blank.");
    }

    if (roleTitle == null || roleTitle.isBlank()) {
      throw new InvalidRequestException("Role title must not be NULL or blank.");
    }

    if (roleTitle.length() > ExperienceConstants.MAX_JOB_TITLE_LENGTH) {
      throw new InvalidRequestException(
          "Role title must be less than %s characters"
              .formatted(ExperienceConstants.MAX_JOB_TITLE_LENGTH));
    }

    if (roleDescription == null || roleDescription.isBlank()) {
      throw new InvalidRequestException("Role description must not be NULL or blank.");
    }

    if (roleDescription.length() > ExperienceConstants.MAX_EXPERIENCE_DESCRIPTION_LENGTH) {
      throw new InvalidRequestException(
          "Role title must be less than %s characters."
              .formatted(ExperienceConstants.MAX_EXPERIENCE_DESCRIPTION_LENGTH));
    }

    if (startDate == null) {
      throw new InvalidRequestException("Start date must not be null");
    }

    if (endDate != null && endDate.isBefore(startDate)) {
      throw new InvalidRequestException("End date must be after start date.");
    }
  }
}
