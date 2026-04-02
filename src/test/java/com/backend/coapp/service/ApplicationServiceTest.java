package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.exception.application.*;
import com.backend.coapp.exception.company.CompanyNotFoundException;
import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class ApplicationServiceTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private CompanyRepository companyRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private MongoTemplate mongoTemplate;

  private ApplicationService applicationService;

  private ApplicationRepository mockAppRepo;
  private CompanyRepository mockCompRepo;
  private UserRepository mockUserRepo;
  private MongoTemplate mockMongoTemplate;

  private CompanyModel testCompany;
  private UserModel testUser;
  private ApplicationModel existingApp;

  private final LocalDate date = LocalDate.of(2800, 1, 1);
  private final LocalDateTime datetime = LocalDateTime.of(2800, 1, 1, 12, 0, 0);

  @BeforeEach
  void setUp() {
    // Clear DB for Integration Tests
    this.applicationRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.userRepository.deleteAll();

    // Setup Real Data for Integration Tests
    this.testCompany = new CompanyModel("Google", "Mountain View", "https://google.com");
    this.companyRepository.save(testCompany);

    this.testUser =
        new UserModel("user_001", "test@example.com", "password123", "John", "Doe", true, 1234);
    this.userRepository.save(testUser);

    // This app has status APPLIED but contains an interviewDateTime.
    // Under the new logic, it SHOULD be returned by getInterviewApplications.
    this.existingApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(date)
            .interviewDateTime(datetime)
            .build();
    this.applicationRepository.save(existingApp);

    // Service with real repositories and MongoTemplate for Integration Tests
    this.applicationService =
        new ApplicationService(
            applicationRepository, companyRepository, userRepository, mongoTemplate);

    // Mocks for Unit Tests
    this.mockAppRepo = Mockito.mock(ApplicationRepository.class);
    this.mockCompRepo = Mockito.mock(CompanyRepository.class);
    this.mockUserRepo = Mockito.mock(UserRepository.class);
    this.mockMongoTemplate = Mockito.mock(MongoTemplate.class);
  }

  // INTEGRATION TESTS (Using Testcontainers)

  @Test
  void constructor_expectSameInitInstance() {
    assertSame(this.applicationRepository, this.applicationService.getApplicationRepository());
    assertSame(this.companyRepository, this.applicationService.getCompanyRepository());
    assertSame(this.mongoTemplate, this.applicationService.getMongoTemplate());
  }

  // Create Application Integration Tests

  @Test
  void createApplication_whenValid_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.createApplication(
            testUser.getId(),
            testCompany.getId(),
            "Data Scientist",
            ApplicationStatus.APPLIED,
            date,
            "Desc",
            1,
            "https://link.com",
            date,
            "Notes",
            datetime);

    assertNotNull(response);
    assertEquals("Data Scientist", response.getJobTitle());
    assertEquals(testCompany.getId(), response.getCompanyId());
  }

  @Test
  void createApplication_whenCompanyNotFound_expectException() {
    assertThrows(
        CompanyNotFoundException.class,
        () ->
            this.applicationService.createApplication(
                "user_001",
                "invalid_id",
                "Title",
                ApplicationStatus.APPLIED,
                date,
                null,
                1,
                null,
                null,
                null,
                datetime));
  }

  @Test
  void createApplication_whenUserNotFound_expectException() {
    String companyId = testCompany.getId();
    assertThrows(
        UserNotFoundException.class,
        () ->
            this.applicationService.createApplication(
                "invalid_user",
                companyId,
                "Title",
                ApplicationStatus.APPLIED,
                date,
                null,
                1,
                null,
                null,
                null,
                datetime));
  }

  @Test
  void createApplication_whenDuplicate_expectException() {
    String companyId = testCompany.getId();
    assertThrows(
        DuplicateApplicationException.class,
        () ->
            this.applicationService.createApplication(
                "user_001",
                companyId,
                "Software Engineer",
                ApplicationStatus.APPLIED,
                date,
                null,
                1,
                null,
                null,
                null,
                datetime));
  }

  // Update Application Integration Tests

  @Test
  void updateApplication_whenValid_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Senior SE",
            ApplicationStatus.INTERVIEWING,
            date,
            "New Desc",
            2,
            "https://new.link",
            date,
            "New Notes",
            datetime);

    assertEquals("Senior SE", response.getJobTitle());
    assertEquals("New Desc", response.getJobDescription());
    assertEquals("New Notes", response.getNotes());
    assertEquals(ApplicationStatus.INTERVIEWING, response.getStatus());
  }

  @Test
  void updateApplication_whenNotOwner_expectUnauthorized() {
    String appId = existingApp.getId();
    String companyId = testCompany.getId();
    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () ->
            this.applicationService.updateApplication(
                "wrong_user",
                appId,
                companyId,
                "Title",
                ApplicationStatus.APPLIED,
                date,
                "Desc",
                1,
                "Link",
                date,
                "Notes",
                datetime));
  }

  @Test
  void updateApplication_whenNoChanges_expectException() {
    String appId = existingApp.getId();
    String companyId = testCompany.getId();
    assertThrows(
        NoChangesDetectedException.class,
        () ->
            this.applicationService.updateApplication(
                "user_001",
                appId,
                companyId,
                "Software Engineer",
                ApplicationStatus.APPLIED,
                date,
                null,
                null,
                null,
                null,
                null,
                datetime));
  }

  @Test
  void updateApplication_whenAppNotFound_expectException() {
    assertThrows(
        ApplicationNotFoundException.class,
        () ->
            this.applicationService.updateApplication(
                "user_001",
                "invalid_app_id",
                "c1",
                "t",
                ApplicationStatus.APPLIED,
                date,
                "d",
                1,
                "l",
                date,
                "n",
                datetime));
  }

  @Test
  void updateApplication_whenNewCompanyNotFound_expectException() {
    String appId = existingApp.getId();
    assertThrows(
        CompanyNotFoundException.class,
        () ->
            this.applicationService.updateApplication(
                "user_001",
                appId,
                "non_existent_company",
                "Title",
                ApplicationStatus.APPLIED,
                date,
                "Desc",
                1,
                "Link",
                date,
                "Notes",
                datetime));
  }

  // Delete Application Integration Tests

  @Test
  void deleteApplication_whenValid_expectDeleted() {
    assertDoesNotThrow(
        () -> this.applicationService.deleteApplication(existingApp.getId(), "user_001"));
    assertTrue(this.applicationRepository.findById(existingApp.getId()).isEmpty());
  }

  @Test
  void deleteApplication_whenNotOwner_expectUnauthorized() {
    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () -> this.applicationService.deleteApplication(existingApp.getId(), "malicious_user"));
  }

  @Test
  void deleteApplication_whenApplicationNotFound_expectException() {
    assertThrows(
        ApplicationNotFoundException.class,
        () -> this.applicationService.deleteApplication("non_existent_id", "user_001"));
  }

  // Get Filtered Applications Tests

  @Test
  void getFilteredApplications_whenNoFilters_expectAllUserApplications() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertNotNull(applications);
    assertEquals(1, applications.size());
  }

  @Test
  void getFilteredApplications_whenSearchMatchesCompany_expectResults() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", "Goo", null, "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(1, applications.size());
  }

  @Test
  void getFilteredApplications_whenSearchMatchesCaseInsensitive_expectResults() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", "google", null, "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(1, applications.size());
  }

  @Test
  void getFilteredApplications_whenSearchMatchesNoCompany_expectEmptyList() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", "nonexistent", null, "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertTrue(applications.isEmpty());
  }

  @Test
  void getFilteredApplications_whenStatusMatches_expectFilteredResults() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(1, applications.size());
  }

  @Test
  void getFilteredApplications_whenStatusDoesNotMatch_expectEmptyList() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.REJECTED), "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertTrue(applications.isEmpty());
  }

  @Test
  void getFilteredApplications_whenMultipleStatuses_expectMatchingResults() {
    ApplicationModel rejectedApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("Product Manager")
            .status(ApplicationStatus.REJECTED)
            .applicationDeadline(date)
            .interviewDateTime(datetime)
            .build();
    this.applicationRepository.save(rejectedApp);

    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001",
            null,
            List.of(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED),
            "dateApplied",
            "desc",
            0,
            20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(2, applications.size());
  }

  @Test
  void getFilteredApplications_whenOtherUserHasApps_expectOnlyOwnApplications() {
    ApplicationModel otherApp =
        ApplicationModel.builder()
            .userId("other_user")
            .companyId(testCompany.getId())
            .jobTitle("Designer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(date)
            .interviewDateTime(datetime)
            .build();
    this.applicationRepository.save(otherApp);

    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(1, applications.size());
  }

  @Test
  void getFilteredApplications_whenPaginated_expectCorrectPage() {
    for (int i = 0; i < 4; i++) {
      this.applicationRepository.save(
          ApplicationModel.builder()
              .userId("user_001")
              .companyId(testCompany.getId())
              .jobTitle("Role " + i)
              .status(ApplicationStatus.APPLIED)
              .applicationDeadline(date)
              .interviewDateTime(datetime)
              .build());
    }

    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 3);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(3, applications.size());
  }

  @Test
  void getFilteredApplications_whenPaginated_expectCorrectPaginationMetadata() {
    // Add 4 more apps so we have 5 total
    for (int i = 0; i < 4; i++) {
      this.applicationRepository.save(
          ApplicationModel.builder()
              .userId("user_001")
              .companyId(testCompany.getId())
              .jobTitle("Role " + i)
              .status(ApplicationStatus.APPLIED)
              .applicationDeadline(date)
              .interviewDateTime(datetime)
              .build());
    }

    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 3);

    Map<?, ?> pagination = (Map<?, ?>) result.get("pagination");
    assertEquals(0, pagination.get("currentPage"));
    assertEquals(5L, pagination.get("totalItems"));
    assertEquals(2, pagination.get("totalPages"));
    assertEquals(3, pagination.get("itemsPerPage"));
    assertEquals(true, pagination.get("hasNext"));
    assertEquals(false, pagination.get("hasPrevious"));
  }

  @Test
  void getFilteredApplications_whenNoResults_expectZeroTotalPages() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.REJECTED), "dateApplied", "desc", 0, 20);

    Map<?, ?> pagination = (Map<?, ?>) result.get("pagination");
    assertEquals(0L, pagination.get("totalItems"));
    assertEquals(0, pagination.get("totalPages"));
  }

  @Test
  void getFilteredApplications_whenSearchAndStatusCombined_expectFilteredResults() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", "Google", List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(1, applications.size());
  }

  // UNIT TESTS

  @Test
  void createApplication_whenDatabaseFails_expectServiceFailException() {
    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo, mockMongoTemplate);

    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById(anyString())).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(anyString(), anyString(), anyString()))
        .thenReturn(false);
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("DB Crash"));

    assertThrows(
        ApplicationServiceFailException.class,
        () ->
            serviceWithMocks.createApplication(
                "u1",
                "c1",
                "t1",
                ApplicationStatus.APPLIED,
                date,
                null,
                1,
                null,
                null,
                null,
                datetime));
  }

  @Test
  void updateApplication_whenDbFails_expectRuntimeException() {
    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo, mockMongoTemplate);

    ApplicationModel mockApp = mock(ApplicationModel.class);
    when(mockApp.getUserId()).thenReturn("user_001");
    when(mockApp.getCompanyId()).thenReturn("c1");
    when(mockApp.getJobTitle()).thenReturn("Old Title");
    when(mockApp.getStatus()).thenReturn(ApplicationStatus.APPLIED);
    when(mockApp.getApplicationDeadline()).thenReturn(date);
    when(mockApp.getJobDescription()).thenReturn("Old Desc");
    when(mockApp.getNumPositions()).thenReturn(1);
    when(mockApp.getSourceLink()).thenReturn("http://old.com");
    when(mockApp.getDateApplied()).thenReturn(date);
    when(mockApp.getNotes()).thenReturn("Old Notes");
    when(mockApp.getInterviewDateTime()).thenReturn(datetime);

    when(mockAppRepo.findById(anyString())).thenReturn(Optional.of(mockApp));
    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("Update DB Crash"));

    assertThrows(
        RuntimeException.class,
        () ->
            serviceWithMocks.updateApplication(
                "user_001",
                "app1",
                "c1",
                "New Title",
                ApplicationStatus.APPLIED,
                date,
                "Desc",
                1,
                "Link",
                date,
                "Notes",
                datetime));
  }

  @Test
  void updateApplication_whenOnlyTitleChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Brand New Title",
            ApplicationStatus.APPLIED,
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null,
            existingApp.getInterviewDateTime());

    assertEquals("Brand New Title", response.getJobTitle());
    assertEquals(testCompany.getId(), response.getCompanyId());
  }

  @Test
  void updateApplication_whenStatusChangesToDateApplied_dateAppliedChanges() {
    this.existingApp.setDateApplied(null);
    this.existingApp.setStatus(ApplicationStatus.NOT_APPLIED);

    this.applicationRepository.save(existingApp);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Brand New Title",
            ApplicationStatus.APPLIED,
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null,
            existingApp.getInterviewDateTime());

    assertNotNull(response.getDateApplied());
  }

  @Test
  void updateApplication_whenStatusChangesToNotApplied_dateAppliedBecomesNull() {
    this.existingApp.setStatus(ApplicationStatus.APPLIED);
    this.existingApp.setDateApplied(date);

    this.applicationRepository.save(existingApp);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Brand New Title",
            ApplicationStatus.NOT_APPLIED,
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null,
            existingApp.getInterviewDateTime());

    assertNull(response.getDateApplied());
  }

  @Test
  void updateApplication_whenStatusChangesFromInterviewing_InterviewDateBecomesNull() {
    this.existingApp.setStatus(ApplicationStatus.INTERVIEWING);
    this.existingApp.setInterviewDateTime(datetime);

    this.applicationRepository.save(existingApp);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Brand New Title",
            ApplicationStatus.NOT_APPLIED,
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null,
            existingApp.getInterviewDateTime());

    assertNull(response.getInterviewDateTime());
  }

  @Test
  void updateApplication_whenStatusChangesFromInterviewScheduled_InterviewDateBecomesNull() {
    this.existingApp.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
    this.existingApp.setInterviewDateTime(datetime);

    this.applicationRepository.save(existingApp);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Brand New Title",
            ApplicationStatus.APPLIED,
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null,
            existingApp.getInterviewDateTime());

    assertNull(response.getInterviewDateTime());
  }

  @Test
  void
      updateApplication_whenStatusChangesFromInterviewToInterviewScheduld_InterviewDateHasNoChange() {
    this.existingApp.setStatus(ApplicationStatus.INTERVIEWING);
    this.existingApp.setInterviewDateTime(datetime);

    this.applicationRepository.save(existingApp);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Brand New Title",
            ApplicationStatus.INTERVIEW_SCHEDULED,
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null,
            existingApp.getInterviewDateTime());

    assertEquals(datetime, response.getInterviewDateTime());
  }

  @Test
  void
      updateApplication_whenStatusChangesFromInterviewScheduledToInterviewing_InterviewDateHasNoChange() {
    this.existingApp.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
    this.existingApp.setInterviewDateTime(datetime);

    this.applicationRepository.save(existingApp);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Brand New Title",
            ApplicationStatus.INTERVIEWING,
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null,
            existingApp.getInterviewDateTime());

    assertEquals(datetime, response.getInterviewDateTime());
  }

  @Test
  void updateApplication_whenDeadlineIsNullAndDateAppliedIsSet_expectSuccess() {
    this.existingApp.setApplicationDeadline(null);
    this.applicationRepository.save(existingApp);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            null, // new deadline is null
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            LocalDate.now(), // new date applied is NOT null
            existingApp.getNotes(),
            existingApp.getInterviewDateTime());

    assertNotNull(response);
    assertNull(response.getApplicationDeadline());
    assertNotNull(response.getDateApplied());
  }

  @Test
  void updateApplication_whenCompanyIsChangedToValidCompany_expectSuccess() {
    CompanyModel secondCompany = new CompanyModel("Amazon", "Seattle", "https://amazon.com");
    companyRepository.save(secondCompany);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            secondCompany.getId(),
            "Software Engineer",
            ApplicationStatus.APPLIED,
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null,
            existingApp.getInterviewDateTime());

    assertEquals(secondCompany.getId(), response.getCompanyId());
  }

  @Test
  void updateApplication_whenOnlyDescriptionChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            "Brand New Description",
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDateTime());

    assertEquals("Brand New Description", response.getJobDescription());
  }

  @Test
  void updateApplication_whenOnlyLinkChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            "https://new-job-link.com",
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDateTime());

    assertEquals("https://new-job-link.com", response.getSourceLink());
  }

  @Test
  void updateApplication_whenOnlyDateAppliedChanges_expectSuccess() {
    LocalDate newDate = date.minusDays(1);
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            newDate,
            existingApp.getNotes(),
            existingApp.getInterviewDateTime());

    assertEquals(newDate, response.getDateApplied());
  }

  @Test
  void updateApplication_whenOnlyNotesChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            "Updated internal notes",
            existingApp.getInterviewDateTime());

    assertEquals("Updated internal notes", response.getNotes());
  }

  @Test
  void updateApplication_whenOnlyStatusChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            ApplicationStatus.ACCEPTED,
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDateTime());

    assertEquals(ApplicationStatus.ACCEPTED, response.getStatus());
  }

  @Test
  void updateApplication_whenOnlyDeadlineChanges_expectSuccess() {
    LocalDate newDeadline = date.plusWeeks(2);
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            newDeadline,
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDateTime());

    assertEquals(newDeadline, response.getApplicationDeadline());
  }

  @Test
  void updateApplication_whenOnlyPositionsChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            99,
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDateTime());

    assertEquals(99, response.getNumPositions());
  }

  @Test
  void updateApplication_whenOnlyInterviewDateChanges_expectSuccess() {
    LocalDateTime newInterviewDateTime = datetime.plusDays(1);

    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            newInterviewDateTime);

    assertNotNull(response);
    assertEquals(newInterviewDateTime, response.getInterviewDateTime());
  }

  @Test
  void updateApplication_whenAppliedDateAfterApplicationDeadline_expectError() {

    String userId = "user_001";
    String appId = existingApp.getId();
    String companyId = existingApp.getCompanyId();
    String jobTitle = existingApp.getJobTitle();
    ApplicationStatus status = existingApp.getStatus();
    LocalDate deadline = existingApp.getApplicationDeadline();
    String description = existingApp.getJobDescription();
    Integer positions = existingApp.getNumPositions();
    String link = existingApp.getSourceLink();
    String notes = existingApp.getNotes();
    LocalDateTime interviewTime = existingApp.getInterviewDateTime();

    LocalDate invalidAppliedDate = deadline.plusDays(1);

    assertThrows(
        InvalidRequestException.class,
        () ->
            this.applicationService.updateApplication(
                userId,
                appId,
                companyId,
                jobTitle,
                status,
                deadline,
                description,
                positions,
                link,
                invalidAppliedDate,
                notes,
                interviewTime));
  }

  @Test
  void getFilteredApplications_whenDbFails_expectServiceFailException() {
    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo, mockMongoTemplate);

    when(mockMongoTemplate.count(any(), eq(ApplicationModel.class)))
        .thenThrow(new RuntimeException("DB Crash"));

    assertThrows(
        ApplicationServiceFailException.class,
        () ->
            serviceWithMocks.getFilteredApplications(
                "user_001", null, null, "dateApplied", "desc", 0, 20));
  }

  @Test
  void getFilteredApplications_whenSortOrderAsc_expectAscendingResults() {
    ApplicationModel laterApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("Later Role")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(date)
            .dateApplied(date.plusDays(1))
            .interviewDateTime(datetime)
            .build();
    this.applicationRepository.save(laterApp);

    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "asc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(2, applications.size());
  }

  @Test
  void getFilteredApplications_whenEmptyStatusList_expectAllResults() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, Collections.emptyList(), "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(1, applications.size());
  }

  @Test
  void getFilteredApplications_whenBlankSearch_expectAllResults() {
    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", "   ", null, "dateApplied", "desc", 0, 20);

    List<?> applications = (List<?>) result.get("applications");
    assertEquals(1, applications.size());
  }

  @Test
  void getFilteredApplications_whenOnSecondPage_expectHasPreviousTrue() {
    for (int i = 0; i < 4; i++) {
      this.applicationRepository.save(
          ApplicationModel.builder()
              .userId("user_001")
              .companyId(testCompany.getId())
              .jobTitle("Role " + i)
              .status(ApplicationStatus.APPLIED)
              .applicationDeadline(date)
              .interviewDateTime(datetime)
              .build());
    }

    Map<String, Object> result =
        this.applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 1, 3);

    Map<?, ?> pagination = (Map<?, ?>) result.get("pagination");
    assertEquals(1, pagination.get("currentPage"));
    assertEquals(true, pagination.get("hasPrevious"));
    assertEquals(false, pagination.get("hasNext"));
  }

  @Test
  void getApplications_whenUserHasApplications_expectList() {
    List<ApplicationResponse> responses = this.applicationService.getApplications("user_001");

    assertNotNull(responses);
    assertFalse(responses.isEmpty());
    assertEquals(1, responses.size());
    assertEquals(existingApp.getJobTitle(), responses.get(0).getJobTitle());
  }

  @Test
  void getApplications_whenUserHasNoApplications_expectEmptyList() {
    // Create a user with no apps
    UserModel user2 =
        new UserModel("user_002", "test2@example.com", "pwd", "Jane", "Doe", true, 5678);
    userRepository.save(user2);

    List<ApplicationResponse> responses = this.applicationService.getApplications("user_002");

    assertNotNull(responses);
    assertTrue(responses.isEmpty());
  }

  // Tests for getInterviewApplications

  @Test
  void getInterviewApplications_whenNoDateRange_returnInterviewingApps() {

    ApplicationModel interviewingApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("Interviewing Role")
            .status(ApplicationStatus.INTERVIEWING)
            .interviewDateTime(datetime)
            .applicationDeadline(date)
            .build();
    applicationRepository.save(interviewingApp);

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", null, null);

    // Should return 2: existingApp (APPLIED + date) + interviewingApp (INTERVIEWING + date)
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(app -> "Interviewing Role".equals(app.getJobTitle())));
    assertTrue(result.stream().anyMatch(app -> "Software Engineer".equals(app.getJobTitle())));
  }

  @Test
  void getInterviewApplications_whenDateRangeProvided_returnFilteredApps() {
    LocalDate start = date.minusDays(1);
    LocalDate end = date.plusDays(1);

    ApplicationModel insideRange =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("Inside Range")
            .status(ApplicationStatus.INTERVIEWING)
            .interviewDateTime(datetime)
            .applicationDeadline(date)
            .build();

    ApplicationModel outsideRange =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("Outside Range")
            .status(ApplicationStatus.INTERVIEWING)
            .interviewDateTime(datetime.plusDays(10))
            .applicationDeadline(date)
            .build();

    applicationRepository.save(insideRange);
    applicationRepository.save(outsideRange);

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", start, end);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(app -> "Inside Range".equals(app.getJobTitle())));
    assertTrue(result.stream().anyMatch(app -> "Software Engineer".equals(app.getJobTitle())));
  }

  @Test
  void getInterviewApplications_whenStatusNotInterviewingButDateExists_returnResults() {

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", null, null);

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("Software Engineer", result.get(0).getJobTitle());
  }

  @Test
  void getInterviewApplications_whenInterviewDateNull_notReturned() {

    ApplicationModel noDateApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("No Date")
            .status(ApplicationStatus.INTERVIEWING)
            .interviewDateTime(null)
            .applicationDeadline(date)
            .build();
    applicationRepository.save(noDateApp);

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", null, null);

    assertEquals(1, result.size());
    assertNotEquals("No Date", result.get(0).getJobTitle());
    assertEquals("Software Engineer", result.get(0).getJobTitle());
  }

  @Test
  void getInterviewApplications_whenWrongUser_returnEmpty() {
    ApplicationModel interviewingApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("Interviewing Role")
            .status(ApplicationStatus.INTERVIEWING)
            .interviewDateTime(datetime)
            .applicationDeadline(date)
            .build();
    applicationRepository.save(interviewingApp);

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("wrong_user", null, null);

    assertTrue(result.isEmpty());
  }

  @Test
  void getInterviewApplications_unitTest_whenDatesProvided() {
    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo, mockMongoTemplate);

    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class)))
        .thenReturn(Collections.emptyList());

    List<ApplicationResponse> result =
        serviceWithMocks.getInterviewApplications("user1", date, date);

    assertTrue(result.isEmpty());
    verify(mockMongoTemplate).find(any(Query.class), eq(ApplicationModel.class));
  }

  @Test
  void getInterviewApplications_unitTest_whenDatesNull() {
    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo, mockMongoTemplate);

    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class)))
        .thenReturn(Collections.emptyList());

    List<ApplicationResponse> result =
        serviceWithMocks.getInterviewApplications("user1", null, null);

    assertTrue(result.isEmpty());
    verify(mockMongoTemplate).find(any(Query.class), eq(ApplicationModel.class));
  }

  @Test
  void getInterviewApplications_unitTest_whenStartDateOnly() {
    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo, mockMongoTemplate);

    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class)))
        .thenReturn(Collections.emptyList());

    List<ApplicationResponse> result =
        serviceWithMocks.getInterviewApplications("user1", date, null);

    assertTrue(result.isEmpty());
    verify(mockMongoTemplate).find(any(Query.class), eq(ApplicationModel.class));
  }

  @Test
  void getInterviewApplications_unitTest_whenEndDateOnly() {
    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo, mockMongoTemplate);

    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class)))
        .thenReturn(Collections.emptyList());

    List<ApplicationResponse> result =
        serviceWithMocks.getInterviewApplications("user1", null, date);

    assertTrue(result.isEmpty());
    verify(mockMongoTemplate).find(any(Query.class), eq(ApplicationModel.class));
  }
}
