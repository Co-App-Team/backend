package com.backend.coapp.exception.genai;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import org.springframework.http.HttpStatus;

/**
 * This is internal exception, which will be thrown when there is something wrong with GenAI service
 */
public class GenAIServiceException extends ApiException {
  @Override
  public HttpStatus getStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public Object getErrorCode() {
    return SystemErrorCode.INTERNAL_ERROR;
  }

  public GenAIServiceException(String message) {
    super("GenAI service failed. " + message);
  }
}
