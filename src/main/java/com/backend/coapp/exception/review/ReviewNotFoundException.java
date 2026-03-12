package com.backend.coapp.exception.review;

/** thrown when a review is not found */
public class ReviewNotFoundException extends RuntimeException {

  public ReviewNotFoundException() {
    super("Review with the provided id does not exist for this company.");
  }
}
