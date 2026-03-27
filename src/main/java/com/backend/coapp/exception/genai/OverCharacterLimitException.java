package com.backend.coapp.exception.genai;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.GenAIErrorCode;
import org.springframework.http.HttpStatus;

public class OverCharacterLimitException extends ApiException {
  public OverCharacterLimitException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public Object getErrorCode() {
    return GenAIErrorCode.OVER_LIMIT_CHARACTER;
  }
}
