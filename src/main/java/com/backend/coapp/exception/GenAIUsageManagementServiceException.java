package com.backend.coapp.exception;

public class GenAIUsageManagementServiceException extends RuntimeException {
  public GenAIUsageManagementServiceException() {
    super("GenAI usage management failure.");
  }

  public GenAIUsageManagementServiceException(String message) {
    super("GenAI usage management failure. " + message);
  }
}
