package com.backend.coapp._integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.*;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
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
@SuppressWarnings("unchecked")
class SearchToApplicationCrossFeatureIntegrationTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private CompanyRepository companyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ReviewRepository reviewRepository;

  @Autowired private ApplicationRepository applicationRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private PasswordEncoder passwordEncoder;

  private String testUserId;
  private String testUserEmail;
  private Cookie authCookie;

  @BeforeEach
  void setUp() {

    this.applicationRepository.deleteAll();
    this.reviewRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.userRepository.deleteAll();

    // Create the main user
    UserModel testUser =
        new UserModel("user_001", "test@example.com", "", "Test", "User", true, 1234);
    testUser.setPassword(passwordEncoder.encode("password123"));
    this.userRepository.save(testUser);
    this.testUserId = testUser.getId();
    this.testUserEmail = testUser.getEmail();

    // Create companies
    CompanyModel company1 =
        new CompanyModel("Tech Alpha", "San Francisco", "https://techalpha.com");
    companyRepository.save(company1);

    CompanyModel company2 = new CompanyModel("Tech Beta", "New York", "https://techbeta.com");
    companyRepository.save(company2);

    CompanyModel company3 =
        new CompanyModel("Finance Delta", "Chicago", "https://financedelta.com");
    companyRepository.save(company3);
  }

  @Test
  void
      searchToApplyFlow_whenUserSearchesPaginatesReviewsAndApplies_expectUserDashboardSliceUpdatedWithIsolation()
          throws Exception {

    assertThat(companyRepository.count()).isEqualTo(3);
    assertThat(userRepository.count()).isOne();
    assertThat(reviewRepository.count()).isZero();
    assertThat(applicationRepository.count()).isZero();

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

    // The user searches for "Tech" and paginates companies (page 0, size 1)
    MvcResult searchResult =
        mockMvc
            .perform(
                get("/api/companies")
                    .param("search", "Tech")
                    .param("usePagination", "true")
                    .param("page", "0")
                    .param("size", "1")
                    .cookie(authCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.companies.length()").value(1))
            .andExpect(jsonPath("$.pagination.totalItems").value(2))
            .andReturn();

    String targetCompanyId =
        com.jayway.jsonpath.JsonPath.read(
            searchResult.getResponse().getContentAsString(), "$.companies[0].companyId");

    // User leaves a review
    CreateReviewRequest reviewRequest =
        new CreateReviewRequest(5, "Great place!", "Dev", "Fall", 2023);
    mockMvc
        .perform(
            post("/api/companies/{companyId}/reviews", targetCompanyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest))
                .cookie(authCookie))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rating").value(5));

    // Paginated reviews on company profile (page 0, size 2)
    mockMvc
        .perform(
            get("/api/companies/{companyId}", targetCompanyId)
                .param("page", "0")
                .param("size", "2")
                .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviews.length()").value(1))
        .andExpect(jsonPath("$.reviewsPagination.totalItems").value(1))
        .andExpect(jsonPath("$.reviewsPagination.totalPages").value(1));

    CreateApplicationRequest createRequest =
        CreateApplicationRequest.builder()
            .userId(testUserId)
            .companyId(targetCompanyId)
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.now().plusDays(30))
            .jobDescription("Full-stack Spring Boot + React role.")
            .numPositions(2)
            .sourceLink("https://testcorp.com/jobs/123")
            .dateApplied(LocalDate.now())
            .notes("Created during integration test flow.")
            .build();

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .cookie(this.authCookie))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.applicationId").isNotEmpty())
        .andExpect(jsonPath("$.companyId").value(targetCompanyId))
        .andExpect(jsonPath("$.jobTitle").value("Software Engineer"))
        .andExpect(jsonPath("$.status").value("APPLIED"))
        .andReturn();

    // User's application dashboard has updated for their new app
    assertThat(applicationRepository.findByUserId(testUserId)).hasSize(1);

    // Cross-feature check
    assertThat(companyRepository.count()).isEqualTo(3);
    assertThat(reviewRepository.count()).isOne();
    assertThat(applicationRepository.count()).isOne();
  }
}
