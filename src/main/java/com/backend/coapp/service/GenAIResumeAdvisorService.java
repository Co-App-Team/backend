package com.backend.coapp.service;

import com.backend.coapp.service.genAI.GenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenAIResumeAdvisorService {
  private final GenAIService genAIService;
  private final GenAIUsageManagementService genAIUsageManagementService;

  @Autowired
  public GenAIResumeAdvisorService(
      GenAIService genAIService, GenAIUsageManagementService genAIUsageManagementService) {
    this.genAIService = genAIService;
    this.genAIUsageManagementService = genAIUsageManagementService;
  }

  public String getAdvice(String userId, String applicationId, String prompt) {

    this.genAIUsageManagementService.checkAndIncrementUsage(userId);
    return this.genAIService.generateResponse(prompt);
  }
}
