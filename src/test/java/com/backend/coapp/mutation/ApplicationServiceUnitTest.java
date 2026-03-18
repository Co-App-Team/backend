package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import com.backend.coapp.service.ApplicationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

/* these tests were written with the help of Claude Sonnet 4.6 and revised by Eric Hodgson */
class ApplicationServiceUnitTest {

  private ApplicationService applicationService;

  private ApplicationRepository mockAppRepo;
  private CompanyRepository mockCompRepo;
  private UserRepository mockUserRepo;
  private MongoTemplate mockMongoTemplate;

  private CompanyModel testCompany;
  private UserModel testUser;
  private ApplicationModel existingApp;

  private final LocalDate testDate = LocalDate.now().plusDays(30);
  private final LocalDateTime testInterviewDateTime = testDate.atTime(10, 0);

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

  private ApplicationModel.ApplicationModelBuilder getValidCreateBuilder() {
    return ApplicationModel.builder()
        .userId("user_001")
        .companyId("company_001")
        .jobTitle("Data Scientist")
        .status(ApplicationStatus.APPLIED)
        .applicationDeadline(LocalDate.now())
        .jobDescription("Desc")
        .numPositions(1)
        .sourceLink("https://link.com")
        .dateApplied(LocalDate.now())
        .notes("Notes")
        .interviewDateTime(null);
  }

  private ApplicationResponse executeCreate(ApplicationModel.ApplicationModelBuilder builder) {
    ApplicationModel m = builder.build();
    return applicationService.createApplication(
        m.getUserId(),
        m.getCompanyId(),
        m.getJobTitle(),
        m.getStatus(),
        m.getApplicationDeadline(),
        m.getJobDescription(),
        m.getNumPositions(),
        m.getSourceLink(),
        m.getDateApplied(),
        m.getNotes(),
        m.getInterviewDateTime());
  }

  // -------------------------------------------------------------------------
  // createApplication
  // -------------------------------------------------------------------------

  @Test
  void createApplication_whenValid_expectSuccess() {
    setupCreateMocks("company_001", "user_001", "Data Scientist");

    ApplicationResponse response = executeCreate(getValidCreateBuilder());

    assertNotNull(response);
    assertEquals("Data Scientist", response.getJobTitle());
    assertEquals("company_001", response.getCompanyId());
  }

  @Test
  void createApplication_whenInterviewDateProvided_expectSuccess() {
    LocalDateTime interview = LocalDateTime.now().plusDays(14);
    setupCreateMocks("company_001", "user_001", "Data Scientist");

    ApplicationResponse response =
        executeCreate(getValidCreateBuilder().interviewDateTime(interview));

    assertNotNull(response);
    assertEquals(interview, response.getInterviewDateTime());
  }

  @Test
  void createApplication_whenCompanyNotFound_expectException() {
    when(mockCompRepo.findById("invalid_id")).thenReturn(Optional.empty());

    var params = getValidCreateBuilder().companyId("invalid_id");
    assertThrows(CompanyNotFoundException.class, () -> executeCreate(params));
  }

  @Test
  void createApplication_whenUserNotFound_expectException() {
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById("invalid_user")).thenReturn(Optional.empty());

