package com.backend.coapp.service;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.dto.response.PaginationResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Getter
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final CompanyRepository companyRepository;
  private final UserRepository userRepository;
  private final MongoTemplate mongoTemplate;

  public ApplicationService(
    ApplicationRepository applicationRepository,
    CompanyRepository companyRepository,
    UserRepository userRepository,
    MongoTemplate mongoTemplate) {
    this.applicationRepository = applicationRepository;
    this.companyRepository = companyRepository;
    this.userRepository = userRepository;
    this.mongoTemplate = mongoTemplate;
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
      throw new CompanyNotFoundException();
    }

    if (this.userRepository.findById(userId).isEmpty()) {
      throw new UserNotFoundException();
    }

    boolean alreadyExists =
        this.applicationRepository.existsByUserIdAndCompanyIdAndJobTitle(
            userId, companyId, jobTitle);

    if (alreadyExists) {
      throw new DuplicateApplicationException(jobTitle, companyId);
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
   * @param userId ID of the user making the update (must own application)
   * @param applicationId ID of the application to update (must exist)
   * @param newCompanyId Updated company ID (nullable)
   * @param newStatus Updated application status (nullable)
   * @param newApplicationDeadline Updated application deadline (nullable)
   * @param newJobTitle Updated job title (nullable)
   * @param newJobDescription Updated job description (nullable)
   * @param newNumPositions Updated number of positions (nullable)
   * @param newSourceLink Updated source link (nullable)
   * @param newDateApplied Updated date applied (nullable)
   * @param newNotes Updated notes (nullable)
   * @return ApplicationResponse DTO containing updated application data
   * @throws ApplicationNotFoundException If application doesn't exist
   * @throws UnauthorizedApplicationAccessException If user doesn't own application
   * @throws NoChangesDetectedException If no modification fields changed
   * @throws CompanyNotFoundException If new company ID doesn't exist
   */
  public ApplicationResponse updateApplication(
      String userId,
      String applicationId,
      String newCompanyId,
      String newJobTitle,
      ApplicationStatus newStatus,
      LocalDate newApplicationDeadline,
      String newJobDescription,
      Integer newNumPositions,
      String newSourceLink,
      LocalDate newDateApplied,
      String newNotes) {

    ApplicationModel existingApp =
        this.applicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException());

    if (!existingApp.getUserId().equals(userId)) {
      throw new UnauthorizedApplicationAccessException(
          "You do not have permission to edit this application.");
    }

    boolean companyChanged = !Objects.equals(existingApp.getCompanyId(), newCompanyId);
    boolean titleChanged = !Objects.equals(existingApp.getJobTitle(), newJobTitle);
    boolean statusChanged = !Objects.equals(existingApp.getStatus(), newStatus);
    boolean deadlineChanged =
        !Objects.equals(existingApp.getApplicationDeadline(), newApplicationDeadline);
    boolean descChanged = !Objects.equals(existingApp.getJobDescription(), newJobDescription);
    boolean positionsChanged = !Objects.equals(existingApp.getNumPositions(), newNumPositions);
    boolean linkChanged = !Objects.equals(existingApp.getSourceLink(), newSourceLink);
    boolean dateAppliedChanged = !Objects.equals(existingApp.getDateApplied(), newDateApplied);
    boolean notesChanged = !Objects.equals(existingApp.getNotes(), newNotes);

    if (!companyChanged
        && !titleChanged
        && !descChanged
        && !linkChanged
        && !dateAppliedChanged
        && !notesChanged
        && !statusChanged
        && !deadlineChanged
        && !positionsChanged) {
      throw new NoChangesDetectedException("No fields were changed.");
    }

    if (companyChanged && this.companyRepository.findById(newCompanyId).isEmpty()) {
      throw new CompanyNotFoundException();
    }

    existingApp.setCompanyId(newCompanyId);
    existingApp.setJobTitle(newJobTitle);
    existingApp.setStatus(newStatus);
    existingApp.setApplicationDeadline(newApplicationDeadline);
    existingApp.setJobDescription(newJobDescription);
    existingApp.setNumPositions(newNumPositions);
    existingApp.setSourceLink(newSourceLink);
    existingApp.setDateApplied(newDateApplied);
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
            .orElseThrow(() -> new ApplicationNotFoundException());

    if (!existingApp.getUserId().equals(userId)) {
      throw new UnauthorizedApplicationAccessException(
          "You do not have permission to delete this application.");
    }

    this.applicationRepository.deleteById(applicationId);
  }

  /**
   * Retrieves all job applications for a specific user.
   *
   * @param userId The ID of the user.
   * @return A list of ApplicationResponse DTOs.
   */
  public List<ApplicationResponse> getApplications(String userId) {
    List<ApplicationModel> userApplicationModels = this.applicationRepository.findByUserId(userId);

    List<ApplicationResponse> userApplications = new ArrayList<>();
    for (ApplicationModel application : userApplicationModels) {
      userApplications.add(ApplicationResponse.fromModel(application));
    }

    return userApplications;
  }

  /**
   * Retrieves a paginated, optionally filtered and sorted list of applications for a user.
   * <p>
   * Search is performed against company name (case-insensitive, partial match).
   * Because the model only stores a company id, matching company IDs are resolved from the companies
   * collection and then used in a filter. If the search matches no companies the result will be empty.
   *
   * @param userId The ID of the authenticated user, always applied as a filter
   * @param search Optional company name search term (case-insensitive, partial match)
   * @param statuses Optional list of status values to include
   * @param sortBy Field to sort by
   * @param sortOrder Sort direction: asc/desc (default desc)
   * @param page page number
   * @param size results per page
   * @return Map with keys "applications"(List) and "pagination" (Map)
   * @throws ApplicationServiceFailException If the database query fails
   */
  public Map<String, Object> getFilteredApplications(
      String userId,
      String search,
      List<ApplicationStatus> statuses,
      String sortBy,
      String sortOrder,
      int page,
      int size) {

    try {
      // use criteria to build the mongo query dynamically with .where() or .and() calls
      Criteria criteria = Criteria.where("userId").is(userId);

      // search for companies using the search string and get their ids
      // if no companies match the search, force an empty result with an "impossible" filter.
      if (search != null && !search.isBlank()) {
        List<String> matchingCompanyIds =
          this.companyRepository.findByCompanyNameContainingIgnoreCase(search.trim()).stream()
            .map(CompanyModel::getId)
            .collect(Collectors.toList());

        // add ids to the query criteria filter
        criteria = criteria.and("companyId").in(matchingCompanyIds);
      }

      // add filter for statuses if given
      if (statuses != null && !statuses.isEmpty()) {
        criteria = criteria.and("status").in(statuses);
      }

      // get direction, default to descending
      // validation has already ensured sortOrder is either asc or desc
      Sort.Direction direction =
        "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
      // sort by the specified field and direction
      Sort sort = Sort.by(direction, sortBy);

      // total matching documents for pagination
      long totalItems = mongoTemplate.count(new Query(criteria), ApplicationModel.class);

      // get the actual paged results
      // skip is used to determine which page to fetch
      List<ApplicationModel> applications =
        mongoTemplate.find(
          new Query(criteria).with(sort).skip((long) page * size).limit(size),
          ApplicationModel.class);

      // map model objects to DTOs
      List<ApplicationResponse> applicationResponses = new ArrayList<>();
      for (ApplicationModel application : applications) {
        applicationResponses.add(ApplicationResponse.fromModel(application));
      }

      // calculate total pages
      // simpler to do manually in this case, as using the spring data page class is a lot of work when building
      // queries in this way
      int totalPages = (totalItems == 0) ? 0 : (int) Math.ceil((double) totalItems / size);
      PaginationResponse pagination =
        new PaginationResponse(
          page, totalPages, totalItems, size, page < totalPages - 1, page > 0);

      // get actual response mapping
      return Map.of(
        "applications", applicationResponses,
        "pagination", pagination.toMap());

    } catch (Exception e) {
      log.error("Failed to retrieve applications for user {}: {}", userId, e.getMessage());
      throw new ApplicationServiceFailException(
        "Failed to retrieve applications. Please try again.");
    }
  }
}
