package com.backend.coapp.exception.genai;

/** This exception will be thrown when we reach usage limit. User need to try again later. */
public class GenAIOutOfServiceException extends RuntimeException {
  public GenAIOutOfServiceException(String message) {
    super("Our AI service failure. " + message);
  }
}
