package com.backend.coapp._integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.CreateApplicationRequest;
import com.backend.coapp.dto.request.LoginRequest;
import com.backend.coapp.dto.request.UpdateApplicationRequest;
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
class ApplicationFilterCrossFeatureIntegrationTest {

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

  @BeforeEach
  void setUp() {
    this.applicationRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.userRepository.deleteAll();

    // A company already exists
    CompanyModel testCompany = new CompanyModel("Filter Corp", "Remote", "https://filter.com");
    this.companyRepository.save(testCompany);
    this.testCompanyId = testCompany.getId();

    // A user already exists
    UserModel testUser =
        new UserModel(
            "user_filter",
            "filter@example.com",
            passwordEncoder.encode("password123"),
            "Filter",
            "User",
            true,
            1234);
    this.userRepository.save(testUser);
    this.testUserId = testUser.getId();
    this.testUserEmail = testUser.getEmail();
  }

  @Test
  void filterFlow_whenUserCreatesAppliesAndFilters_expectCorrectStatusAndSort() throws Exception {

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

    Cookie authCookie = loginResult.getResponse().getCookie("Authorization");
    assertThat(authCookie).isNotNull();

    // The user creates Application A (APPLIED, yesterday)
    CreateApplicationRequest reqA =
        CreateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("Dev A")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.now().plusDays(10))
            .dateApplied(LocalDate.now().minusDays(1))
            .build();
    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqA))
                .cookie(authCookie))
        .andExpect(status().isCreated());

    // The user creates Application B (INTERVIEWING, today)
    CreateApplicationRequest reqB =
        CreateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("Dev B")
            .status(ApplicationStatus.INTERVIEWING)
            .applicationDeadline(LocalDate.now().plusDays(10))
            .dateApplied(LocalDate.now())
            .build();
    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqB))
                .cookie(authCookie))
        .andExpect(status().isCreated());

    // The user creates Application C (REJECTED, tomorrow)
    CreateApplicationRequest reqC =
        CreateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("Dev C")
            .status(ApplicationStatus.REJECTED)
            .applicationDeadline(LocalDate.now().plusDays(10))
            .dateApplied(LocalDate.now().plusDays(1))
            .build();
    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqC))
                .cookie(authCookie))
        .andExpect(status().isCreated());

    // Verify all 3 applications exist in DB
    assertThat(applicationRepository.count()).isEqualTo(3);

    // The user filters applications by status INTERVIEWING
    mockMvc
        .perform(get("/api/application").param("status", "INTERVIEWING").cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applications.length()").value(1))
        .andExpect(jsonPath("$.applications[0].status").value("INTERVIEWING"));

    // The user sorts applications by dateApplied ascending
    mockMvc
        .perform(
            get("/api/application")
                .param("sortBy", "dateApplied")
                .param("sortOrder", "asc")
                .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applications.length()").value(3))
        .andExpect(jsonPath("$.applications[0].jobTitle").value("Dev A"))
        .andExpect(jsonPath("$.applications[2].jobTitle").value("Dev C"));

    // The user updates Application A to OFFER_RECEIVED
    ApplicationModel appA =
        applicationRepository.findAll().stream()
            .filter(app -> app.getJobTitle().equals("Dev A"))
            .findFirst()
            .orElseThrow();

    UpdateApplicationRequest updateReq =
        UpdateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("Dev A")
            .status(ApplicationStatus.OFFER_RECEIVED)
            .build();

    mockMvc
        .perform(
            put("/api/application/{id}", appA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq))
                .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("OFFER_RECEIVED"));

    // Verify DB update
    ApplicationModel updatedAppA = applicationRepository.findById(appA.getId()).orElseThrow();
    assertThat(updatedAppA.getStatus()).isEqualTo(ApplicationStatus.OFFER_RECEIVED);

    // The user filters by APPLIED status and expects empty result
    mockMvc
        .perform(get("/api/application").param("status", "APPLIED").cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applications.length()").value(0));

    // Cross-feature check
    assertThat(companyRepository.count()).isOne();
    assertThat(userRepository.count()).isOne();

    // The user logs out
    mockMvc.perform(get("/api/auth/logout").cookie(authCookie)).andExpect(status().isOk());
  }
}
