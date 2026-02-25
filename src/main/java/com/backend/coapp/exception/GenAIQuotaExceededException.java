package com.backend.coapp.exception;

/** This exception will be thrown when the user exceed request to use GenAI limit. */
public class GenAIQuotaExceededException extends RuntimeException {
  public GenAIQuotaExceededException() {
    super(
        "You have reached monthly usage limit for Co-App chatbot. Please wait before making more requests.");
  }
}
