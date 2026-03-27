package com.backend.coapp.exception.genai;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.UserExperienceErrorCode;
import org.springframework.http.HttpStatus;

/**
 * This exception will be thrown when the user tries to update/delete the experience that doesn't
 * belong to the user.
 */
public class ExperienceNotOwnedException extends ApiException {
  public ExperienceNotOwnedException(String message) {
    super("Experience record does not belong to this user. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.FORBIDDEN;
  }

  @Override
  public Object getErrorCode() {
    return UserExperienceErrorCode.EXPERIENCE_NOT_OWN;
  }
}
