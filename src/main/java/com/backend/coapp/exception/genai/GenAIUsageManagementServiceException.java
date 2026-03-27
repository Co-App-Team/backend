package com.backend.coapp.exception.genai;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when something goes wrong in GenAI usage management service. */
public class GenAIUsageManagementServiceException extends ApiException {
  public GenAIUsageManagementServiceException() {
    super("GenAI usage management failure.");
  }

  public GenAIUsageManagementServiceException(String message) {
    super("GenAI usage management failure. " + message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public Object getErrorCode() {
    return SystemErrorCode.INTERNAL_ERROR;
  }
}
