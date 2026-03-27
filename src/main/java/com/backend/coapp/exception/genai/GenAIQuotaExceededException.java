package com.backend.coapp.exception.genai;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.model.enumeration.GenAIErrorCode;
import org.springframework.http.HttpStatus;

/** This exception will be thrown when the user exceed request to use GenAI limit. */
public class GenAIQuotaExceededException extends ApiException {
  public GenAIQuotaExceededException() {
    super(
        "You have reached monthly usage limit for Co-App chatbot. Please wait before making more requests.");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.TOO_MANY_REQUESTS;
  }

  @Override
  public Object getErrorCode() {
    return GenAIErrorCode.OVER_LIMIT_CHATBOT_REQUEST;
  }
}
