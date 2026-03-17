package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.exception.application.*;
import com.backend.coapp.exception.company.CompanyNotFoundException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.ApplicationService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;

public class ApplicationServiceUnitTest {

  private ApplicationService applicationService;

  private ApplicationRepository mockAppRepo;
  private CompanyRepository mockCompRepo;
  private UserRepository mockUserRepo;
  private MongoTemplate mockMongoTemplate;

  private CompanyModel testCompany;
  private UserModel testUser;
  private ApplicationModel existingApp;

  @BeforeEach
  void setUp() {
    this.mockAppRepo = Mockito.mock(ApplicationRepository.class);
    this.mockCompRepo = Mockito.mock(CompanyRepository.class);
    this.mockUserRepo = Mockito.mock(UserRepository.class);
    this.mockMongoTemplate = Mockito.mock(MongoTemplate.class);

    this.testCompany = new CompanyModel("Google", "Mountain View", "https://google.com");
    ReflectionTestUtils.setField(this.testCompany, "id", "company_001");

    this.testUser =
        new UserModel("user_001", "test@example.com", "password123", "John", "Doe", true, 1234);

    this.existingApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId("company_001")
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.now().plusDays(5))
            .build();
    ReflectionTestUtils.setField(this.existingApp, "id", "app_001");

