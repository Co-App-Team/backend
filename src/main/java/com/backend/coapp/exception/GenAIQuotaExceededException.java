package com.backend.coapp.exception;

public class GenAIQuotaExceededException extends RuntimeException {
  public GenAIQuotaExceededException() {
    super(
        "You have reached monthly usage limit for Co-App chatbot. Please wait before making more requests.");
  }
}
