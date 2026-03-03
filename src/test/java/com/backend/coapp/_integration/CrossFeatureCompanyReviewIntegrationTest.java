package com.backend.coapp._integration;

import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.CompanyService;
import com.backend.coapp.service.ReviewService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class CrossFeatureCompanyReviewIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ReviewService reviewService;

    private String testCompanyId;
    private String testUserId;

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
        UserModel testUser = new UserModel(
                "user_001",
                "test@example.com",
                "password123",
                "Test", "User",
                true,
                1234
        );
        this.userRepository.save(testUser);
        this.testUserId = testUser.getId();
    }

    @Test
    void companyReviewFlow_whenUserLogsInAndPostsReview_expectRatingUpdatedWithCrossFeatureIsolation() {

        assertThat(reviewRepository.count()).isZero();
        assertThat(companyRepository.count()).isOne();
        assertThat(userRepository.count()).isOne();

        // The user logs in
        UserModel loggedInUser = userRepository.findById(testUserId)
                .orElseThrow(() -> new AssertionError("User should exist in the DB"));
        String authorName = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();

        // The user views the company profile before leaving a review
        CompanyResponse initialCompanyView = companyService.getCompanyById(testCompanyId);
        
        assertThat(initialCompanyView).isNotNull();
        Double initialRating = initialCompanyView.getAvgRating() != null ? initialCompanyView.getAvgRating() : 0.0;

        // The logged-in user posts a review for the company
        ReviewModel createdReview = reviewService.createReview(
                testCompanyId,
                loggedInUser.getId(),
                authorName,
                5,
                "Amazing company with great remote culture!",
                "Software Engineer",
                "Summer",
                2023
        );

        // Verify the review
        assertThat(createdReview).isNotNull();
        assertThat(createdReview.getId()).isNotBlank();
        assertThat(createdReview.getCompanyId()).isEqualTo(testCompanyId);
        assertThat(createdReview.getUserId()).isEqualTo(testUserId);
        assertThat(createdReview.getRating()).isEqualTo(5);
        
        assertThat(reviewRepository.count()).isOne();
        
        // Cross-feature check
        assertThat(companyRepository.count()).isOne();
        assertThat(userRepository.count()).isOne();

        // The user re-fetches the company page and expects to see the updated average rating
        CompanyResponse updatedCompanyView = companyService.getCompanyById(testCompanyId);
        
        assertThat(updatedCompanyView).isNotNull();
        Double updatedRating = updatedCompanyView.getAvgRating();
        
        // Assert that the avgRating increased
        assertThat(updatedRating)
                .isNotNull()
                .isGreaterThan(initialRating)
                .isEqualTo(5.0);
    }
}
