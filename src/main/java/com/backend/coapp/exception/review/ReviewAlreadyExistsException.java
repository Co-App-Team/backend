package com.backend.coapp.exception.review;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.ReviewErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** thrown when attempting to create a review that already exists for a user/company pair */
@Getter
public class ReviewAlreadyExistsException extends ApiException {

  public ReviewAlreadyExistsException() {
    super("You have already submitted a review for this company.");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public Object getErrorCode() {
    return ReviewErrorCode.REVIEW_ALREADY_EXISTS;
  }
}
