package com.backend.coapp.exception;

/** This exception will be thrown when something goes wrong in GenAI usage management service. */
public class GenAIUsageManagementServiceException extends RuntimeException {
  public GenAIUsageManagementServiceException() {
    super("GenAI usage management failure.");
  }

  public GenAIUsageManagementServiceException(String message) {
    super("GenAI usage management failure. " + message);
  }
}
