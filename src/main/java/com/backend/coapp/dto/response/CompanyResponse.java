package com.backend.coapp.dto.response;

import com.backend.coapp.model.document.CompanyModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

/** DTO response for returning company information. */
@Getter
@AllArgsConstructor
public class CompanyResponse implements IResponse {

  private String companyId;
  private String companyName;
  private String location;
  private String website;
  private Double avgRating;

  /**
   * Create a map, where keys are object attributes name and values are attributes' values
   *
   * @return Map<String, Object>
   */
  @Override
  public Map<String, Object> toMap() {
    return Map.of(
      "companyId", this.companyId,
      "companyName", this.companyName,
      "location", this.location,
      "website", this.website,
      "avgRating", this.avgRating
    );
  }

  /**
   * Maps CompanyModel to CompanyResponse DTO
   *
   * @param company The company model to map
   * @return CompanyResponse DTO
   */
  public static CompanyResponse fromModel(CompanyModel company) {
    return new CompanyResponse(
      company.getId(),
      company.getCompanyName(),
      company.getLocation(),
      company.getWebsite(),
      company.getAvgRating()
    );
  }
}