    this.applicationService =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo, mockMongoTemplate);
  }

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  @Test
  void constructor_expectSameInitInstance() {
    assertSame(mockAppRepo, applicationService.getApplicationRepository());
    assertSame(mockCompRepo, applicationService.getCompanyRepository());
    assertSame(mockMongoTemplate, applicationService.getMongoTemplate());
  }

  // -------------------------------------------------------------------------
  // createApplication — helpers
  // -------------------------------------------------------------------------

  private void setupCreateMocks(String companyId, String userId, String jobTitle) {
    when(mockCompRepo.findById(companyId)).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(userId, companyId, jobTitle))
        .thenReturn(false);
    when(mockAppRepo.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  private class CreateBuilder {
    String userId = "user_001";
    String companyId = "company_001";
    String jobTitle = "Data Scientist";
    ApplicationStatus status = ApplicationStatus.APPLIED;
    LocalDate deadline = LocalDate.now();
    String desc = "Desc";
    Integer positions = 1;
    String link = "https://link.com";
    LocalDate dateApplied = LocalDate.now();
    String notes = "Notes";
    LocalDate interviewDate = null;

    CreateBuilder withUserId(String v) {
      this.userId = v;
      return this;
    }

    CreateBuilder withCompanyId(String v) {
      this.companyId = v;
      return this;
    }

    CreateBuilder withJobTitle(String v) {
      this.jobTitle = v;
      return this;
    }

    CreateBuilder withStatus(ApplicationStatus v) {
      this.status = v;
      return this;
    }

    CreateBuilder withDeadline(LocalDate v) {
      this.deadline = v;
      return this;
    }

    CreateBuilder withDesc(String v) {
      this.desc = v;
      return this;
    }

    CreateBuilder withPositions(Integer v) {
      this.positions = v;
      return this;
    }

    CreateBuilder withLink(String v) {
      this.link = v;
      return this;
    }

    CreateBuilder withDateApplied(LocalDate v) {
      this.dateApplied = v;
      return this;
    }

    CreateBuilder withNotes(String v) {
      this.notes = v;
      return this;
    }

    CreateBuilder withInterviewDate(LocalDate v) {
      this.interviewDate = v;
      return this;
    }

    ApplicationResponse execute() {
      return applicationService.createApplication(
          userId,
          companyId,
          jobTitle,
          status,
          deadline,
          desc,
          positions,
          link,
          dateApplied,
          notes,
          interviewDate);
    }
  }

  // -------------------------------------------------------------------------
  // createApplication
  // -------------------------------------------------------------------------

  @Test
  void createApplication_whenValid_expectSuccess() {
    setupCreateMocks("company_001", "user_001", "Data Scientist");

    ApplicationResponse response = new CreateBuilder().execute();

    assertNotNull(response);
    assertEquals("Data Scientist", response.getJobTitle());
    assertEquals("company_001", response.getCompanyId());
  }

  @Test
  void createApplication_whenInterviewDateProvided_expectSuccess() {
    LocalDate interview = LocalDate.now().plusDays(14);
    setupCreateMocks("company_001", "user_001", "Data Scientist");

    ApplicationResponse response = new CreateBuilder().withInterviewDate(interview).execute();

    assertNotNull(response);
    assertEquals(interview, response.getInterviewDate());
  }

  @Test
  void createApplication_whenCompanyNotFound_expectException() {
    when(mockCompRepo.findById("invalid_id")).thenReturn(Optional.empty());

    assertThrows(
        CompanyNotFoundException.class,
        () -> new CreateBuilder().withCompanyId("invalid_id").execute());
  }

  @Test
  void createApplication_whenUserNotFound_expectException() {
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById("invalid_user")).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> new CreateBuilder().withUserId("invalid_user").execute());
  }

  @Test
  void createApplication_whenDuplicate_expectException() {
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById("user_001")).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(
            "user_001", "company_001", "Software Engineer"))
        .thenReturn(true);

    assertThrows(
        DuplicateApplicationException.class,
        () -> new CreateBuilder().withJobTitle("Software Engineer").execute());
  }

  @Test
  void createApplication_whenDatabaseFails_expectServiceFailException() {
    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById(anyString())).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(anyString(), anyString(), anyString()))
        .thenReturn(false);
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("DB Crash"));

    assertThrows(ApplicationServiceFailException.class, () -> new CreateBuilder().execute());
  }

  // -------------------------------------------------------------------------
  // getApplications
  // -------------------------------------------------------------------------

  @Test
  void getApplications_whenUserHasApplications_expectList() {
    when(mockAppRepo.findByUserId("user_001")).thenReturn(List.of(existingApp));

    List<?> result = applicationService.getApplications("user_001");

    assertEquals(1, result.size());
  }

  @Test
  void getApplications_whenNoApplications_expectEmptyList() {
    when(mockAppRepo.findByUserId("user_001")).thenReturn(Collections.emptyList());

    List<?> result = applicationService.getApplications("user_001");

    assertTrue(result.isEmpty());
  }

  // -------------------------------------------------------------------------
  // updateApplication — helpers
  // -------------------------------------------------------------------------

  private void setupExistingAppMock() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockAppRepo.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  private class UpdateBuilder {
    String userId = "user_001";
    String appId = "app_001";
    String companyId = "company_001";
    String jobTitle = existingApp.getJobTitle();
    ApplicationStatus status = existingApp.getStatus();
    LocalDate deadline = existingApp.getApplicationDeadline();
    String desc = existingApp.getJobDescription();
    Integer positions = existingApp.getNumPositions();
    String link = existingApp.getSourceLink();
    LocalDate dateApplied = existingApp.getDateApplied();
    String notes = existingApp.getNotes();
    LocalDate interviewDate = existingApp.getInterviewDate();

    UpdateBuilder withUserId(String v) {
      this.userId = v;
      return this;
    }

    UpdateBuilder withAppId(String v) {
      this.appId = v;
      return this;
    }

    UpdateBuilder withCompanyId(String v) {
      this.companyId = v;
      return this;
    }

    UpdateBuilder withJobTitle(String v) {
      this.jobTitle = v;
      return this;
    }

    UpdateBuilder withStatus(ApplicationStatus v) {
      this.status = v;
      return this;
    }

    UpdateBuilder withDeadline(LocalDate v) {
      this.deadline = v;
      return this;
    }

    UpdateBuilder withDesc(String v) {
      this.desc = v;
      return this;
    }

    UpdateBuilder withPositions(Integer v) {
      this.positions = v;
      return this;
    }

    UpdateBuilder withLink(String v) {
      this.link = v;
      return this;
    }

    UpdateBuilder withDateApplied(LocalDate v) {
      this.dateApplied = v;
      return this;
    }

    UpdateBuilder withNotes(String v) {
      this.notes = v;
      return this;
    }

    UpdateBuilder withInterviewDate(LocalDate v) {
      this.interviewDate = v;
      return this;
    }

    ApplicationResponse execute() {
      return applicationService.updateApplication(
          userId,
          appId,
          companyId,
          jobTitle,
          status,
          deadline,
          desc,
          positions,
          link,
          dateApplied,
          notes,
          interviewDate);
    }
  }

  // -------------------------------------------------------------------------
  // updateApplication
  // -------------------------------------------------------------------------

  @Test
  void updateApplication_whenValid_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        new UpdateBuilder()
            .withJobTitle("Senior SE")
            .withStatus(ApplicationStatus.INTERVIEWING)
            .withDeadline(LocalDate.now().plusDays(10))
            .withDesc("New Desc")
            .withPositions(2)
            .withLink("https://new.link")
            .withDateApplied(LocalDate.now())
            .withNotes("New Notes")
            .execute();

    assertEquals("Senior SE", response.getJobTitle());
    assertEquals("New Desc", response.getJobDescription());
    assertEquals("New Notes", response.getNotes());
    assertEquals(ApplicationStatus.INTERVIEWING, response.getStatus());
  }

  @Test
  void updateApplication_whenNotOwner_expectUnauthorized() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));

    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () -> new UpdateBuilder().withUserId("wrong_user").withJobTitle("Title").execute());
  }

  @Test
  void updateApplication_whenNoChanges_expectException() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));

    assertThrows(NoChangesDetectedException.class, () -> new UpdateBuilder().execute());
  }

  @Test
  void updateApplication_whenAppNotFound_expectException() {
    when(mockAppRepo.findById("invalid_app_id")).thenReturn(Optional.empty());

    assertThrows(
        ApplicationNotFoundException.class,
        () -> new UpdateBuilder().withAppId("invalid_app_id").withJobTitle("t").execute());
  }

  @Test
  void updateApplication_whenNewCompanyNotFound_expectException() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("non_existent_company")).thenReturn(Optional.empty());

    assertThrows(
        CompanyNotFoundException.class,
        () -> new UpdateBuilder().withCompanyId("non_existent_company").execute());
  }

  @Test
  void updateApplication_whenDbFails_expectRuntimeException() {
    ApplicationModel mockApp = mock(ApplicationModel.class);
    when(mockApp.getUserId()).thenReturn("user_001");
    when(mockApp.getCompanyId()).thenReturn("c1");
    when(mockApp.getJobTitle()).thenReturn("Old Title");
    when(mockApp.getStatus()).thenReturn(ApplicationStatus.APPLIED);
    when(mockApp.getApplicationDeadline()).thenReturn(LocalDate.now());
    when(mockApp.getJobDescription()).thenReturn("Old Desc");
    when(mockApp.getNumPositions()).thenReturn(1);
    when(mockApp.getSourceLink()).thenReturn("http://old.com");
    when(mockApp.getDateApplied()).thenReturn(LocalDate.now());
    when(mockApp.getNotes()).thenReturn("Old Notes");
    when(mockApp.getInterviewDate()).thenReturn(null);

    when(mockAppRepo.findById(anyString())).thenReturn(Optional.of(mockApp));
    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("Update DB Crash"));

    assertThrows(
        RuntimeException.class, () -> new UpdateBuilder().withJobTitle("New Title").execute());
  }

  @Test
  void updateApplication_whenOnlyTitleChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response = new UpdateBuilder().withJobTitle("Brand New Title").execute();

    assertEquals("Brand New Title", response.getJobTitle());
  }

  @Test
  void updateApplication_whenOnlyStatusChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        new UpdateBuilder().withStatus(ApplicationStatus.ACCEPTED).execute();

    assertEquals(ApplicationStatus.ACCEPTED, response.getStatus());
  }

  @Test
  void updateApplication_whenOnlyDescriptionChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response = new UpdateBuilder().withDesc("Brand New Description").execute();

    assertEquals("Brand New Description", response.getJobDescription());
  }

  @Test
  void updateApplication_whenOnlyLinkChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        new UpdateBuilder().withLink("https://new-job-link.com").execute();

    assertEquals("https://new-job-link.com", response.getSourceLink());
  }

  @Test
  void updateApplication_whenOnlyDateAppliedChanges_expectSuccess() {
    setupExistingAppMock();
    LocalDate newDate = LocalDate.now().minusDays(1);

    ApplicationResponse response = new UpdateBuilder().withDateApplied(newDate).execute();

    assertEquals(newDate, response.getDateApplied());
  }

  @Test
  void updateApplication_whenOnlyNotesChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        new UpdateBuilder().withNotes("Updated internal notes").execute();

    assertEquals("Updated internal notes", response.getNotes());
  }

  @Test
  void updateApplication_whenOnlyDeadlineChanges_expectSuccess() {
    setupExistingAppMock();
    LocalDate newDeadline = LocalDate.now().plusWeeks(2);

    ApplicationResponse response = new UpdateBuilder().withDeadline(newDeadline).execute();

    assertEquals(newDeadline, response.getApplicationDeadline());
  }

  @Test
  void updateApplication_whenOnlyPositionsChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response = new UpdateBuilder().withPositions(99).execute();

    assertEquals(99, response.getNumPositions());
  }

  @Test
  void updateApplication_whenOnlyInterviewDateChanges_expectSuccess() {
    setupExistingAppMock();
    LocalDate newInterview = LocalDate.now().plusDays(7);

    ApplicationResponse response = new UpdateBuilder().withInterviewDate(newInterview).execute();

    assertEquals(newInterview, response.getInterviewDate());
  }

  @Test
  void updateApplication_whenCompanyIsChangedToValidCompany_expectSuccess() {
    CompanyModel secondCompany = new CompanyModel("Amazon", "Seattle", "https://amazon.com");
    ReflectionTestUtils.setField(secondCompany, "id", "company_002");

    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("company_002")).thenReturn(Optional.of(secondCompany));
    when(mockAppRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    ApplicationResponse response = new UpdateBuilder().withCompanyId("company_002").execute();

    assertEquals("company_002", response.getCompanyId());
  }

  // -------------------------------------------------------------------------
  // deleteApplication
  // -------------------------------------------------------------------------

  @Test
  void deleteApplication_whenValid_expectDeleted() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));

    assertDoesNotThrow(() -> applicationService.deleteApplication("app_001", "user_001"));
    verify(mockAppRepo, times(1)).deleteById("app_001");
  }

  @Test
  void deleteApplication_whenNotOwner_expectUnauthorized() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));

    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () -> applicationService.deleteApplication("app_001", "malicious_user"));
  }

  @Test
  void deleteApplication_whenApplicationNotFound_expectException() {
    when(mockAppRepo.findById("non_existent_id")).thenReturn(Optional.empty());

    assertThrows(
        ApplicationNotFoundException.class,
        () -> applicationService.deleteApplication("non_existent_id", "user_001"));
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — helpers
  // -------------------------------------------------------------------------

  private void mockFilteredQuery(List<?> results, long count) {
    when(mockMongoTemplate.count(any(Query.class), eq(ApplicationModel.class))).thenReturn(count);
    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class)))
        .thenReturn((List<ApplicationModel>) results);
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — results
  // -------------------------------------------------------------------------

  @Test
  void getFilteredApplications_whenNoFilters_expectAllUserApplications() {
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenSearchMatchesCompany_expectResults() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("Goo"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", "Goo", null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
    verify(mockCompRepo, atLeastOnce()).findByCompanyNameContainingIgnoreCase("Goo");
  }

  @Test
  void getFilteredApplications_whenSearchMatchesCaseInsensitive_expectResults() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("google"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", "google", null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenSearchMatchesNoCompany_expectEmptyList() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("nonexistent"))
        .thenReturn(Collections.emptyList());
    mockFilteredQuery(Collections.emptyList(), 0L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", "nonexistent", null, "dateApplied", "desc", 0, 20);

    assertTrue(((List<?>) result.get("applications")).isEmpty());
  }

  @Test
  void getFilteredApplications_whenBlankSearch_expectAllResults() {
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications("user_001", " ", null, "dateApplied", "desc", 0, 20);

    verify(mockCompRepo, never()).findByCompanyNameContainingIgnoreCase(anyString());
  }

  @Test
  void getFilteredApplications_whenStatusMatches_expectFilteredResults() {
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenStatusDoesNotMatch_expectEmptyList() {
    mockFilteredQuery(Collections.emptyList(), 0L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.REJECTED), "dateApplied", "desc", 0, 20);

    assertTrue(((List<?>) result.get("applications")).isEmpty());
  }

  @Test
  void getFilteredApplications_whenMultipleStatuses_expectMatchingResults() {
    mockFilteredQuery(List.of(existingApp, existingApp), 2L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001",
            null,
            List.of(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED),
            "dateApplied",
            "desc",
            0,
            20);

    assertEquals(2, ((List<?>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenEmptyStatusList_expectAllResults() {
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, Collections.emptyList(), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenSortOrderAsc_expectAscendingResults() {
    mockFilteredQuery(List.of(existingApp, existingApp), 2L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "asc", 0, 20);

    assertEquals(2, ((List<?>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenPaginated_expectCorrectPage() {
    mockFilteredQuery(List.of(existingApp, existingApp, existingApp), 5L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 3);

    assertEquals(3, ((List<?>) result.get("applications")).size());
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — query construction verified via ArgumentCaptor
  // -------------------------------------------------------------------------

  @Test
  void getFilteredApplications_whenSearchProvided_expectCompanyIdsInQuery() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("Google"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications(
        "user_001", "Google", null, "dateApplied", "desc", 0, 20);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertTrue(captor.getValue().toString().contains("company_001"));
  }

  @Test
  void getFilteredApplications_whenStatusProvided_expectStatusInQuery() {
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications(
        "user_001", null, List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertTrue(captor.getValue().toString().contains("APPLIED"));
  }

  @Test
  void getFilteredApplications_whenSortAsc_expectAscDirectionInQuery() {
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications("user_001", null, null, "dateApplied", "asc", 0, 20);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertFalse(captor.getValue().getSortObject().toString().contains("-1"));
  }

  @Test
  void getFilteredApplications_whenSortDesc_expectDescDirectionInQuery() {
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications(
        "user_001", null, null, "dateApplied", "desc", 0, 20);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertTrue(captor.getValue().getSortObject().toString().contains("-1"));
  }

  @Test
  void getFilteredApplications_whenPage2Size3_expectSkip6() {
    mockFilteredQuery(List.of(existingApp, existingApp), 10L);

    applicationService.getFilteredApplications("user_001", null, null, "dateApplied", "desc", 2, 3);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertEquals(6L, captor.getValue().getSkip());
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — pagination metadata
  // -------------------------------------------------------------------------

  @Test
  void getFilteredApplications_whenPaginated_expectCorrectPaginationMetadata() {
    mockFilteredQuery(List.of(existingApp, existingApp, existingApp), 5L);

    Map<?, ?> pagination =
        (Map<?, ?>)
            applicationService
                .getFilteredApplications("user_001", null, null, "dateApplied", "desc", 0, 3)
                .get("pagination");

    assertEquals(0, pagination.get("currentPage"));
    assertEquals(5L, pagination.get("totalItems"));
    assertEquals(2, pagination.get("totalPages"));
    assertEquals(3, pagination.get("itemsPerPage"));
    assertEquals(true, pagination.get("hasNext"));
    assertEquals(false, pagination.get("hasPrevious"));
  }

  @Test
  void getFilteredApplications_whenNoResults_expectZeroTotalPages() {
    mockFilteredQuery(Collections.emptyList(), 0L);

    Map<?, ?> pagination =
        (Map<?, ?>)
            applicationService
                .getFilteredApplications(
                    "user_001",
                    null,
                    List.of(ApplicationStatus.REJECTED),
                    "dateApplied",
                    "desc",
                    0,
                    20)
                .get("pagination");

    assertEquals(0L, pagination.get("totalItems"));
    assertEquals(0, pagination.get("totalPages"));
  }

  @Test
  void getFilteredApplications_whenOnSecondPage_expectHasPreviousTrue() {
    mockFilteredQuery(List.of(existingApp, existingApp), 5L);

    Map<?, ?> pagination =
        (Map<?, ?>)
            applicationService
                .getFilteredApplications("user_001", null, null, "dateApplied", "desc", 1, 3)
                .get("pagination");

    assertEquals(1, pagination.get("currentPage"));
    assertEquals(true, pagination.get("hasPrevious"));
    assertEquals(false, pagination.get("hasNext"));
  }

  @Test
  void getFilteredApplications_whenSearchAndStatusCombined_expectFilteredResults() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("Google"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<?, ?> result =
        applicationService.getFilteredApplications(
            "user_001", "Google", List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — failure
  // -------------------------------------------------------------------------

  @Test
  void getFilteredApplications_whenDbFails_expectServiceFailException() {
    when(mockMongoTemplate.count(any(), eq(ApplicationModel.class)))
        .thenThrow(new RuntimeException("DB Crash"));

    assertThrows(
        ApplicationServiceFailException.class,
        () ->
            applicationService.getFilteredApplications(
                "user_001", null, null, "dateApplied", "desc", 0, 20));
  }

  @Test
  void updateApplication_whenStatusChangedToApplied_expectDateAppliedSetToToday() {
    ApplicationModel interviewingApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId("company_001")
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.INTERVIEWING) // starts as non-APPLIED
            .applicationDeadline(LocalDate.now().plusDays(5))
            .build();
    ReflectionTestUtils.setField(interviewingApp, "id", "app_001");

    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(interviewingApp));
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockAppRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    ApplicationResponse response =
        new UpdateBuilder().withStatus(ApplicationStatus.APPLIED).execute();

    assertEquals(ApplicationStatus.APPLIED, response.getStatus());
    assertEquals(LocalDate.now(), response.getDateApplied()); // ← kills the mutant
  }
}
