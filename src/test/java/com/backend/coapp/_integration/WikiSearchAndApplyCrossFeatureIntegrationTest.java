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
class WikiSearchAndApplyCrossFeatureIntegrationTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private ApplicationRepository applicationRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private PasswordEncoder passwordEncoder;

  private String testUserId;
  private String testUserEmail;
  private String amazonId;
  private String googleId;
  private Cookie authCookie;

  @BeforeEach
  void setUp() {

    this.applicationRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.userRepository.deleteAll();

    // Create searchable companies
    CompanyModel amazon = new CompanyModel("Amazon", "Seattle", "https://amazon.com");
    this.companyRepository.save(amazon);
    this.amazonId = amazon.getId();

    CompanyModel google = new CompanyModel("Samsung", "Mountain View", "https://google.com");
    this.companyRepository.save(google);
    this.googleId = google.getId();

    CompanyModel microsoft = new CompanyModel("Google", "Redmond", "https://microsoft.com");
    this.companyRepository.save(microsoft);

    // Create a user
    UserModel testUser =
        new UserModel("user_001", "test@example.com", "", "Test", "User", true, 1234);
    testUser.setPassword(passwordEncoder.encode("password123"));
    this.userRepository.save(testUser);
    this.testUserId = testUser.getId();
    this.testUserEmail = testUser.getEmail();
  }

  @Test
  void wikiBulkApplyFlow_whenUserSearchesAppliesMultipleAndProgressesStatus_expectCompanyIsolation()
      throws Exception {

    assertThat(applicationRepository.count()).isZero();
    assertThat(companyRepository.count()).isEqualTo(3);
    assertThat(userRepository.count()).isOne();

    // User logs in
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

    // Wiki search for "o"
    mockMvc
        .perform(get("/api/companies").param("search", "o").cookie(this.authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.companies").isArray())
        .andExpect(jsonPath("$.companies.length()").value(2));

    // User applies to Amazon
    CreateApplicationRequest amazonApplyReq =
        CreateApplicationRequest.builder()
            .userId(testUserId)
            .companyId(amazonId)
            .jobTitle("SDE II")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.now().plusDays(14))
            .jobDescription("AWS role")
            .numPositions(4)
            .sourceLink("https://amazon.jobs/123")
            .dateApplied(LocalDate.now())
            .notes("apply #1 via wiki.")
            .build();

    MvcResult amazonResult =
        mockMvc
            .perform(
                post("/api/application")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(amazonApplyReq))
                    .cookie(this.authCookie))
            .andExpect(status().isCreated())
            .andReturn();

    ApplicationResponse amazonApp =
        objectMapper.readValue(
            amazonResult.getResponse().getContentAsString(), ApplicationResponse.class);

    // User applies to Google
    CreateApplicationRequest googleApplyReq =
        CreateApplicationRequest.builder()
            .userId(testUserId)
            .companyId(googleId)
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.INTERVIEWING)
            .applicationDeadline(LocalDate.now().plusDays(21))
            .jobDescription("Cloud role")
            .numPositions(5)
            .sourceLink("https://google.jobs/456")
            .dateApplied(LocalDate.now())
            .notes("apply #2 via wiki.")
            .build();

    MvcResult googleResult =
        mockMvc
            .perform(
                post("/api/application")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(googleApplyReq))
                    .cookie(this.authCookie))
            .andExpect(status().isCreated())
            .andReturn();

    ApplicationResponse googleApp =
        objectMapper.readValue(
            googleResult.getResponse().getContentAsString(), ApplicationResponse.class);

    assertThat(applicationRepository.count()).isEqualTo(2);

    // User gets an offer from Amazon and updates the status
    UpdateApplicationRequest updateAmazonReq =
        UpdateApplicationRequest.builder()
            .companyId(amazonId)
            .jobTitle("SDE II")
            .status(ApplicationStatus.OFFER_RECEIVED)
            .notes("Offer received!")
            .build();

    mockMvc
        .perform(
            put("/api/application/{id}", amazonApp.getApplicationId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAmazonReq))
                .cookie(this.authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("OFFER_RECEIVED"));

    // User deletes their Google application
    mockMvc
        .perform(
            delete("/api/application/{id}", googleApp.getApplicationId()).cookie(this.authCookie))
        .andExpect(status().isOk());

    // Verify 1 app remains
    assertThat(applicationRepository.count()).isOne();
    ApplicationModel remainingApp = applicationRepository.findAll().iterator().next();
    assertThat(remainingApp.getStatus()).isEqualTo(ApplicationStatus.OFFER_RECEIVED);
    assertThat(remainingApp.getCompanyId()).isEqualTo(amazonId);

    // Cross-feature checks
    assertThat(companyRepository.count()).isEqualTo(3);
    assertThat(userRepository.count()).isOne();

    // User logs out
    mockMvc.perform(get("/api/auth/logout").cookie(this.authCookie)).andExpect(status().isOk());
  }
}
