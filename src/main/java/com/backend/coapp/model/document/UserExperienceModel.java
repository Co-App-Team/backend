package com.backend.coapp.model.document;

import com.backend.coapp.util.ExperienceConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Model to keep track of user experience */
@Document(collection = "userExperience")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExperienceModel {
  @Id private String id;

  @NotBlank(message = "User ID cannot be empty")
  @Indexed
  private String userId;

  @NotBlank(message = "Company ID cannot be empty")
  private String companyId;

  @NotBlank(message = "Job title cannot be empty")
  @Size(
      max = ExperienceConstants.MAX_JOB_TITLE_LENGTH,
      message =
          "Job title description cannot exceed "
              + ExperienceConstants.MAX_JOB_TITLE_LENGTH
              + " characters")
  private String roleTitle;

  @NotBlank(message = "Experience description cannot be empty")
  @Size(
      max = ExperienceConstants.MAX_EXPERIENCE_DESCRIPTION_LENGTH,
      message =
          "Experience description cannot exceed "
              + ExperienceConstants.MAX_EXPERIENCE_DESCRIPTION_LENGTH
              + " characters")
  private String roleDescription;

  @NotNull(message = "Start date cannot be null")
  private LocalDate startDate;

  private LocalDate endDate;

  /**
   * UserExperienceModel
   *
   * @param userId ID of the user
   * @param companyId ID of the company of the experience
   * @param roleTitle job title of the experience
   * @param roleDescription role description of the experience
   * @param startDate start date of the experience
   * @param endDate end date of the experience
   */
  public UserExperienceModel(
      String userId,
      String companyId,
      String roleTitle,
      String roleDescription,
      LocalDate startDate,
      LocalDate endDate) {
    this.userId = userId;
    this.companyId = companyId;
    this.roleTitle = roleTitle;
    this.roleDescription = roleDescription;
    this.startDate = startDate;
    this.endDate = endDate;
  }
}
