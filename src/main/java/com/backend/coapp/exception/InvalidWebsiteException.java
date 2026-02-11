package com.backend.coapp.exception;

/** This exception will be thrown when a website URL is invalid. */
public class InvalidWebsiteException extends RuntimeException {

  public InvalidWebsiteException() {
    super("The website URL is not valid.");
  }
}
