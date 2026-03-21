package com.backend.coapp._integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.CreateApplicationRequest;
import com.backend.coapp.dto.request.LoginRequest;
import com.backend.coapp.dto.request.UpdateApplicationRequest;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class InterviewFilterCrossFeatureIntegrationTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private CompanyRepository companyRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PasswordEncoder passwordEncoder;

  private String testCompanyId;
  private String testUserEmail;

  @BeforeEach
  void setUp() {
    this.applicationRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.userRepository.deleteAll();

    CompanyModel testCompany = new CompanyModel("Interview Inc", "Remote", "https://interview.com");
    this.companyRepository.save(testCompany);
    this.testCompanyId = testCompany.getId();

    UserModel testUser =
        new UserModel(
            "user_int",
            "int@example.com",
            passwordEncoder.encode("password123"),
            "Int",
            "User",
            true,
            1234);
    this.userRepository.save(testUser);
    this.testUserEmail = testUser.getEmail();
  }

  @Test
  void interviewFilterFlow_whenUserSchedulesAndFiltersByDate_expectCorrectApplications()
      throws Exception {
    assertThat(applicationRepository.count()).isZero();

    // Login
    LoginRequest loginRequest = new LoginRequest(testUserEmail, "password123");
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

    Cookie authCookie = loginResult.getResponse().getCookie("Authorization");
    assertThat(authCookie).isNotNull();

    // Create App A (Interview Future month 1)
    LocalDateTime interviewDateA = LocalDate.now().plusMonths(1).atTime(10, 0);
    LocalDateTime interviewDateB = LocalDate.now().plusMonths(2).atTime(10, 0);

    CreateApplicationRequest reqA =
        CreateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("Job A")
            .status(ApplicationStatus.INTERVIEWING)
            .applicationDeadline(LocalDate.now().plusDays(10))
            .interviewDateTime(interviewDateA)
            .build();
    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqA))
                .cookie(authCookie))
        .andExpect(status().isCreated());

    // Create App B (Interview future month 2)
    CreateApplicationRequest reqB =
        CreateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("Job B")
            .status(ApplicationStatus.INTERVIEWING)
            .applicationDeadline(LocalDate.now().plusDays(10))
            .interviewDateTime(interviewDateB)
            .build();
    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqB))
                .cookie(authCookie))
        .andExpect(status().isCreated());

    // Feature 5: Filter Interviews by Date Range
    String startDate = LocalDate.now().plusWeeks(2).toString();
    String endDate = LocalDate.now().plusWeeks(6).toString();

    mockMvc
        .perform(
            get("/api/application/interviews")
                .param("startDate", startDate)
                .param("endDate", endDate)
                .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1)) // Should only be App A
        .andExpect(jsonPath("$[0].jobTitle").value("Job A"));

    // Feature 5: Validation Error (Missing endDate)
    mockMvc
        .perform(
            get("/api/application/interviews").param("startDate", startDate).cookie(authCookie))
        .andExpect(status().isBadRequest());

    // Update App B date to fall within range
    String jobBId =
        applicationRepository.findAll().stream()
            .filter(app -> app.getJobTitle().equals("Job B"))
            .findFirst()
            .get()
            .getId();

    LocalDateTime newDateB = LocalDate.now().plusWeeks(4).atTime(10, 0);

    UpdateApplicationRequest updateReq =
        UpdateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("Job B")
            .interviewDateTime(newDateB)
            .build();

    mockMvc
        .perform(
            put("/api/application/{id}", jobBId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq))
                .cookie(authCookie))
        .andExpect(status().isOk());

    // Feature 5: Re-filter, should now have 2 results
    mockMvc
        .perform(
            get("/api/application/interviews")
                .param("startDate", startDate)
                .param("endDate", endDate)
                .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }
}
