package com.backend.coapp.exception.auth;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.UserErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when user update password with old password. */
public class UserInvalidPasswordChangeException extends ApiException {
  public UserInvalidPasswordChangeException() {
    super("New password must be different from old password");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return UserErrorCode.NEW_PASSWORD_SAME_WITH_OLD_PASSWORD;
  }
}
