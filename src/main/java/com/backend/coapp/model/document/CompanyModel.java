package com.backend.coapp.model.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Company Model */
@Getter
@SuppressWarnings("LombokSetterMayBeUsed") // ignore this warning when we want to use our own setters
@Document(collection = "companies")
public class CompanyModel {

  @Id private String id;

  @NotNull(message = "Company name cannot be null")
  @NotBlank(message = "Company name cannot be empty")
  @Indexed(unique = true)
  private String companyNameLower; // stored in lowercase for case insensitive uniqueness

  @NotNull(message = "Company name cannot be null")
  @NotBlank(message = "Company name cannot be empty")
  private String companyName; // display name preserving case sensitivity

  @NotBlank(message = "Location cannot be empty")
  private String location;

  @NotBlank(message = "Website cannot be empty")
  @Pattern(regexp = "^(https?://).*", message = "Website must be a valid URL")
  private String website;

  @NotNull(message = "Average rating cannot be null")
  private Double avgRating;

  /** Empty constructor initializing average rating to 0.0 */
  public CompanyModel() {
    this.avgRating = 0.0;
  }

  /**
   * Constructor for creating a new company.
   * Average rating is automatically initialized to 0.0 and must be calculated from reviews.
   *
   * @param companyName The company name (will be trimmed and stored) - cannot be null
   * @param location The company location
   * @param website The company website URL
   * @throws IllegalArgumentException if companyName is null
   */
  public CompanyModel(String companyName, String location, String website) {
    if (companyName == null) {
      throw new IllegalArgumentException("Company name cannot be null");
    }

    this.companyName = companyName.trim();
    this.companyNameLower = this.companyName.toLowerCase();
    this.location = location;
    this.website = website;
    this.avgRating = 0.0; // initialize to 0.0 then update with reviews
  }

  /** Setter methods */
  public void setId(String id) { this.id = id; }

  /**
   * Sets the company name and automatically updates the lowercase version
   * for case-insensitivity.
   *
   * @param companyName The company name to set. cannot be null
   * @throws IllegalArgumentException if companyName is null
   */
  public void setCompanyName(String companyName) {
    if (companyName == null) {
      throw new IllegalArgumentException("Company name cannot be null");
    }
    this.companyName = companyName.trim();
    this.companyNameLower = this.companyName.toLowerCase();
  }

  public void setLocation(String location) { this.location = location; }

  public void setWebsite(String website) { this.website = website; }

  public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
}
