package com.backend.coapp.service;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Getter // For testing only
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final CompanyRepository companyRepository;
  private final UserRepository userRepository;

  public ApplicationService(
      ApplicationRepository applicationRepository,
      CompanyRepository companyRepository,
      UserRepository userRepository) {
    this.applicationRepository = applicationRepository;
    this.companyRepository = companyRepository;
    this.userRepository = userRepository;
  }

  /**
   * Create a new application
   *
   * @param userId userId of the user applying for the job
   * @param companyId companyId of the company applying for
   * @param jobTitle job title of the position
   * @param status status of the application (e.g., INTERVIEWING)
   * @param applicationDeadline deadline for the application
   * @param jobDescription information about the job
   * @param numPositions number of positions available
   * @param sourceLink link to the job posting
   * @param dateApplied date the application was submitted
   * @param notes notes regarding the application/job
   * @return ApplicationResponse DTO
   */
  public ApplicationResponse createApplication(
      String userId,
      String companyId,
      String jobTitle,
      ApplicationStatus status,
      LocalDate applicationDeadline,
      String jobDescription,
      Integer numPositions,
      String sourceLink,
      LocalDate dateApplied,
      String notes) {
    if (this.companyRepository.findById(companyId).isEmpty()) {
      throw new CompanyNotFoundException(companyId);
    }

    if (this.userRepository.findById(userId).isEmpty()) {
      throw new UserNotFoundException(userId);
    }

    boolean alreadyExists =
        this.applicationRepository.existsByUserIdAndCompanyIdAndJobTitle(
            userId, companyId, jobTitle);

    if (alreadyExists) {
      throw new DuplicateApplicationException(userId, jobTitle, companyId);
    }

    try {
      ApplicationModel applicationModel =
          ApplicationModel.builder()
              .userId(userId)
              .companyId(companyId)
              .jobTitle(jobTitle)
              .status(status)
              .applicationDeadline(applicationDeadline)
              .jobDescription(jobDescription)
              .numPositions(numPositions)
              .sourceLink(sourceLink)
              .dateApplied(dateApplied)
              .notes(notes)
              .build();

      ApplicationModel savedApplication = this.applicationRepository.save(applicationModel);
      return ApplicationResponse.fromModel(savedApplication);

    } catch (Exception e) {
      log.error("Failed to create application for user {}: {}", userId, e.getMessage());
      throw new ApplicationServiceFailException("Failed to create application. Please try again.");
    }
  }

  public ApplicationResponse updateApplication(
      String applicationId,
      String userId,
      String newCompanyId,
      String newJobTitle,
      String newJobDescription,
      String newSourceLink,
      String newNotes) {
    // 1. Find Application
    ApplicationModel existingApp =
        this.applicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

    // 2. Verify Ownership
    if (!existingApp.getUserId().equals(userId)) {
      throw new UnauthorizedApplicationAccessException(
          "You do not have permission to edit this application.");
    }

    // 3. Check for Changes
    boolean companyChanged = !Objects.equals(existingApp.getCompanyId(), newCompanyId);
    boolean titleChanged = !Objects.equals(existingApp.getJobTitle(), newJobTitle);
    boolean descChanged = !Objects.equals(existingApp.getJobDescription(), newJobDescription);
    boolean linkChanged = !Objects.equals(existingApp.getSourceLink(), newSourceLink);

    if (!companyChanged && !titleChanged && !descChanged && !linkChanged) {
      throw new NoChangesDetectedException("No fields were changed.");
    }

    // 4. Validate new company exists (if it changed)
    if (companyChanged && this.companyRepository.findById(newCompanyId).isEmpty()) {
      throw new CompanyNotFoundException(newCompanyId);
    }

    // 5. Apply Updates
    existingApp.setCompanyId(newCompanyId);
    existingApp.setJobTitle(newJobTitle);
    existingApp.setJobDescription(newJobDescription);
    existingApp.setSourceLink(newSourceLink);
    existingApp.setNotes(newNotes);

    ApplicationModel updatedApp = this.applicationRepository.save(existingApp);
    return ApplicationResponse.fromModel(updatedApp);
  }

  /** Deletes an application after verifying ownership. */
  public void deleteApplication(String applicationId, String userId) {
    ApplicationModel existingApp =
        this.applicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

    if (!existingApp.getUserId().equals(userId)) {
      throw new UnauthorizedApplicationAccessException(
          "You do not have permission to delete this application.");
    }

    this.applicationRepository.deleteById(applicationId);
  }
}
