package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.util.UrlValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** DTO request for updating a company. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompanyRequest implements IRequest {
  // JSON request keys
  private String companyName;
  private String location;
  private String website;

  @Override
  public void validateRequest() throws InvalidRequestException {
    // At least one field must be provided
    if ((this.companyName == null || this.companyName.isBlank()) &&
      (this.location == null || this.location.isBlank()) &&
      (this.website == null || this.website.isBlank())) {
      throw new InvalidRequestException("At least one field must be provided to update the company.");
    }

    if (this.website != null && !this.website.isBlank() && !UrlValidator.isValidUrl(this.website)) {
      throw new InvalidRequestException("Website must be a valid URL starting with http:// or https://");
    }
  }
}
