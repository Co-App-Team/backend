package com.backend.coapp._integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.*;
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
@SuppressWarnings("unchecked")
class ReviewUpdateAndHistoryCrossFeatureIntegrationTest {

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
    CompanyModel testCompany =
        new CompanyModel("Tech Innovators", "Remote", "https://techinnovators.com");
    this.companyRepository.save(testCompany);
    this.testCompanyId = testCompany.getId();

    // Create a user
    UserModel testUser =
        new UserModel("user_001", "test@example.com", "password123", "Test", "User", true, 1234);
    testUser.setPassword(passwordEncoder.encode("password123"));
    this.userRepository.save(testUser);
    this.testUserId = testUser.getId();
    this.testUserEmail = testUser.getEmail();
  }

  @Test
  void
      reviewUpdateFlow_whenUserAttemptsDuplicateAndUpdates_expectRatingMathRecalculatedWithIsolation()
          throws Exception {

    assertThat(reviewRepository.count()).isZero();
    assertThat(companyRepository.count()).isOne();
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

    // Post a Review (Rating: 1)
    CreateReviewRequest createReviewRequest =
        new CreateReviewRequest(
            1, "Terrible experience, poorly managed.", "Junior Dev", "Fall", 2023);

    mockMvc
        .perform(
            post("/api/companies/{companyId}/reviews", testCompanyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReviewRequest))
                .cookie(this.authCookie))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reviewId").isNotEmpty())
        .andExpect(jsonPath("$.companyId").value(testCompanyId))
        .andExpect(jsonPath("$.userId").value(testUserId))
        .andExpect(jsonPath("$.rating").value(1));

    assertThat(reviewRepository.count()).isOne();

    mockMvc
        .perform(get("/api/companies/{companyId}", testCompanyId).cookie(this.authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.company.avgRating").value(1.0));

    // Post another Review for the same company (duplicate): error
    CreateReviewRequest duplicateReviewRequest =
        new CreateReviewRequest(5, "Wait, nevermind it was great!", "Junior Dev", "Fall", 2023);

    mockMvc
        .perform(
            post("/api/companies/{companyId}/reviews", testCompanyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateReviewRequest))
                .cookie(this.authCookie))
        .andExpect(status().is4xxClientError());

    // Verify the second review was not saved
    assertThat(reviewRepository.count()).isOne();

    // Update the existing Review (Rating: 4)
    UpdateReviewRequest updateReviewRequest =
        new UpdateReviewRequest(
            4,
            "Actually, things improved drastically after I spoke to management.",
            "Junior Dev",
            "Fall",
            2023);

    mockMvc
        .perform(
            put("/api/companies/{companyId}/reviews", testCompanyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReviewRequest))
                .cookie(this.authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rating").value(4));

    // Verify the db still has only 1 review and updated content
    assertThat(reviewRepository.count()).isOne();
    ReviewModel updatedReview = reviewRepository.findAll().iterator().next();
    assertThat(updatedReview.getRating()).isEqualTo(4);
    assertThat(updatedReview.getComment()).contains("improved drastically");

    // Cross-feature check
    assertThat(companyRepository.count()).isOne();
    assertThat(userRepository.count()).isOne();

    mockMvc
        .perform(get("/api/companies/{companyId}", testCompanyId).cookie(this.authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.company.avgRating").value(4.0));
  }
}