    var params = getValidCreateBuilder().companyId("company_001").userId("invalid_user");
    assertThrows(UserNotFoundException.class, () -> executeCreate(params));
  }

  @Test
  void createApplication_whenDuplicate_expectException() {
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById("user_001")).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(
            "user_001", "company_001", "Software Engineer"))
        .thenReturn(true);

    var params = getValidCreateBuilder().jobTitle("Software Engineer");
    assertThrows(DuplicateApplicationException.class, () -> executeCreate(params));
  }

  @Test
  void createApplication_whenDatabaseFails_expectServiceFailException() {
    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById(anyString())).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(anyString(), anyString(), anyString()))
        .thenReturn(false);
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("DB Crash"));

    var params = getValidCreateBuilder();
    assertThrows(ApplicationServiceFailException.class, () -> executeCreate(params));
  }

  // -------------------------------------------------------------------------
  // getApplications
  // -------------------------------------------------------------------------

  @Test
  void getApplications_whenUserHasApplications_expectList() {
    when(mockAppRepo.findByUserId("user_001")).thenReturn(List.of(existingApp));

    List<ApplicationResponse> result = applicationService.getApplications("user_001");

    assertEquals(1, result.size());
  }

  @Test
  void getApplications_whenNoApplications_expectEmptyList() {
    when(mockAppRepo.findByUserId("user_001")).thenReturn(Collections.emptyList());

    List<ApplicationResponse> result = applicationService.getApplications("user_001");

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

  private ApplicationModel.ApplicationModelBuilder getValidUpdateBuilder() {
    return ApplicationModel.builder()
        .userId("user_001")
        .companyId("company_001")
        .jobTitle(existingApp.getJobTitle())
        .status(existingApp.getStatus())
        .applicationDeadline(existingApp.getApplicationDeadline())
        .jobDescription(existingApp.getJobDescription())
        .numPositions(existingApp.getNumPositions())
        .sourceLink(existingApp.getSourceLink())
        .dateApplied(existingApp.getDateApplied())
        .notes(existingApp.getNotes())
        .interviewDateTime(existingApp.getInterviewDateTime());
  }

  private ApplicationResponse executeUpdate(ApplicationModel.ApplicationModelBuilder builder) {
    return executeUpdate("app_001", builder);
  }

  private ApplicationResponse executeUpdate(
      String appId, ApplicationModel.ApplicationModelBuilder builder) {
    ApplicationModel m = builder.build();
    return applicationService.updateApplication(
        m.getUserId(),
        appId,
        m.getCompanyId(),
        m.getJobTitle(),
        m.getStatus(),
        m.getApplicationDeadline(),
        m.getJobDescription(),
        m.getNumPositions(),
        m.getSourceLink(),
        m.getDateApplied(),
        m.getNotes(),
        m.getInterviewDateTime());
  }

  // -------------------------------------------------------------------------
  // updateApplication
  // -------------------------------------------------------------------------

  @Test
  void updateApplication_whenValid_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        executeUpdate(
            getValidUpdateBuilder()
                .jobTitle("Senior SE")
                .status(ApplicationStatus.INTERVIEWING)
                .applicationDeadline(LocalDate.now().plusDays(10))
                .jobDescription("New Desc")
                .numPositions(2)
                .sourceLink("https://new.link")
                .dateApplied(LocalDate.now())
                .notes("New Notes"));

    assertEquals("Senior SE", response.getJobTitle());
    assertEquals("New Desc", response.getJobDescription());
    assertEquals("New Notes", response.getNotes());
    assertEquals(ApplicationStatus.INTERVIEWING, response.getStatus());
  }

  @Test
  void updateApplication_whenNotOwner_expectUnauthorized() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));

    var params = getValidUpdateBuilder().userId("wrong_user").jobTitle("Title");
    assertThrows(UnauthorizedApplicationAccessException.class, () -> executeUpdate(params));
  }

  @Test
  void updateApplication_whenNoChanges_expectException() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));

    var params = getValidUpdateBuilder();
    assertThrows(NoChangesDetectedException.class, () -> executeUpdate(params));
  }

  @Test
  void updateApplication_whenAppNotFound_expectException() {
    when(mockAppRepo.findById("invalid_app_id")).thenReturn(Optional.empty());

    var params = getValidUpdateBuilder().jobTitle("t");
    assertThrows(ApplicationNotFoundException.class, () -> executeUpdate("invalid_app_id", params));
  }

  @Test
  void updateApplication_whenNewCompanyNotFound_expectException() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("non_existent_company")).thenReturn(Optional.empty());

    var params = getValidUpdateBuilder().companyId("non_existent_company");
    assertThrows(CompanyNotFoundException.class, () -> executeUpdate(params));
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
    when(mockApp.getInterviewDateTime()).thenReturn(null);

    when(mockAppRepo.findById(anyString())).thenReturn(Optional.of(mockApp));
    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("Update DB Crash"));

    var params = getValidUpdateBuilder().jobTitle("New Title");
    assertThrows(RuntimeException.class, () -> executeUpdate(params));
  }

  @Test
  void updateApplication_whenOnlyTitleChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        executeUpdate(getValidUpdateBuilder().jobTitle("Brand New Title"));

    assertEquals("Brand New Title", response.getJobTitle());
  }

  @Test
  void updateApplication_whenOnlyStatusChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        executeUpdate(getValidUpdateBuilder().status(ApplicationStatus.ACCEPTED));

    assertEquals(ApplicationStatus.ACCEPTED, response.getStatus());
  }

  @Test
  void updateApplication_whenOnlyDescriptionChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        executeUpdate(getValidUpdateBuilder().jobDescription("Brand New Description"));

    assertEquals("Brand New Description", response.getJobDescription());
  }

  @Test
  void updateApplication_whenOnlyLinkChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        executeUpdate(getValidUpdateBuilder().sourceLink("https://new-job-link.com"));

    assertEquals("https://new-job-link.com", response.getSourceLink());
  }

  @Test
  void updateApplication_whenOnlyDateAppliedChanges_expectSuccess() {
    setupExistingAppMock();
    LocalDate newDate = LocalDate.now().minusDays(1);

    ApplicationResponse response = executeUpdate(getValidUpdateBuilder().dateApplied(newDate));

    assertEquals(newDate, response.getDateApplied());
  }

  @Test
  void updateApplication_whenOnlyNotesChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        executeUpdate(getValidUpdateBuilder().notes("Updated internal notes"));

    assertEquals("Updated internal notes", response.getNotes());
  }

  @Test
  void updateApplication_whenOnlyDeadlineChanges_expectSuccess() {
    setupExistingAppMock();
    LocalDate newDeadline = LocalDate.now().plusWeeks(2);

    ApplicationResponse response =
        executeUpdate(getValidUpdateBuilder().applicationDeadline(newDeadline));

    assertEquals(newDeadline, response.getApplicationDeadline());
  }

  @Test
  void updateApplication_whenOnlyPositionsChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response = executeUpdate(getValidUpdateBuilder().numPositions(99));

    assertEquals(99, response.getNumPositions());
  }

  @Test
  void updateApplication_whenOnlyInterviewDateChanges_expectSuccess() {
    setupExistingAppMock();
    LocalDateTime newInterview = LocalDateTime.now().plusDays(7);

    ApplicationResponse response =
        executeUpdate(getValidUpdateBuilder().interviewDateTime(newInterview));

    assertEquals(newInterview, response.getInterviewDateTime());
  }

  @Test
  void updateApplication_whenCompanyIsChangedToValidCompany_expectSuccess() {
    CompanyModel secondCompany = new CompanyModel("Amazon", "Seattle", "https://amazon.com");
    ReflectionTestUtils.setField(secondCompany, "id", "company_002");

    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("company_002")).thenReturn(Optional.of(secondCompany));
    when(mockAppRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    ApplicationResponse response = executeUpdate(getValidUpdateBuilder().companyId("company_002"));

    assertEquals("company_002", response.getCompanyId());
  }

  @Test
  void updateApplication_whenStatusChangedToApplied_expectDateAppliedSetToToday() {
    ApplicationModel interviewingApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId("company_001")
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.INTERVIEWING)
            .applicationDeadline(LocalDate.now().plusDays(5))
            .build();
    ReflectionTestUtils.setField(interviewingApp, "id", "app_001");

    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(interviewingApp));
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockAppRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    ApplicationResponse response =
        executeUpdate(getValidUpdateBuilder().status(ApplicationStatus.APPLIED));

    assertEquals(ApplicationStatus.APPLIED, response.getStatus());
    assertEquals(LocalDate.now(), response.getDateApplied());
  }

  @Test
  void updateApplication_whenAppliedDateAfterDeadline_expectInvalidRequest() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));

    // existingApp deadline = LocalDate.now().plusDays(5); set dateApplied beyond it
    var params = getValidUpdateBuilder().dateApplied(LocalDate.now().plusDays(10));
    assertThrows(InvalidRequestException.class, () -> executeUpdate(params));
  }

  @Test
  void updateApplication_whenAppliedDateEqualsDeadline_expectSuccess() {
    setupExistingAppMock();

    // dateApplied == applicationDeadline (same day) should NOT throw
    var params = getValidUpdateBuilder().dateApplied(LocalDate.now().plusDays(5));
    ApplicationResponse response = executeUpdate(params);
    assertNotNull(response);
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

  private void mockFilteredQuery(List<ApplicationModel> results, long count) {
    when(mockMongoTemplate.count(any(Query.class), eq(ApplicationModel.class))).thenReturn(count);
    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class)))
        .thenReturn((List) results);
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — results
  // -------------------------------------------------------------------------

  @Test
  void getFilteredApplications_whenNoFilters_expectAllUserApplications() {
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<ApplicationResponse>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenSearchMatchesCompany_expectResults() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("Goo"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", "Goo", null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<ApplicationResponse>) result.get("applications")).size());
    verify(mockCompRepo, atLeastOnce()).findByCompanyNameContainingIgnoreCase("Goo");
  }

  @Test
  void getFilteredApplications_whenSearchMatchesCaseInsensitive_expectResults() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("google"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", "google", null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<ApplicationResponse>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenSearchMatchesNoCompany_expectEmptyList() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("nonexistent"))
        .thenReturn(Collections.emptyList());
    mockFilteredQuery(Collections.emptyList(), 0L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", "nonexistent", null, "dateApplied", "desc", 0, 20);

    assertTrue(((List<ApplicationResponse>) result.get("applications")).isEmpty());
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

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<ApplicationResponse>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenStatusDoesNotMatch_expectEmptyList() {
    mockFilteredQuery(Collections.emptyList(), 0L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.REJECTED), "dateApplied", "desc", 0, 20);

    assertTrue(((List<ApplicationResponse>) result.get("applications")).isEmpty());
  }

  @Test
  void getFilteredApplications_whenMultipleStatuses_expectMatchingResults() {
    mockFilteredQuery(List.of(existingApp, existingApp), 2L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001",
            null,
            List.of(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED),
            "dateApplied",
            "desc",
            0,
            20);

    assertEquals(2, ((List<ApplicationResponse>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenEmptyStatusList_expectAllResults() {
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, Collections.emptyList(), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<ApplicationResponse>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenSortOrderAsc_expectAscendingResults() {
    mockFilteredQuery(List.of(existingApp, existingApp), 2L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "asc", 0, 20);

    assertEquals(2, ((List<ApplicationResponse>) result.get("applications")).size());
  }

  @Test
  void getFilteredApplications_whenPaginated_expectCorrectPage() {
    mockFilteredQuery(List.of(existingApp, existingApp, existingApp), 5L);

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 3);

    assertEquals(3, ((List<ApplicationResponse>) result.get("applications")).size());
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

    Map<String, ?> pagination =
        (Map<String, ?>)
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

    Map<String, ?> pagination =
        (Map<String, ?>)
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

    Map<String, ?> pagination =
        (Map<String, ?>)
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

    Map<String, ?> result =
        applicationService.getFilteredApplications(
            "user_001", "Google", List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<ApplicationResponse>) result.get("applications")).size());
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

  // -------------------------------------------------------------------------
  // getInterviewApplications — helpers
  // -------------------------------------------------------------------------

  private void mockInterviewQuery(List<ApplicationModel> results) {
    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class)))
        .thenReturn((List) results);
  }

  // -------------------------------------------------------------------------
  // getInterviewApplications
  // -------------------------------------------------------------------------

  @Test
  void getInterviewApplications_whenNoDateRange_returnInterviewingApps() {
    ApplicationModel interviewingApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId("company_001")
            .jobTitle("Interviewing Role")
            .status(ApplicationStatus.INTERVIEWING)
            .interviewDateTime(testInterviewDateTime)
            .applicationDeadline(testDate)
            .build();

    mockInterviewQuery(List.of(existingApp, interviewingApp));

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", null, null);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(app -> "Interviewing Role".equals(app.getJobTitle())));
    assertTrue(result.stream().anyMatch(app -> "Software Engineer".equals(app.getJobTitle())));
  }

  @Test
  void getInterviewApplications_whenDateRangeProvided_returnFilteredApps() {
    LocalDate start = testDate.minusDays(1);
    LocalDate end = testDate.plusDays(1);

    ApplicationModel insideRange =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId("company_001")
            .jobTitle("Inside Range")
            .status(ApplicationStatus.INTERVIEWING)
            .interviewDateTime(testInterviewDateTime)
            .applicationDeadline(testDate)
            .build();

    mockInterviewQuery(List.of(existingApp, insideRange));

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", start, end);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(app -> "Inside Range".equals(app.getJobTitle())));
    assertTrue(result.stream().anyMatch(app -> "Software Engineer".equals(app.getJobTitle())));
  }

  @Test
  void getInterviewApplications_whenStatusNotInterviewingButDateExists_returnResults() {
    mockInterviewQuery(List.of(existingApp));

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", null, null);

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("Software Engineer", result.get(0).getJobTitle());
  }

  @Test
  void getInterviewApplications_whenInterviewDateNull_notReturned() {
    mockInterviewQuery(List.of(existingApp));

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", null, null);

    assertEquals(1, result.size());
    assertNotEquals("No Date", result.get(0).getJobTitle());
    assertEquals("Software Engineer", result.get(0).getJobTitle());
  }

  @Test
  void getInterviewApplications_whenWrongUser_returnEmpty() {
    mockInterviewQuery(Collections.emptyList());

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("wrong_user", null, null);

    assertTrue(result.isEmpty());
  }

  @Test
  void getInterviewApplications_whenDatesProvided_expectMongoQueryCalled() {
    mockInterviewQuery(Collections.emptyList());

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", testDate, testDate);

    assertTrue(result.isEmpty());
    verify(mockMongoTemplate).find(any(Query.class), eq(ApplicationModel.class));
  }

  @Test
  void getInterviewApplications_whenBothDatesProvided_expectDateRangeInQuery() {
    mockInterviewQuery(Collections.emptyList());

    applicationService.getInterviewApplications(
        "user_001", testDate.minusDays(1), testDate.plusDays(1));

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertTrue(captor.getValue().toString().contains("gte"));
  }

  @Test
  void getInterviewApplications_whenOnlyStartDate_expectNoDateRangeInQuery() {
    mockInterviewQuery(Collections.emptyList());

    applicationService.getInterviewApplications("user_001", testDate, null);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertFalse(captor.getValue().toString().contains("gte"));
  }

  @Test
  void getInterviewApplications_whenDatesNull_expectMongoQueryCalled() {
    mockInterviewQuery(Collections.emptyList());

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", null, null);

    assertTrue(result.isEmpty());
    verify(mockMongoTemplate).find(any(Query.class), eq(ApplicationModel.class));
  }

  @Test
  void getInterviewApplications_whenStartDateOnly_expectMongoQueryCalled() {
    mockInterviewQuery(Collections.emptyList());

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", testDate, null);

    assertTrue(result.isEmpty());
    verify(mockMongoTemplate).find(any(Query.class), eq(ApplicationModel.class));
  }

  @Test
  void getInterviewApplications_whenEndDateOnly_expectMongoQueryCalled() {
    mockInterviewQuery(Collections.emptyList());

    List<ApplicationResponse> result =
        applicationService.getInterviewApplications("user_001", null, testDate);

    assertTrue(result.isEmpty());
    verify(mockMongoTemplate).find(any(Query.class), eq(ApplicationModel.class));
  }

  @Test
  void getInterviewApplications_whenDbFails_expectRuntimeException() {
    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class)))
        .thenThrow(new RuntimeException("DB Crash"));

    assertThrows(
        RuntimeException.class,
        () -> applicationService.getInterviewApplications("user_001", null, null));
  }
}
