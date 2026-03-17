package com.backend.coapp.exception.genai;

/**
 * This exception will be thrown when the user tries to update/delete the experience that doesn't
 * belong to the user.
 */
public class ExperienceNotOwnedException extends RuntimeException {
  public ExperienceNotOwnedException(String message) {
    super("Experience record does not belong to this user. " + message);
  }
}
