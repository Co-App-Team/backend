package com.backend.coapp.controller;

import com.backend.coapp.service.GenAIResumeAdvisorService;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Getter // For testing only
@RequestMapping("/api/resume-advisor")
public class GenAIResumeAdvisorController {

  private final GenAIResumeAdvisorService genAIResumeAdvisorService;

  @Autowired
  public GenAIResumeAdvisorController(GenAIResumeAdvisorService genAIResumeAdvisorService) {
    this.genAIResumeAdvisorService = genAIResumeAdvisorService;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getDummyUser(@RequestBody String prompt) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userID = auth.getName();

    String response = this.genAIResumeAdvisorService.getAdvice(userID, null, prompt);
    return ResponseEntity.ok(Map.of("response", response));
  }
}
