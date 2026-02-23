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

  // --- Create Application Integration Tests ---

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
            "Notes");

    assertNotNull(response);
    assertEquals("Data Scientist", response.getJobTitle());
    assertEquals(testUser.getId(), response.getUserId());
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
                null));
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
                null));
  }

  // --- Update Application Integration Tests ---

  @Test
  public void updateApplication_whenValid_expectSuccess() {
    ApplicationResponse response =
        this.applicationService.updateApplication(
            existingApp.getId(),
            "user_001",
            testCompany.getId(),
            "Senior SE",
            "New Desc",
            "https://new.link",
            "New Notes");

    assertEquals("Senior SE", response.getJobTitle());
    assertEquals("New Desc", response.getJobDescription());
    assertEquals("New Notes", response.getNotes());
  }

  @Test
  public void updateApplication_whenNotOwner_expectUnauthorized() {
    assertThrows(
        UnauthorizedApplicationAccessException.class,
        () ->
            this.applicationService.updateApplication(
                existingApp.getId(),
                "wrong_user",
                testCompany.getId(),
                "Title",
                "Desc",
                null,
                null));
  }

  @Test
  public void updateApplication_whenNoChanges_expectException() {
    assertThrows(
        NoChangesDetectedException.class,
        () ->
            this.applicationService.updateApplication(
                existingApp.getId(),
                "user_001",
                testCompany.getId(),
                "Software Engineer",
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
                "invalid_app_id", "user_001", "c1", "t", "d", "l", "n"));
  }

  @Test
  public void updateApplication_whenNewCompanyNotFound_expectException() {
    assertThrows(
        CompanyNotFoundException.class,
        () ->
            this.applicationService.updateApplication(
                existingApp.getId(),
                "user_001",
                "non_existent_company",
                "Title",
                "Desc",
                null,
                null));
  }

  // --- Delete Application Integration Tests ---

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
    when(mockApp.getJobDescription()).thenReturn("Old Desc");
    when(mockApp.getSourceLink()).thenReturn("http://old.com");

    when(mockAppRepo.findById(anyString())).thenReturn(Optional.of(mockApp));
    when(mockCompRepo.findById(anyString())).thenReturn(Optional.of(testCompany));
    when(mockAppRepo.save(any())).thenThrow(new RuntimeException("Update DB Crash"));

    assertThrows(
        RuntimeException.class,
        () ->
            serviceWithMocks.updateApplication(
                "app1", "user_001", "c1", "New Title", "Desc", "Link", "Notes"));
  }
}
