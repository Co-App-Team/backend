package com.backend.coapp.exception;

/** This exception will be thrown when experience request by client does NOT exist */
public class ExperienceNotFoundException extends RuntimeException {
  public ExperienceNotFoundException() {
    super("Experience does NOT exist.");
  }
}
