package com.backend.coapp._integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.CreateApplicationRequest;
import com.backend.coapp.dto.request.LoginRequest;
import com.backend.coapp.dto.request.UpdateApplicationRequest;
import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationLifecycleCrossFeatureIntegrationTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private ApplicationRepository applicationRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private PasswordEncoder passwordEncoder;

  private String testCompanyId;
  private String testUserId;
  private String testUserEmail;
  private Cookie authCookie;

  @BeforeEach
  void setUp() {
    this.applicationRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.userRepository.deleteAll();

    // A company already exists
    CompanyModel testCompany = new CompanyModel("Test Corp Inc.", "Remote", "https://testcorp.com");
    this.companyRepository.save(testCompany);
    this.testCompanyId = testCompany.getId();

    // A user already exists
    UserModel testUser =
        new UserModel("user_001", "test@example.com", "", "Test", "User", true, 1234);

    testUser.setPassword(passwordEncoder.encode("password123"));
    this.userRepository.save(testUser);
    this.testUserId = testUser.getId();
    this.testUserEmail = testUser.getEmail();
  }

  @Test
  void whenUserLogsInAndCreatesUpdatesAndDeletesApplicationAndLogsOut_ExpectCorrectDataInDB()
      throws Exception {

    // A user exists and a company exists in the database
    assertThat(applicationRepository.count()).isZero();
    assertThat(companyRepository.count()).isOne();
    assertThat(userRepository.count()).isOne();

    // A user logs in
    LoginRequest loginRequest = new LoginRequest(testUserEmail, "password123");

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged in successfully."))
            .andReturn();

    this.authCookie = loginResult.getResponse().getCookie("Authorization");
    assertThat(this.authCookie).isNotNull();

    // The user creates a new application using that companies id
    CreateApplicationRequest createRequest =
        CreateApplicationRequest.builder()
            .userId(testUserId)
            .companyId(testCompanyId)
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.now().plusDays(30))
            .jobDescription("Full-stack Spring Boot + React role.")
            .numPositions(2)
            .sourceLink("https://testcorp.com/jobs/123")
            .dateApplied(LocalDate.now())
            .notes("Created during integration test flow.")
            .build();

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/application")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
                    .cookie(this.authCookie)) // Real JWT cookie for auth
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.applicationId").isNotEmpty())
            .andExpect(jsonPath("$.companyId").value(testCompanyId))
            .andExpect(jsonPath("$.jobTitle").value("Software Engineer"))
            .andExpect(jsonPath("$.status").value("APPLIED"))
            .andReturn();

    // The user wants to see what is in the application they created
    String responseBody = createResult.getResponse().getContentAsString();
    ApplicationResponse createdApp =
        objectMapper.readValue(responseBody, ApplicationResponse.class);
    String appId = createdApp.getApplicationId();
    ApplicationModel fetchedApp =
        applicationRepository
            .findById(appId)
            .orElseThrow(() -> new AssertionError("Application should exist in the DB"));
    assertThat(fetchedApp.getUserId()).isEqualTo(testUserId);
    assertThat(fetchedApp.getCompanyId()).isEqualTo(testCompanyId);
    assertThat(fetchedApp.getJobTitle()).isEqualTo("Software Engineer");
    assertThat(fetchedApp.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
    assertThat(applicationRepository.count()).isOne();

    // Cross-feature check
    assertThat(companyRepository.count()).isOne();
    assertThat(userRepository.count()).isOne();

    // The user then updates the application
    UpdateApplicationRequest updateRequest =
        UpdateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("Senior Software Engineer")
            .status(ApplicationStatus.INTERVIEWING)
            .notes("Updated: Scheduled interview.")
            .build();

    mockMvc
        .perform(
            put("/api/application/{id}", appId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .cookie(this.authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.jobTitle").value("Senior Software Engineer"))
        .andExpect(jsonPath("$.status").value("INTERVIEWING"))
        .andExpect(jsonPath("$.notes").value("Updated: Scheduled interview."));

    // Verify Database after update
    ApplicationModel refetchedApp =
        applicationRepository
            .findById(appId)
            .orElseThrow(() -> new AssertionError("Application should still exist"));
    assertThat(refetchedApp.getJobTitle()).isEqualTo("Senior Software Engineer");
    assertThat(refetchedApp.getStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);

    // The user then decides to delete the Application
    mockMvc
        .perform(delete("/api/application/{id}", appId).cookie(this.authCookie))
        .andExpect(status().isOk());

    // The user logs out
    mockMvc.perform(get("/api/auth/logout").cookie(this.authCookie)).andExpect(status().isOk());

    // Verify deleted
    assertThat(applicationRepository.count()).isZero();
    assertThat(applicationRepository.findById(appId)).isEmpty();
    // Verify Cross-feature
    assertThat(companyRepository.count()).isOne();
    assertThat(userRepository.count()).isOne();
  }
}
