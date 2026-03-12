package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** DTO for GenAI resumer advisor request */
@Getter
@AllArgsConstructor
public class GenAIResumeAdvisorRequest implements IRequest {
  private String userPrompt;
  private String applicationId;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (userPrompt == null || userPrompt.isBlank()) {
      throw new InvalidRequestException("Prompt can NOT be null or blank");
    }

    if (applicationId != null && applicationId.isBlank()) {
      throw new InvalidRequestException("Application can NOT be null or blank");
    }
  }
}
