package com.backend.coapp.exception.genai;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.UserExperienceErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when experience request by client does NOT exist */
public class ExperienceNotFoundException extends ApiException {
  public ExperienceNotFoundException() {
    super("Experience does NOT exist.");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public Object getErrorCode() {
    return UserExperienceErrorCode.EXPERIENCE_NOT_FOUND;
  }
}
