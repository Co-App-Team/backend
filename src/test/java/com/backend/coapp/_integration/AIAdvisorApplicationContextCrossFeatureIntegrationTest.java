package com.backend.coapp._integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.CreateApplicationRequest;
import com.backend.coapp.dto.request.LoginRequest;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.genAI.GenAIService;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AIAdvisorApplicationContextCrossFeatureIntegrationTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private CompanyRepository companyRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PasswordEncoder passwordEncoder;

  @MockitoBean private GenAIService genAIService;

  private String testCompanyId;
  private String testUserId;
  private String testUserEmail;

  @BeforeEach
  void setUp() {
    this.applicationRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.userRepository.deleteAll();

    // A company already exists
    CompanyModel testCompany = new CompanyModel("AI Corp", "Remote", "https://ai.com");
    this.companyRepository.save(testCompany);
    this.testCompanyId = testCompany.getId();

    // A user already exists
    UserModel testUser =
        new UserModel(
            "user_ai", "ai@example.com", passwordEncoder.encode("pass"), "AI", "User", true, 1234);
    this.userRepository.save(testUser);
    this.testUserId = testUser.getId();
    this.testUserEmail = testUser.getEmail();
  }

  @Test
  void aiAdvisorFlow_whenUserPromptsWithApplicationContext_expectQuotaDecrementAndCorrectDBState()
      throws Exception {

    // A user exists and a company exists in the database
    assertThat(applicationRepository.count()).isZero();
    assertThat(companyRepository.count()).isOne();
    assertThat(userRepository.count()).isOne();

    // A user logs in
    LoginRequest loginRequest = new LoginRequest(testUserEmail, "pass");
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

    // The user checks their initial AI quota
    MvcResult quotaResult =
        mockMvc
            .perform(get("/api/resume-ai-advisor/remaining-quota").cookie(authCookie))
            .andExpect(status().isOk())
            .andReturn();

    int initialQuota =
        JsonPath.read(quotaResult.getResponse().getContentAsString(), "$.remainingQuota");

    // The user creates an application to provide context for the AI
    CreateApplicationRequest appReq =
        CreateApplicationRequest.builder()
            .companyId(testCompanyId)
            .jobTitle("AI Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.now().plusDays(10))
            .build();

    MvcResult appResult =
        mockMvc
            .perform(
                post("/api/application")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(appReq))
                    .cookie(authCookie))
            .andExpect(status().isCreated())
            .andReturn();

    String appId = JsonPath.read(appResult.getResponse().getContentAsString(), "$.applicationId");

    // Verify application exists in DB
    ApplicationModel createdApp =
        applicationRepository
            .findById(appId)
            .orElseThrow(() -> new AssertionError("Application should exist"));
    assertThat(createdApp.getJobTitle()).isEqualTo("AI Engineer");
    assertThat(applicationRepository.count()).isOne();

    // Mock GenAI Service response
    when(genAIService.generateResponse(anyString())).thenReturn("Mocked AI Advice");

    // The user prompts the AI advisor using the application context
    Map<String, String> promptReq = new HashMap<>();
    promptReq.put("userPrompt", "Review my resume");
    promptReq.put("applicationId", appId);

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(promptReq))
                .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.response").value("Mocked AI Advice"));

    // Verify the quota was decremented
    mockMvc
        .perform(get("/api/resume-ai-advisor/remaining-quota").cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.remainingQuota").value(initialQuota - 1));

    // Cross-feature check
    assertThat(companyRepository.count()).isOne();
    assertThat(userRepository.count()).isOne();

    // The user logs out
    mockMvc.perform(get("/api/auth/logout").cookie(authCookie)).andExpect(status().isOk());
  }
}
