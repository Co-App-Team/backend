package com.backend.coapp.service.genAI;

import com.backend.coapp.exception.genai.GenAIServiceException;
import com.backend.coapp.exception.genai.OverCharacterLimitException;
import com.backend.coapp.util.GenAIConstants;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "gen-ai.provider", havingValue = "gemini")
public class GeminiGenAIService implements GenAIService {
  private final Client geminiClient;

  @Value("${google.genai.model}")
  private String model;

  @Autowired
  public GeminiGenAIService(Client geminiClient) {
    this.geminiClient = geminiClient;
  }

  /**
   * Prompt to GenAI model
   *
   * @param prompt client's prompt
   * @return GenAI response
   * @throws IllegalArgumentException when invalid input
   * @throws GenAIServiceException when there is something wrong with GenAI provider.
   */
  @Override
  public String generateResponse(String prompt)
      throws IllegalArgumentException, GenAIServiceException {
    if (prompt == null || prompt.isBlank()) {
      throw new IllegalArgumentException("Prompt can't be null or blank");
    }

    if (prompt.length() > GenAIConstants.MAX_TOTAL_CHARACTERS) {
      String message =
          "Prompt exceed characters limit of " + GenAIConstants.MAX_TOTAL_CHARACTERS + ".";
      throw new OverCharacterLimitException(message);
    }
    try {
      GenerateContentResponse response =
          geminiClient.models.generateContent(this.model, prompt, null);
      return response.text();
    } catch (Exception e) {
      throw new GenAIServiceException(e.getMessage());
    }
  }
}
