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
@Getter
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
   * Create a new job application
   *
   * @param userId ID of the applying user (must exist)
   * @param companyId ID of the target company (must exist)
   * @param jobTitle Title of the job position (non-null)
   * @param status Application status (e.g., INTERVIEWING)
   * @param applicationDeadline Deadline for application submission (nullable)
   * @param jobDescription Job description details (nullable)
   * @param numPositions Number of available positions (nullable)
   * @param sourceLink URL to job posting (nullable)
   * @param dateApplied Date application was submitted (non-null)
   * @param notes Additional notes (nullable)
   * @return ApplicationResponse DTO containing created application data
   * @throws CompanyNotFoundException If company doesn't exist
   * @throws UserNotFoundException If user doesn't exist
   * @throws DuplicateApplicationException If identical application exists
   * @throws ApplicationServiceFailException If persistence fails
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

  /**
   * Update an existing job application
   *
   * @param applicationId ID of the application to update (must exist)
   * @param userId ID of the requesting user (must own application)
   * @param newCompanyId Updated company ID (must exist if changed)
   * @param newJobTitle Updated job title
   * @param newJobDescription Updated job description (nullable)
   * @param newSourceLink Updated source link (nullable)
   * @param newNotes Updated notes (nullable)
   * @return ApplicationResponse DTO containing updated application data
   * @throws ApplicationNotFoundException If application doesn't exist
   * @throws UnauthorizedApplicationAccessException If user doesn't own application
   * @throws NoChangesDetectedException If no modification fields changed
   * @throws CompanyNotFoundException If new company ID doesn't exist
   */
  public ApplicationResponse updateApplication(
      String applicationId,
      String userId,
      String newCompanyId,
      String newJobTitle,
      String newJobDescription,
      String newSourceLink,
      String newNotes) {

    ApplicationModel existingApp =
        this.applicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

    if (!existingApp.getUserId().equals(userId)) {
      throw new UnauthorizedApplicationAccessException(
          "You do not have permission to edit this application.");
    }

    boolean companyChanged = !Objects.equals(existingApp.getCompanyId(), newCompanyId);
    boolean titleChanged = !Objects.equals(existingApp.getJobTitle(), newJobTitle);
    boolean descChanged = !Objects.equals(existingApp.getJobDescription(), newJobDescription);
    boolean linkChanged = !Objects.equals(existingApp.getSourceLink(), newSourceLink);

    if (!companyChanged && !titleChanged && !descChanged && !linkChanged) {
      throw new NoChangesDetectedException("No fields were changed.");
    }

    if (companyChanged && this.companyRepository.findById(newCompanyId).isEmpty()) {
      throw new CompanyNotFoundException(newCompanyId);
    }

    existingApp.setCompanyId(newCompanyId);
    existingApp.setJobTitle(newJobTitle);
    existingApp.setJobDescription(newJobDescription);
    existingApp.setSourceLink(newSourceLink);
    existingApp.setNotes(newNotes);

    ApplicationModel updatedApp = this.applicationRepository.save(existingApp);
    return ApplicationResponse.fromModel(updatedApp);
  }

  /**
   * Delete a job application
   *
   * @param applicationId ID of application to delete (must exist)
   * @param userId ID of requesting user (must own application)
   * @throws ApplicationNotFoundException If application doesn't exist
   * @throws UnauthorizedApplicationAccessException If ownership validation fails
   */
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
