package com.backend.coapp.exception.review;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.ReviewErrorCode;
import org.springframework.http.HttpStatus;

/** thrown when a review is not found */
public class ReviewNotFoundException extends ApiException {

  public ReviewNotFoundException() {
    super("Review with the provided id does not exist for this company.");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public Object getErrorCode() {
    return ReviewErrorCode.REVIEW_NOT_FOUND;
  }
}
