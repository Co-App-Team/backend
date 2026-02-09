package com.backend.coapp.model.document;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

/** Company Model */
@Data
@NoArgsConstructor
@Document(collection = "companies")
public class CompanyModel {

  @Id private String id;

  @NotBlank(message = "Company name cannot be empty")
  private String companyName; // display name for case sensitivity

  @Indexed(unique = true)
  private String companyNameLower; // stored in lowercase for case-insensitive uniqueness

  @NotBlank(message = "Location cannot be empty")
  private String location;

  @NotBlank(message = "Website cannot be empty")
  @URL(message = "Website must be a valid URL")
  private String website;

  private Double avgRating;

  @CreatedDate private Instant dateCreated;

  @LastModifiedDate private Instant dateModified;

  /**
   * Constructor for creating a new company. Average rating is automatically initialized to 0.0 and
   * must be calculated from reviews.
   *
   * @param companyName The company name (will be trimmed)
   * @param location The company location
   * @param website The company website URL
   */
  public CompanyModel(String companyName, String location, String website) {
    this.companyName = companyName != null ? companyName.trim() : null;
    this.companyNameLower = this.companyName != null ? this.companyName.toLowerCase() : null;
    this.location = location != null ? location.trim() : null;
    this.website = website != null ? website.trim() : null;
    this.avgRating = 0.0; // new companies start with 0.0 rating
  }

  // must have custom setter here for the case-insensitive field
  public void setCompanyName(String companyName) {
    this.companyName = companyName != null ? companyName.trim() : null;
    this.companyNameLower = this.companyName != null ? this.companyName.toLowerCase() : null;
  }

  @Component
  static class CompanyModelListener extends AbstractMongoEventListener<CompanyModel> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<CompanyModel> event) {
      CompanyModel company = event.getSource();

      // string fields
      if (company.getLocation() != null) {
        company.setLocation(company.getLocation().trim());
      }
      if (company.getWebsite() != null) {
        company.setWebsite(company.getWebsite().trim());
      }

      // average rating
      if (company.getAvgRating() == null) {
        company.setAvgRating(0.0);
      }

      // sync lowercase name for consistency
      if (company.getCompanyName() != null) {
        company.setCompanyNameLower(company.getCompanyName().toLowerCase());
      }
    }
  }
}
