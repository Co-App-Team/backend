package com.backend.coapp.exception;

/** thrown when a website URL is invalid */
public class InvalidWebsiteException extends RuntimeException {

  public InvalidWebsiteException() {
    super("The website URL is not valid.");
  }
}
