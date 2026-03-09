package com.backend.coapp.exception.review;

/** thrown when review service operations fail */
public class ReviewServiceFailException extends RuntimeException {

  public ReviewServiceFailException(String message) {
    super(message);
  }
}
