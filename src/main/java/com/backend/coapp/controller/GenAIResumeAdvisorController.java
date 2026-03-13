package com.backend.coapp.controller;

import com.backend.coapp.dto.request.GenAIResumeAdvisorRequest;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.service.GenAIResumeAdvisorService;
import com.backend.coapp.service.GenAIUsageManagementService;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Getter // For testing only
@RequestMapping("/api/resume-ai-advisor")
public class GenAIResumeAdvisorController {
  /** Singleton service and repository * */
  private final GenAIResumeAdvisorService genAIResumeAdvisorService;

  private final GenAIUsageManagementService genAIUsageManagementService;

  /** Constructor */
  @Autowired
  public GenAIResumeAdvisorController(
      GenAIResumeAdvisorService genAIResumeAdvisorService,
      GenAIUsageManagementService genAIUsageManagementService) {
    this.genAIResumeAdvisorService = genAIResumeAdvisorService;
    this.genAIUsageManagementService = genAIUsageManagementService;
  }

  /**
   * Resume advisor
   *
   * @param genAIResumeAdvisorRequest Prompt from user
   * @param authentication Authentication with JWT token
   * @return response from GenAI
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> resumeAdvisor(
      @RequestBody GenAIResumeAdvisorRequest genAIResumeAdvisorRequest,
      Authentication authentication) {
    genAIResumeAdvisorRequest.validateRequest();
    UserModel user = (UserModel) authentication.getPrincipal();
    String userID = user.getId();
    String response =
        this.genAIResumeAdvisorService.getAdvice(
            userID,
            genAIResumeAdvisorRequest.getApplicationId(),
            genAIResumeAdvisorRequest.getUserPrompt());
    return ResponseEntity.ok().body(Map.of("response", response));
  }

  /**
   * Get the remaining quota of the user
   *
   * @param authentication contains information about user
   * @return the remaining quota
   */
  @GetMapping("/remaining-quota")
  public ResponseEntity<Map<String, Object>> getRemainingQuota(Authentication authentication) {
    UserModel user = (UserModel) authentication.getPrincipal();
    String userID = user.getId();
    int numberOfRequestLeft = this.genAIUsageManagementService.getNumberOfRequestLeft(userID);
    return ResponseEntity.ok().body(Map.of("remainingQuota", numberOfRequestLeft));
  }
}
