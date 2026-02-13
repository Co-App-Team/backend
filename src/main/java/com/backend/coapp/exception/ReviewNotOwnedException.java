package com.backend.coapp.exception;

/** thrown when a user tries to update/delete a review they don't own */
public class ReviewNotOwnedException extends RuntimeException {

  public ReviewNotOwnedException(String action) {
    super("You can only " + action + " your own reviews.");
  }
}
