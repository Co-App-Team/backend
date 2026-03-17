package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.exception.*;
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
  public void setUp() {
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
  public void constructor_expectSameInitInstance() {
    assertSame(mockAppRepo, applicationService.getApplicationRepository());
    assertSame(mockCompRepo, applicationService.getCompanyRepository());
    assertSame(mockMongoTemplate, applicationService.getMongoTemplate());
  }

  // -------------------------------------------------------------------------
  // createApplication
  // -------------------------------------------------------------------------

  @Test
  public void createApplication_whenValid_expectSuccess() {
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById("user_001")).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(
            "user_001", "company_001", "Data Scientist"))
        .thenReturn(false);
    when(mockAppRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    ApplicationResponse response =
        applicationService.createApplication(
            "user_001",
            "company_001",
            "Data Scientist",
            ApplicationStatus.APPLIED,
            LocalDate.now(),
            "Desc",
            1,
            "https://link.com",
            LocalDate.now(),
            "Notes");

    assertNotNull(response);
    assertEquals("Data Scientist", response.getJobTitle());
    assertEquals("company_001", response.getCompanyId());
  }

  @Test
  public void createApplication_whenCompanyNotFound_expectException() {
    when(mockCompRepo.findById("invalid_id")).thenReturn(Optional.empty());

    assertThrows(
        CompanyNotFoundException.class,
        () ->
            applicationService.createApplication(
                "user_001",
                "invalid_id",
                "Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                null,
                1,
                null,
                null,
                null));
  }

  @Test
  public void createApplication_whenUserNotFound_expectException() {
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById("invalid_user")).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () ->
            applicationService.createApplication(
                "invalid_user",
                "company_001",
                "Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                null,
                1,
                null,
                null,
                null));
  }

  @Test
  public void createApplication_whenDuplicate_expectException() {
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById("user_001")).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(
            "user_001", "company_001", "Software Engineer"))
        .thenReturn(true);

    assertThrows(
        DuplicateApplicationException.class,
        () ->
            applicationService.createApplication(
                "user_001",
                "company_001",
                "Software Engineer",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                null,
                1,
                null,
                null,
                null));
  }

  @Test
  public void createApplication_whenDatabaseFails_expectServiceFailException() {
    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockUserRepo.findById(anyString())).thenReturn(Optional.of(testUser));
    when(mockAppRepo.existsByUserIdAndCompanyIdAndJobTitle(anyString(), anyString(), anyString()))
        .thenReturn(false);
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("DB Crash"));

    assertThrows(
        ApplicationServiceFailException.class,
        () ->
            applicationService.createApplication(
                "u1",
                "c1",
                "t1",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                null,
                1,
                null,
                null,
                null));
  }

  // -------------------------------------------------------------------------
  // getApplications
  // -------------------------------------------------------------------------

  @Test
  public void getApplications_whenUserHasApplications_expectList() {
    when(mockAppRepo.findByUserId("user_001")).thenReturn(List.of(existingApp));

    List<ApplicationResponse> result = applicationService.getApplications("user_001");

    assertEquals(1, result.size());
  }

  @Test
  public void getApplications_whenNoApplications_expectEmptyList() {
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

  // -------------------------------------------------------------------------
  // updateApplication
  // -------------------------------------------------------------------------

  @Test
  public void updateApplication_whenValid_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            "Senior SE",
            ApplicationStatus.INTERVIEWING,
            LocalDate.now().plusDays(10),
            "New Desc",
            2,
            "https://new.link",
            LocalDate.now(),
            "New Notes");

    assertEquals("Senior SE", response.getJobTitle());
    assertEquals("New Desc", response.getJobDescription());
    assertEquals("New Notes", response.getNotes());
    assertEquals(ApplicationStatus.INTERVIEWING, response.getStatus());
  }

  @Test
  public void updateApplication_whenNotOwner_expectUnauthorized() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));

    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () ->
            applicationService.updateApplication(
                "wrong_user",
                "app_001",
                "company_001",
                "Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                "Desc",
                1,
                "Link",
                LocalDate.now(),
                "Notes"));
  }

  @Test
  public void updateApplication_whenNoChanges_expectException() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("company_001")).thenReturn(Optional.of(testCompany));

    assertThrows(
        NoChangesDetectedException.class,
        () ->
            applicationService.updateApplication(
                "user_001",
                "app_001",
                "company_001",
                "Software Engineer",
                ApplicationStatus.APPLIED,
                LocalDate.now().plusDays(5),
                null,
                null,
                null,
                null,
                null));
  }

  @Test
  public void updateApplication_whenAppNotFound_expectException() {
    when(mockAppRepo.findById("invalid_app_id")).thenReturn(Optional.empty());

    assertThrows(
        ApplicationNotFoundException.class,
        () ->
            applicationService.updateApplication(
                "user_001",
                "invalid_app_id",
                "c1",
                "t",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                "d",
                1,
                "l",
                LocalDate.now(),
                "n"));
  }

  @Test
  public void updateApplication_whenNewCompanyNotFound_expectException() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("non_existent_company")).thenReturn(Optional.empty());

    assertThrows(
        CompanyNotFoundException.class,
        () ->
            applicationService.updateApplication(
                "user_001",
                "app_001",
                "non_existent_company",
                "Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                "Desc",
                1,
                "Link",
                LocalDate.now(),
                "Notes"));
  }

  @Test
  public void updateApplication_whenDbFails_expectRuntimeException() {
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

    when(mockAppRepo.findById(anyString())).thenReturn(Optional.of(mockApp));
    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("Update DB Crash"));

    assertThrows(
        RuntimeException.class,
        () ->
            applicationService.updateApplication(
                "user_001",
                "app1",
                "c1",
                "New Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                "Desc",
                1,
                "Link",
                LocalDate.now(),
                "Notes"));
  }

  @Test
  public void updateApplication_whenOnlyTitleChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            "Brand New Title",
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null);

    assertEquals("Brand New Title", response.getJobTitle());
  }

  @Test
  public void updateApplication_whenOnlyStatusChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            existingApp.getJobTitle(),
            ApplicationStatus.ACCEPTED,
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes());

    assertEquals(ApplicationStatus.ACCEPTED, response.getStatus());
  }

  @Test
  public void updateApplication_whenOnlyDescriptionChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            "Brand New Description",
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes());

    assertEquals("Brand New Description", response.getJobDescription());
  }

  @Test
  public void updateApplication_whenOnlyLinkChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            "https://new-job-link.com",
            existingApp.getDateApplied(),
            existingApp.getNotes());

    assertEquals("https://new-job-link.com", response.getSourceLink());
  }

  @Test
  public void updateApplication_whenOnlyDateAppliedChanges_expectSuccess() {
    setupExistingAppMock();
    LocalDate newDate = LocalDate.now().minusDays(1);

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            newDate,
            existingApp.getNotes());

    assertEquals(newDate, response.getDateApplied());
  }

  @Test
  public void updateApplication_whenOnlyNotesChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            "Updated internal notes");

    assertEquals("Updated internal notes", response.getNotes());
  }

  @Test
  public void updateApplication_whenOnlyDeadlineChanges_expectSuccess() {
    setupExistingAppMock();
    LocalDate newDeadline = LocalDate.now().plusWeeks(2);

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            newDeadline,
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes());

    assertEquals(newDeadline, response.getApplicationDeadline());
  }

  @Test
  public void updateApplication_whenOnlyPositionsChanges_expectSuccess() {
    setupExistingAppMock();

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_001",
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            99,
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes());

    assertEquals(99, response.getNumPositions());
  }

  @Test
  public void updateApplication_whenCompanyIsChangedToValidCompany_expectSuccess() {
    CompanyModel secondCompany = new CompanyModel("Amazon", "Seattle", "https://amazon.com");
    ReflectionTestUtils.setField(secondCompany, "id", "company_002");

    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));
    when(mockCompRepo.findById("company_002")).thenReturn(Optional.of(secondCompany));
    when(mockAppRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    ApplicationResponse response =
        applicationService.updateApplication(
            "user_001",
            "app_001",
            "company_002",
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            null,
            null,
            null,
            null,
            null);

    assertEquals("company_002", response.getCompanyId());
  }

  // -------------------------------------------------------------------------
  // deleteApplication
  // -------------------------------------------------------------------------

  @Test
  public void deleteApplication_whenValid_expectDeleted() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));

    assertDoesNotThrow(() -> applicationService.deleteApplication("app_001", "user_001"));
    verify(mockAppRepo, times(1)).deleteById("app_001");
  }

  @Test
  public void deleteApplication_whenNotOwner_expectUnauthorized() {
    when(mockAppRepo.findById("app_001")).thenReturn(Optional.of(existingApp));

    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () -> applicationService.deleteApplication("app_001", "malicious_user"));
  }

  @Test
  public void deleteApplication_whenApplicationNotFound_expectException() {
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
    when(mockMongoTemplate.find(any(Query.class), eq(ApplicationModel.class))).thenReturn(results);
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — results
  // -------------------------------------------------------------------------

  @Test
  public void getFilteredApplications_whenNoFilters_expectAllUserApplications() {
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  @Test
  public void getFilteredApplications_whenSearchMatchesCompany_expectResults() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("Goo"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", "Goo", null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
    verify(mockCompRepo, atLeastOnce()).findByCompanyNameContainingIgnoreCase("Goo");
  }

  @Test
  public void getFilteredApplications_whenSearchMatchesCaseInsensitive_expectResults() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("google"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", "google", null, "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  @Test
  public void getFilteredApplications_whenSearchMatchesNoCompany_expectEmptyList() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("nonexistent"))
        .thenReturn(Collections.emptyList());
    mockFilteredQuery(Collections.emptyList(), 0L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", "nonexistent", null, "dateApplied", "desc", 0, 20);

    assertTrue(((List<?>) result.get("applications")).isEmpty());
  }

  @Test
  public void getFilteredApplications_whenBlankSearch_expectAllResults() {
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications(
        "user_001", "   ", null, "dateApplied", "desc", 0, 20);

    verify(mockCompRepo, never()).findByCompanyNameContainingIgnoreCase(anyString());
  }

  @Test
  public void getFilteredApplications_whenStatusMatches_expectFilteredResults() {
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  @Test
  public void getFilteredApplications_whenStatusDoesNotMatch_expectEmptyList() {
    mockFilteredQuery(Collections.emptyList(), 0L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", null, List.of(ApplicationStatus.REJECTED), "dateApplied", "desc", 0, 20);

    assertTrue(((List<?>) result.get("applications")).isEmpty());
  }

  @Test
  public void getFilteredApplications_whenMultipleStatuses_expectMatchingResults() {
    mockFilteredQuery(List.of(existingApp, existingApp), 2L);

    Map<String, Object> result =
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
  public void getFilteredApplications_whenEmptyStatusList_expectAllResults() {
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", null, Collections.emptyList(), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  @Test
  public void getFilteredApplications_whenSortOrderAsc_expectAscendingResults() {
    mockFilteredQuery(List.of(existingApp, existingApp), 2L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "asc", 0, 20);

    assertEquals(2, ((List<?>) result.get("applications")).size());
  }

  @Test
  public void getFilteredApplications_whenPaginated_expectCorrectPage() {
    mockFilteredQuery(List.of(existingApp, existingApp, existingApp), 5L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", null, null, "dateApplied", "desc", 0, 3);

    assertEquals(3, ((List<?>) result.get("applications")).size());
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — query construction verified via ArgumentCaptor
  // -------------------------------------------------------------------------

  @Test
  public void getFilteredApplications_whenSearchProvided_expectCompanyIdsInQuery() {
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
  public void getFilteredApplications_whenStatusProvided_expectStatusInQuery() {
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications(
        "user_001", null, List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertTrue(captor.getValue().toString().contains("APPLIED"));
  }

  @Test
  public void getFilteredApplications_whenSortAsc_expectAscDirectionInQuery() {
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications("user_001", null, null, "dateApplied", "asc", 0, 20);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    String sortObject = captor.getValue().getSortObject().toString();
    assertFalse(sortObject.contains("-1")); // DESC would be -1, not 1
  }

  @Test
  public void getFilteredApplications_whenSortDesc_expectDescDirectionInQuery() {
    mockFilteredQuery(List.of(existingApp), 1L);

    applicationService.getFilteredApplications(
        "user_001", null, null, "dateApplied", "desc", 0, 20);

    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mockMongoTemplate).find(captor.capture(), eq(ApplicationModel.class));
    assertTrue(captor.getValue().getSortObject().toString().contains("-1"));
  }

  @Test
  public void getFilteredApplications_whenPage2Size3_expectSkip6() {
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
  public void getFilteredApplications_whenPaginated_expectCorrectPaginationMetadata() {
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
  public void getFilteredApplications_whenNoResults_expectZeroTotalPages() {
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
  public void getFilteredApplications_whenOnSecondPage_expectHasPreviousTrue() {
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
  public void getFilteredApplications_whenSearchAndStatusCombined_expectFilteredResults() {
    when(mockCompRepo.findByCompanyNameContainingIgnoreCase("Google"))
        .thenReturn(List.of(testCompany));
    mockFilteredQuery(List.of(existingApp), 1L);

    Map<String, Object> result =
        applicationService.getFilteredApplications(
            "user_001", "Google", List.of(ApplicationStatus.APPLIED), "dateApplied", "desc", 0, 20);

    assertEquals(1, ((List<?>) result.get("applications")).size());
  }

  // -------------------------------------------------------------------------
  // getFilteredApplications — failure
  // -------------------------------------------------------------------------

  @Test
  public void getFilteredApplications_whenDbFails_expectServiceFailException() {
    when(mockMongoTemplate.count(any(), eq(ApplicationModel.class)))
        .thenThrow(new RuntimeException("DB Crash"));

    assertThrows(
        ApplicationServiceFailException.class,
        () ->
            applicationService.getFilteredApplications(
                "user_001", null, null, "dateApplied", "desc", 0, 20));
  }
}
