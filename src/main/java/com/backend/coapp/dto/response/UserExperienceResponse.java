package com.backend.coapp.dto.response;

import com.backend.coapp.model.document.UserExperienceModel;
import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Response DTO for User Experience */
@Getter
@AllArgsConstructor
public class UserExperienceResponse implements IResponse {

  // JSON keys
  private String experienceId;
  private String companyId;
  private String roleTitle;
  private String roleDescription;
  private LocalDate startDate;
  private LocalDate endDate;

  /**
   * Create a map, where keys are object attributes name and values are attributes' values
   *
   * @return Map<String, Object>
   */
  @Override
  public Map<String, Object> toMap() {
    return Map.of(
        "experienceId", this.experienceId,
        "companyId", this.companyId,
        "roleTitle", this.roleTitle,
        "roleDescription", this.roleDescription,
        "startDate", this.startDate,
        "endDate", this.endDate);
  }

  /**
   * Map UserExperienceModel to CompanyResponse DTO
   *
   * @param userExperienceModel The User Experience model to map
   * @return UserExperience DTO
   */
  public static UserExperienceResponse fromModel(UserExperienceModel userExperienceModel) {
    return new UserExperienceResponse(
        userExperienceModel.getId(),
        userExperienceModel.getCompanyId(),
        userExperienceModel.getRoleTitle(),
        userExperienceModel.getRoleDescription(),
        userExperienceModel.getStartDate(),
        userExperienceModel.getEndDate());
  }
}
