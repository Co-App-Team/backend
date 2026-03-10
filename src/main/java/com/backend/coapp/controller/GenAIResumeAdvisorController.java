package com.backend.coapp.controller;

import com.backend.coapp.dto.request.CreateReviewRequest;
import com.backend.coapp.dto.request.GenAIResumeAdvisorRequest;
import com.backend.coapp.dto.request.UpdatePasswordWithOldPasswordRequest;
import com.backend.coapp.service.GenAIResumeAdvisorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@Getter // For testing only
@RequestMapping("/api/resume-ai-advisor")
public class GenAIResumeAdvisorController {
    /** Singleton service and repository * */
    private final GenAIResumeAdvisorService genAIResumeAdvisorService;

    /** Constructor */
    @Autowired
    public GenAIResumeAdvisorController(GenAIResumeAdvisorService genAIResumeAdvisorService) {
        this.genAIResumeAdvisorService = genAIResumeAdvisorService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> resumeAdvisor(@RequestBody GenAIResumeAdvisorRequest genAIResumeAdvisorRequest) {
        genAIResumeAdvisorRequest.validateRequest();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userID = auth.getName();
        String response = this.genAIResumeAdvisorService.getAdvice(userID,genAIResumeAdvisorRequest.getApplicationId(), genAIResumeAdvisorRequest.getUserPrompt());
        return ResponseEntity.ok().body(Map.of("response",response));
    }
}
