package com.backend.coapp.exception.global;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.UserErrorCode;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
  public UserNotFoundException() {
    super("Could not find user");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return UserErrorCode.USER_NOT_EXIST;
  }
}
