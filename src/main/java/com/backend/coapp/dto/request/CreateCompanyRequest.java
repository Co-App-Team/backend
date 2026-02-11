package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** DTO request for creating a new company. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCompanyRequest implements IRequest {
  //JSON request keys
  private String companyName;
  private String location;
  private String website;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.companyName == null || this.companyName.isBlank() ||
      this.location == null || this.location.isBlank() ||
      this.website == null || this.website.isBlank()) {
      throw new InvalidRequestException("Fields (companyName, location, website) cannot be null or empty."); //! TODO: should i be throwing here?
    }

    // Validate URL format
    if (!this.website.matches("^(https?://).*")) {
      throw new InvalidRequestException("Website must be a valid URL starting with http:// or https://"); //! TODO: should i be throwing here?
    }
  }
}
