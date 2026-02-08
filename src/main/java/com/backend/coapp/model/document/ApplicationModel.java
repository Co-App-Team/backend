package com.backend.coapp.model.document;

import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import lombok.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

@Data // Generates Getters, Setters, equals(), hashCode(), and toString()
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationModel {

  // Required Fields
  @Id private String id;

  // NOTE: I did not include null checks as they are redundant with notblanks.
  @NotBlank(message = "User ID cannot be empty")
  private String userId;

  @NotBlank(message = "Company ID cannot be empty")
  private String companyId;

  @NotBlank(message = "Job title cannot be empty")
  private String jobTitle;

  @NotNull(message = "Application Deadline cannot be null")
  private LocalDate applicationDeadline;

  @NotNull(message = "Status cannot be null")
  private ApplicationStatus status;

  // Auto-generated Fields

  @CreatedDate private Instant dateCreated;

  @LastModifiedDate private Instant dateModified;

  // Optional Fields

  @URL(message = "Website must be a valid URL")
  private String sourceLink;

  @Min(value = 1, message = "Number of positions must be at least 1")
  private Integer numPositions;

  private LocalDate dateApplied;

  @Size(
      max = ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH,
      message =
          ("Description cannot exceed "
              + ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH
              + " characters"))
  private String jobDescription;

  @Size(
      max = ApplicationConstants.MAX_JOB_NOTES_LENGTH,
      message =
          ("Notes cannot exceed "
              + ApplicationConstants.MAX_JOB_NOTES_LENGTH
              + " characters"))
  private String notes;
}
