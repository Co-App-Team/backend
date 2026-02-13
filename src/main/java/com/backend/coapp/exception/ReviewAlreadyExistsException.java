package com.backend.coapp.exception;

import lombok.Getter;

/** thrown when attempting to create a review that already exists for a user/company pair */
@Getter
public class ReviewAlreadyExistsException extends RuntimeException {

  private final String existingReviewId;

  public ReviewAlreadyExistsException(String existingReviewId) {
    super("You have already submitted a review for this company.");
    this.existingReviewId = existingReviewId;
  }
}
