package com.backend.coapp.exception;

/**
 * This exception will be thrown when the user tries to update/delete the experience that doesn't
 * belong to the user.
 */
public class ExperienceNotOwnException extends RuntimeException {
  public ExperienceNotOwnException(String message) {
    super("Experience record does not belong to this user. " + message);
  }
}
