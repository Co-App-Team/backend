package com.backend.coapp.service.genAI;

import com.backend.coapp.exception.genai.GenAIServiceException;

/**
 * Interface for GenAI service integrations.
 *
 * <p>All GenAI providers (e.g. Gemini, OpenAI) must implement this interface to ensure a consistent
 * and interchangeable API across the application.
 */
public interface GenAIService {
  /**
   * Prompt to GenAI model
   *
   * @param prompt client's prompt
   * @return GenAI response
   * @throws IllegalArgumentException when invalid input
   * @throws GenAIServiceException when there is something wrong with GenAI provider.
   */
  String generateResponse(String prompt) throws IllegalArgumentException, GenAIServiceException;
}
