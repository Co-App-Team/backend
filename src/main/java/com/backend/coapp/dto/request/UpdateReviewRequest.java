package com.backend.coapp.dto.request;

import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.util.ReviewConstants;
import com.backend.coapp.util.WorkTermValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** DTO request for updating an existing review. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewRequest implements IRequest {

  private Integer rating;
  private String comment;
  private String jobTitle;
  private String workTermSeason;
  private Integer workTermYear;

  @Override
  public void validateRequest() throws InvalidRequestException {
    // had to split it like this for IntelliJ complexity to work
    validateAtLeastOneFieldProvided();
    validateRatingIfProvided();
    validateCommentIfProvided();
    validateWorkTermSeasonIfProvided();
    validateWorkTermYearIfProvided();
  }

  private void validateAtLeastOneFieldProvided() throws InvalidRequestException {
    if (this.rating == null
        && this.comment == null
        && this.jobTitle == null
        && this.workTermSeason == null
        && this.workTermYear == null) {
      throw new InvalidRequestException(
          "At least one field must be provided to update the review.");
    }
  }

  private void validateRatingIfProvided() throws InvalidRequestException {
    if (this.rating != null
      && (this.rating < ReviewConstants.MIN_RATING || this.rating > ReviewConstants.MAX_RATING)) {
      throw new InvalidRequestException("Rating must be between 1 and 5.");
    }
  }


  private void validateCommentIfProvided() throws InvalidRequestException {
    if (this.comment != null && this.comment.length() > ReviewConstants.MAX_COMMENT_LENGTH) {
      throw new InvalidRequestException(
          "comment exceeds maximum length of "
              + ReviewConstants.MAX_COMMENT_LENGTH
              + " characters.");
    }
  }

  private void validateWorkTermSeasonIfProvided() throws InvalidRequestException {
    if (this.workTermSeason != null && !this.workTermSeason.isBlank()) {
      try {
        WorkTermValidator.validateSeason(this.workTermSeason);
      } catch (IllegalArgumentException e) {
        throw new InvalidRequestException(e.getMessage());
      }
    }
  }

  private void validateWorkTermYearIfProvided() throws InvalidRequestException {
    if (this.workTermYear != null) {
      try {
        WorkTermValidator.validateYear(this.workTermYear);
      } catch (IllegalArgumentException e) {
        throw new InvalidRequestException(e.getMessage());
      }
    }
  }
}
