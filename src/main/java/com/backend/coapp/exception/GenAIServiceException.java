package com.backend.coapp.exception;

/**
 * This is internal exception, which will be thrown when there is something wrong with GenAI service
 */
public class GenAIServiceException extends RuntimeException {
  public GenAIServiceException(String message) {
    super("GenAI service failed. " + message);
  }
}
