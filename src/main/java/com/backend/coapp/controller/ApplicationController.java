package com.backend.coapp.controller;

import com.backend.coapp.dto.request.CreateApplicationRequest;
import com.backend.coapp.dto.request.GetApplicationsRequest;
import com.backend.coapp.dto.request.GetInterviewApplicationsRequest;
import com.backend.coapp.dto.request.UpdateApplicationRequest;
import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.service.ApplicationService;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Getter
@RequestMapping("/api/application")
public class ApplicationController {

  private final ApplicationService applicationService;

  @Autowired
  public ApplicationController(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  /**
   * Create a new application
   *
   * @param applicationRequest CreateApplicationRequest DTO
   * @param authentication authentication object
   * @return ResponseEntity with the information regarding the created application
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createApplication(
      @RequestBody CreateApplicationRequest applicationRequest, Authentication authentication) {

    applicationRequest.validateRequest();

    UserModel user = (UserModel) authentication.getPrincipal();
    String userId = user.getId();

    ApplicationResponse application =
        this.applicationService.createApplication(
            userId,
            applicationRequest.getCompanyId(),
            applicationRequest.getJobTitle(),
            applicationRequest.getStatus(),
            applicationRequest.getApplicationDeadline(),
            applicationRequest.getJobDescription(),
            applicationRequest.getNumPositions(),
            applicationRequest.getSourceLink(),
            applicationRequest.getDateApplied(),
            applicationRequest.getNotes(),
            applicationRequest.getInterviewDate());

    return ResponseEntity.status(HttpStatus.CREATED).body(application.toMap());
  }

  /**
   * Update specific fields of an existing application
   *
   * @param applicationId The id of the application to update
   * @param applicationRequest UpdateApplicationRequest DTO
   * @param authentication authentication object
   * @return ResponseEntity with updated application data
   */
  @PutMapping("/{applicationId}")
  public ResponseEntity<Map<String, Object>> updateApplication(
      @PathVariable String applicationId,
      @RequestBody UpdateApplicationRequest applicationRequest,
      Authentication authentication) {

    applicationRequest.validateRequest();

    UserModel user = (UserModel) authentication.getPrincipal();
    String userId = user.getId();

    ApplicationResponse application =
        this.applicationService.updateApplication(
            userId,
            applicationId,
            applicationRequest.getCompanyId(),
            applicationRequest.getJobTitle(),
            applicationRequest.getStatus(),
            applicationRequest.getApplicationDeadline(),
            applicationRequest.getJobDescription(),
            applicationRequest.getNumPositions(),
            applicationRequest.getSourceLink(),
            applicationRequest.getDateApplied(),
            applicationRequest.getNotes(),
            applicationRequest.getInterviewDate());

    return ResponseEntity.ok(application.toMap());
  }

  /**
   * Delete an existing application
   *
   * @param applicationId The id of the application to delete
   * @param authentication authentication object
   * @return ResponseEntity with a success message
   */
  @DeleteMapping("/{applicationId}")
  public ResponseEntity<Map<String, Object>> deleteApplication(
      @PathVariable String applicationId, Authentication authentication) {

    UserModel user = (UserModel) authentication.getPrincipal();
    String userId = user.getId();

    this.applicationService.deleteApplication(applicationId, userId);

    return ResponseEntity.ok(Map.of("message", "Application successfully deleted."));
  }

  /**
   * Get a paginated list of applications with optional filtering and sorting. all query params are
   * optional and can be combined freely.
   *
   * @param applicationRequest query params: search, status, sortBy, sortOrder, page, size
   * @return 200 with applications and pagination unless there is an error.
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getApplications(
      @ModelAttribute GetApplicationsRequest applicationRequest) {

    applicationRequest.validateRequest();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = auth.getName();

    Map<String, Object> response =
        this.applicationService.getFilteredApplications(
            userId,
            applicationRequest.getSearch(),
            applicationRequest.getParsedStatuses(),
            applicationRequest.getSortBy(),
            applicationRequest.getSortOrder(),
            applicationRequest.getPage(),
            applicationRequest.getSize());

    return ResponseEntity.ok(response);
  }

  /**
   */
  @GetMapping("/interviews")
  public ResponseEntity<List<ApplicationModel>> getInterviewApplications(
      @ModelAttribute GetInterviewApplicationsRequest interviewApplicationsRequest) {

    interviewApplicationsRequest.validateRequest();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = auth.getName();

    List<ApplicationModel> response =
        this.applicationService.getInterviewApplications(
            userId,
            interviewApplicationsRequest.getStartDate(),
            interviewApplicationsRequest.getEndDate());

    return ResponseEntity.ok(response);
  }
}
