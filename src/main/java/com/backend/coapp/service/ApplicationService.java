package com.backend.coapp.service;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.dto.response.PaginationResponse;
import com.backend.coapp.exception.application.*;
import com.backend.coapp.exception.company.CompanyNotFoundException;
import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
   * @param interviewDateTime Date of interview (nullable)
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
      String notes,
      LocalDateTime interviewDateTime) {
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
              .interviewDateTime(interviewDateTime)
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
   * @param newInterviewDateTime Updated interview date (nullable)
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
      String newNotes,
      LocalDateTime newInterviewDateTime) {

    ApplicationModel existingApp =
        this.applicationRepository
            .findById(applicationId)
            .orElseThrow(ApplicationNotFoundException::new);

    if (!existingApp.getUserId().equals(userId)) {
      throw new UnauthorizedApplicationAccessException(
          "You do not have permission to edit this application.");
    }

    boolean hasChanges =
        !Objects.equals(existingApp.getCompanyId(), newCompanyId)
            || !Objects.equals(existingApp.getJobTitle(), newJobTitle)
            || !Objects.equals(existingApp.getStatus(), newStatus)
            || !Objects.equals(existingApp.getApplicationDeadline(), newApplicationDeadline)
            || !Objects.equals(existingApp.getJobDescription(), newJobDescription)
            || !Objects.equals(existingApp.getNumPositions(), newNumPositions)
            || !Objects.equals(existingApp.getSourceLink(), newSourceLink)
            || !Objects.equals(existingApp.getDateApplied(), newDateApplied)
            || !Objects.equals(existingApp.getNotes(), newNotes)
            || !Objects.equals(existingApp.getInterviewDateTime(), newInterviewDateTime);

    if (!hasChanges) {
      throw new NoChangesDetectedException("No fields were changed.");
    }

    if (!Objects.equals(existingApp.getCompanyId(), newCompanyId)
        && this.companyRepository.findById(newCompanyId).isEmpty()) {
      throw new CompanyNotFoundException();
    }

    // Process logic for status transitions and date constraints
    validateAndSyncStatusDates(existingApp, newStatus, newDateApplied, newInterviewDateTime);

    existingApp.setCompanyId(newCompanyId);
    existingApp.setJobTitle(newJobTitle);
    existingApp.setStatus(newStatus);
    existingApp.setApplicationDeadline(newApplicationDeadline);
    existingApp.setJobDescription(newJobDescription);
    existingApp.setNumPositions(newNumPositions);
    existingApp.setSourceLink(newSourceLink);
    existingApp.setNotes(newNotes);

    ApplicationModel updatedApp = this.applicationRepository.save(existingApp);
    return ApplicationResponse.fromModel(updatedApp);
  }

  /**
   * Validates and synchronizes application dates based on status changes
   *
   * @param existingApp The existing application model
   * @param newStatus The proposed new status
   * @param newDateApplied The proposed applied date
   * @param newInterviewDateTime The proposed interview date
   * @throws InvalidRequestException If dates violate business logic
   */
  private void validateAndSyncStatusDates(
      ApplicationModel existingApp,
      ApplicationStatus newStatus,
      LocalDate newDateApplied,
      LocalDateTime newInterviewDateTime) {

    boolean statusChanged = !Objects.equals(existingApp.getStatus(), newStatus);

    if (statusChanged) {
      if (newStatus == ApplicationStatus.APPLIED) {
        newDateApplied = LocalDate.now();
      } else if (newStatus == ApplicationStatus.NOT_APPLIED) {
        newDateApplied = null;
      }

      boolean wasInterviewing =
          existingApp.getStatus() == ApplicationStatus.INTERVIEWING
              || existingApp.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED;
      boolean isReverting =
          newStatus == ApplicationStatus.NOT_APPLIED || newStatus == ApplicationStatus.APPLIED;

      if (wasInterviewing && isReverting) {
        newInterviewDateTime = null;
      }
    }

    if (newDateApplied != null) {
      LocalDate deadline = existingApp.getApplicationDeadline();
      if (deadline != null && newDateApplied.isAfter(deadline)) {
        throw new InvalidRequestException(
            "The applied date must be before the application deadline."
                + deadline
                + " "
                + newDateApplied);
      }
    }

    existingApp.setDateApplied(newDateApplied);
    existingApp.setInterviewDateTime(newInterviewDateTime);
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
            .orElseThrow(ApplicationNotFoundException::new);

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
   *
   * <p>Search is performed against company name (case-insensitive, partial match). Because the
   * model only stores a company id, matching company IDs are resolved from the companies collection
   * and then used in a filter. If the search matches no companies the result will be empty.
   *
   * @param userId The ID of the authenticated user, always applied as a filter
   * @param search Optional company name search term (case-insensitive, partial match)
   * @param statuses Optional list of status values to include
   * @param sortBy Field to sort by
   * @param sortOrder Sort direction: asc/desc (default desc)
   * @param page page number
   * @param size results per page
   * @return Map with keys "applications" (List) and "pagination" (Map)
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
      Criteria criteria = buildCriteria(userId, search, statuses);
      Sort sort = buildSort(sortBy, sortOrder);

      long totalItems = mongoTemplate.count(new Query(criteria), ApplicationModel.class);
      List<ApplicationModel> applications = fetchApplications(criteria, sort, page, size);

      List<ApplicationResponse> applicationResponses = mapToResponses(applications);
      PaginationResponse pagination = buildPagination(page, size, totalItems);

      return Map.of("applications", applicationResponses, "pagination", pagination.toMap());

    } catch (Exception e) {
      log.error("Failed to retrieve applications for user {}: {}", userId, e.getMessage());
      throw new ApplicationServiceFailException(
          "Failed to retrieve applications. Please try again.");
    }
  }

  /**
   * Builds the MongoDB query criteria from user filters. Always filters by userId. optionally
   * filters by company name search and status list.
   */
  private Criteria buildCriteria(String userId, String search, List<ApplicationStatus> statuses) {
    Criteria criteria = Criteria.where("userId").is(userId);

    if (search != null && !search.isBlank()) {
      List<String> matchingCompanyIds = resolveCompanyIds(search);
      criteria = criteria.and("companyId").in(matchingCompanyIds);
    }

    if (statuses != null && !statuses.isEmpty()) {
      criteria = criteria.and("status").in(statuses);
    }

    return criteria;
  }

  /**
   * Resolves a list of company IDs matching the given search term (case-insensitive, partial
   * match). Returns an empty list if no companies match, which will force an empty query result.
   */
  private List<String> resolveCompanyIds(String search) {
    return this.companyRepository.findByCompanyNameContainingIgnoreCase(search.trim()).stream()
        .map(CompanyModel::getId)
        .toList();
  }

  /**
   * Builds a Sort from the given field and direction string. Defaults to descending if sortOrder is
   * not "asc".
   */
  private Sort buildSort(String sortBy, String sortOrder) {
    // validation has already ensured sortOrder is either asc or desc
    Sort.Direction direction =
        "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
    return Sort.by(direction, sortBy);
  }

  /** Executes the paginated MongoDB query and returns the matching ApplicationModel documents. */
  private List<ApplicationModel> fetchApplications(
      Criteria criteria, Sort sort, int page, int size) {
    return mongoTemplate.find(
        new Query(criteria).with(sort).skip((long) page * size).limit(size),
        ApplicationModel.class);
  }

  /** Maps a list of ApplicationModel documents to ApplicationResponse DTOs. */
  private List<ApplicationResponse> mapToResponses(List<ApplicationModel> applications) {
    return applications.stream().map(ApplicationResponse::fromModel).toList();
  }

  /** Constructs the PaginationResponse from current page state and total item count. */
  private PaginationResponse buildPagination(int page, int size, long totalItems) {
    // simpler to do manually in this case, as using the spring data page class is a lot of work
    // when building queries in this way.
    int totalPages = (totalItems == 0) ? 0 : (int) Math.ceil((double) totalItems / size);
    return new PaginationResponse(
        page, totalPages, totalItems, size, page < totalPages - 1, page > 0);
  }

  /**
   * Retrieves a list of applications for a user that are currently in the interview stage.
   *
   * <p>Filters applications where {@code interviewDateTime} exists. If both startDate and endDate
   * are provided, results are further filtered to include only interviews within that specific date
   * range.
   *
   * @param userId The ID of the user whose applications are being retrieved
   * @param startDate Optional start date to filter interview dates (inclusive)
   * @param endDate Optional end date to filter interview dates (inclusive)
   * @return A list of {@link ApplicationResponse} objects representing the matching applications
   * @throws ApplicationServiceFailException If the database query fails
   */
  public List<ApplicationResponse> getInterviewApplications(
      String userId, LocalDate startDate, LocalDate endDate) {
    Criteria criteria = Criteria.where("userId").is(userId).and("interviewDateTime").exists(true);

    if (startDate != null && endDate != null) {
      criteria.gte(startDate).lte(endDate);
    }

    List<ApplicationModel> applicationModels =
        mongoTemplate.find(new Query(criteria), ApplicationModel.class);

    return applicationModels.stream().map(ApplicationResponse::fromModel).toList();
  }
}
