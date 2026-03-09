package com.backend.coapp.dto.request;

import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.util.ReviewConstants;
import com.backend.coapp.util.WorkTermValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** DTO request for creating a new review */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest implements IRequest {

  private Integer rating;
  private String comment;
  private String jobTitle;
  private String workTermSeason;
  private Integer workTermYear;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.rating == null) {
      throw new InvalidRequestException("Rating is required and must be between 1 and 5.");
    }

    if (this.rating < ReviewConstants.MIN_RATING || this.rating > ReviewConstants.MAX_RATING) {
      throw new InvalidRequestException("Rating is required and must be between 1 and 5.");
    }

    if (this.comment != null && this.comment.length() > ReviewConstants.MAX_COMMENT_LENGTH) {
      throw new InvalidRequestException(
          "comment exceeds maximum length of "
              + ReviewConstants.MAX_COMMENT_LENGTH
              + " characters.");
    }

    if (this.jobTitle == null || this.jobTitle.isBlank()) {
      throw new InvalidRequestException("Job title is required and cannot be empty.");
    }

    if (this.workTermSeason == null || this.workTermSeason.isBlank()) {
      throw new InvalidRequestException("Work term season is required and cannot be empty.");
    }

    try {
      WorkTermValidator.validateSeason(this.workTermSeason);
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException(e.getMessage());
    }

    if (this.workTermYear == null) {
      throw new InvalidRequestException("Work term year is required and cannot be null.");
    }

    try {
      WorkTermValidator.validateYear(this.workTermYear);
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException(e.getMessage());
    }
  }
}
