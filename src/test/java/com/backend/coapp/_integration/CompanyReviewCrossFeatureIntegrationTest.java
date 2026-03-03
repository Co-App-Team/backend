package com.backend.coapp._integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.CreateReviewRequest;
import com.backend.coapp.dto.request.LoginRequest;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import com.backend.coapp.repository.UserRepository;
import jakarta.servlet.http.Cookie;
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
class CompanyReviewCrossFeatureIntegrationTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private CompanyRepository companyRepository;

  @Autowired private ReviewRepository reviewRepository;

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

    this.reviewRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.userRepository.deleteAll();

    // Create a company
    CompanyModel testCompany = new CompanyModel("Test Corp Inc.", "Remote", "https://testcorp.com");
    this.companyRepository.save(testCompany);
    this.testCompanyId = testCompany.getId();

    // Create a user
    UserModel testUser =
        new UserModel("user_001", "test@example.com", "", "Test", "User", true, 1234);
    testUser.setPassword(passwordEncoder.encode("password123"));
    this.userRepository.save(testUser);
    this.testUserId = testUser.getId();
    this.testUserEmail = testUser.getEmail();
  }

  @Test
  void companyReviewFlow_whenUserLogsInAndPostsReview_expectRatingUpdatedWithCrossFeatureIsolation()
      throws Exception {

    // A user exists and a company exists in the database
    assertThat(reviewRepository.count()).isZero();
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

    // The user views the company profile before leaving a review
    mockMvc
        .perform(get("/api/companies/{id}", testCompanyId).cookie(this.authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.company.avgRating").value(0.0));

    // The logged-in user posts a review for the company
    CreateReviewRequest createReviewRequest =
        new CreateReviewRequest(
            5, "Amazing company with great remote culture!", "Software Engineer", "Summer", 2023);

    mockMvc
        .perform(
            post("/api/companies/{companyId}/reviews", testCompanyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReviewRequest))
                .cookie(this.authCookie))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reviewId").isNotEmpty()) // Fixed: "reviewId" instead of "id"
        .andExpect(jsonPath("$.companyId").value(testCompanyId))
        .andExpect(jsonPath("$.userId").value(testUserId))
        .andExpect(jsonPath("$.rating").value(5))
        .andReturn();

    // Verify the review was created in DB
    assertThat(reviewRepository.count()).isOne();
    ReviewModel createdReview = reviewRepository.findAll().iterator().next();
    assertThat(createdReview.getCompanyId()).isEqualTo(testCompanyId);
    assertThat(createdReview.getUserId()).isEqualTo(testUserId);
    assertThat(createdReview.getRating()).isEqualTo(5);

    // Cross-feature check
    assertThat(companyRepository.count()).isOne();
    assertThat(userRepository.count()).isOne();

    // The user re-fetches the company page and expects to see the updated average rating
    mockMvc
        .perform(get("/api/companies/{id}", testCompanyId).cookie(this.authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.company").exists())
        .andExpect(jsonPath("$.company.avgRating").value(5.0));

    // The user logs out
    mockMvc.perform(get("/api/auth/logout").cookie(this.authCookie)).andExpect(status().isOk());
  }
}
