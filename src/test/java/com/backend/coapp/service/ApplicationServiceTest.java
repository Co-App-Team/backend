package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class ApplicationServiceTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private CompanyRepository companyRepository;
  @Autowired private UserRepository userRepository;

  private ApplicationService applicationService;

  private ApplicationRepository mockAppRepo;
  private CompanyRepository mockCompRepo;
  private UserRepository mockUserRepo;

  private CompanyModel testCompany;
  private UserModel testUser;
  private ApplicationModel existingApp;

  @BeforeEach
  public void setUp() {
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

    this.existingApp =
        ApplicationModel.builder()
            .userId("user_001")
            .companyId(testCompany.getId())
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.now().plusDays(5))
            .build();
    this.applicationRepository.save(existingApp);

    // Service with real repositories for Integration Tests
    this.applicationService =
        new ApplicationService(applicationRepository, companyRepository, userRepository);

    // Mocks for Unit Tests
    this.mockAppRepo = Mockito.mock(ApplicationRepository.class);
    this.mockCompRepo = Mockito.mock(CompanyRepository.class);
    this.mockUserRepo = Mockito.mock(UserRepository.class);
  }

  // INTEGRATION TESTS (Using Testcontainers)

  @Test
  public void constructor_expectSameInitInstance() {
    assertSame(this.applicationRepository, this.applicationService.getApplicationRepository());
    assertSame(this.companyRepository, this.applicationService.getCompanyRepository());
  }

  // Create Application Integration Tests

  @Test
  public void createApplication_whenValid_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.createApplication(
            testUser.getId(),
            testCompany.getId(),
            "Data Scientist",
            ApplicationStatus.APPLIED,
            LocalDate.now(),
            "Desc",
            1,
            "https://link.com",
            LocalDate.now(),
            "Notes",
            LocalDate.now());

    assertNotNull(response);
    assertEquals("Data Scientist", response.getJobTitle());
    assertEquals(testCompany.getId(), response.getCompanyId());
  }

  @Test
  public void createApplication_whenCompanyNotFound_expectException() {
    assertThrows(
        CompanyNotFoundException.class,
        () ->
            this.applicationService.createApplication(
                "user_001",
                "invalid_id",
                "Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                null,
                1,
                null,
                null,
                null,
                LocalDate.now()));
  }

  @Test
  public void createApplication_whenUserNotFound_expectException() {
    assertThrows(
        UserNotFoundException.class,
        () ->
            this.applicationService.createApplication(
                "invalid_user",
                testCompany.getId(),
                "Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                null,
                1,
                null,
                null,
                null,
                null));
  }

  @Test
  public void createApplication_whenDuplicate_expectException() {
    assertThrows(
        DuplicateApplicationException.class,
        () ->
            this.applicationService.createApplication(
                "user_001",
                testCompany.getId(),
                "Software Engineer",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                null,
                1,
                null,
                null,
                null,
                null));
  }

  // Update Application Integration Tests

  @Test
  public void updateApplication_whenValid_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            testCompany.getId(),
            "Senior SE",
            ApplicationStatus.INTERVIEWING,
            LocalDate.now().plusDays(10),
            "New Desc",
            2,
            "https://new.link",
            LocalDate.now(),
            "New Notes",
            LocalDate.now());

    assertEquals("Senior SE", response.getJobTitle());
    assertEquals("New Desc", response.getJobDescription());
    assertEquals("New Notes", response.getNotes());
    assertEquals(ApplicationStatus.INTERVIEWING, response.getStatus());
  }

  @Test
  public void updateApplication_whenNotOwner_expectUnauthorized() {
    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () ->
            this.applicationService.updateApplication(
                "wrong_user",
                existingApp.getId(),
                testCompany.getId(),
                "Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                "Desc",
                1,
                "Link",
                LocalDate.now(),
                "Notes",
                LocalDate.now()));
  }

  @Test
  public void updateApplication_whenNoChanges_expectException() {
    assertThrows(
        NoChangesDetectedException.class,
        () ->
            this.applicationService.updateApplication(
                "user_001",
                existingApp.getId(),
                testCompany.getId(),
                "Software Engineer",
                ApplicationStatus.APPLIED,
                LocalDate.now().plusDays(5),
                null,
                null,
                null,
                null,
                null,
                null));
  }

  @Test
  public void updateApplication_whenAppNotFound_expectException() {
    assertThrows(
        ApplicationNotFoundException.class,
        () ->
            this.applicationService.updateApplication(
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
                "n",
                LocalDate.now()));
  }

  @Test
  public void updateApplication_whenNewCompanyNotFound_expectException() {
    assertThrows(
        CompanyNotFoundException.class,
        () ->
            this.applicationService.updateApplication(
                "user_001",
                existingApp.getId(),
                "non_existent_company",
                "Title",
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                "Desc",
                1,
                "Link",
                LocalDate.now(),
                "Notes",
                LocalDate.now()));
  }

  // Delete Application Integration Tests

  @Test
  public void deleteApplication_whenValid_expectDeleted() {
    assertDoesNotThrow(
        () -> this.applicationService.deleteApplication(existingApp.getId(), "user_001"));
    assertTrue(this.applicationRepository.findById(existingApp.getId()).isEmpty());
  }

  @Test
  public void deleteApplication_whenNotOwner_expectUnauthorized() {
    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () -> this.applicationService.deleteApplication(existingApp.getId(), "malicious_user"));
  }

  @Test
  public void deleteApplication_whenApplicationNotFound_expectException() {
    assertThrows(
        ApplicationNotFoundException.class,
        () -> this.applicationService.deleteApplication("non_existent_id", "user_001"));
  }

  // Get Applications Integration Tests

  @Test
  public void getApplications_whenUserHasApplications_expectList() {
    List<ApplicationResponse> responses = this.applicationService.getApplications("user_001");

    assertNotNull(responses);
    assertFalse(responses.isEmpty());
    assertEquals(1, responses.size());
    assertEquals(existingApp.getJobTitle(), responses.get(0).getJobTitle());
  }

  @Test
  public void getApplications_whenUserHasNoApplications_expectEmptyList() {
    // Create a user with no apps
    UserModel user2 =
        new UserModel("user_002", "test2@example.com", "pwd", "Jane", "Doe", true, 5678);
    userRepository.save(user2);

    List<ApplicationResponse> responses = this.applicationService.getApplications("user_002");

    assertNotNull(responses);
    assertTrue(responses.isEmpty());
  }

  // UNIT TESTS

  @Test
  public void createApplication_whenDatabaseFails_expectServiceFailException() {
    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo);

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
                LocalDate.now(),
                null,
                1,
                null,
                null,
                null,
                null));
  }

  @Test
  public void updateApplication_whenDbFails_expectRuntimeException() {

    ApplicationService serviceWithMocks =
        new ApplicationService(mockAppRepo, mockCompRepo, mockUserRepo);

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
            serviceWithMocks.updateApplication(
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
                "Notes",
                LocalDate.now()));
  }

  @Test
  public void updateApplication_whenOnlyTitleChanges_expectSuccess() {

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
            null);

    assertEquals("Brand New Title", response.getJobTitle());
    assertEquals(testCompany.getId(), response.getCompanyId());
  }

  @Test
  public void updateApplication_whenCompanyIsChangedToValidCompany_expectSuccess() {

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
            null);

    assertEquals(secondCompany.getId(), response.getCompanyId());
  }

  @Test
  public void updateApplication_whenOnlyDescriptionChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            "Brand New Description", // Changed
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDate());

    assertEquals("Brand New Description", response.getJobDescription());
  }

  @Test
  public void updateApplication_whenOnlyLinkChanges_expectSuccess() {
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
            "https://new-job-link.com", // Changed
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDate());

    assertEquals("https://new-job-link.com", response.getSourceLink());
  }

  @Test
  public void updateApplication_whenOnlyDateAppliedChanges_expectSuccess() {
    LocalDate newDate = LocalDate.now().minusDays(1);
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
            newDate, // Changed
            existingApp.getNotes(),
            existingApp.getInterviewDate());

    assertEquals(newDate, response.getDateApplied());
  }

  @Test
  public void updateApplication_whenOnlyNotesChanges_expectSuccess() {
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
            "Updated internal notes", // Changed
            existingApp.getInterviewDate());

    assertEquals("Updated internal notes", response.getNotes());
  }

  @Test
  public void updateApplication_whenOnlyStatusChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            ApplicationStatus.ACCEPTED, // Changed
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDate());

    assertEquals(ApplicationStatus.ACCEPTED, response.getStatus());
  }

  @Test
  public void updateApplication_whenOnlyDeadlineChanges_expectSuccess() {
    LocalDate newDeadline = LocalDate.now().plusWeeks(2);
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            newDeadline, // Changed
            existingApp.getJobDescription(),
            existingApp.getNumPositions(),
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDate());

    assertEquals(newDeadline, response.getApplicationDeadline());
  }

  @Test
  public void updateApplication_whenOnlyPositionsChanges_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            "user_001",
            existingApp.getId(),
            existingApp.getCompanyId(),
            existingApp.getJobTitle(),
            existingApp.getStatus(),
            existingApp.getApplicationDeadline(),
            existingApp.getJobDescription(),
            99, // Changed
            existingApp.getSourceLink(),
            existingApp.getDateApplied(),
            existingApp.getNotes(),
            existingApp.getInterviewDate());

    assertEquals(99, response.getNumPositions());
  }
}
