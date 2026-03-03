package com.backend.coapp._integration;

import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.exception.ReviewAlreadyExistsException;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.CompanyService;
import com.backend.coapp.service.ReviewService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
class CrossFeatureReviewUpdateAndHistoryIntegrationTest {

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
        CompanyModel testCompany = new CompanyModel("Tech Innovators", "Remote", "https://techinnovators.com");
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
    void reviewUpdateFlow_whenUserAttemptsDuplicateAndUpdates_expectRatingMathRecalculatedWithIsolation() {

        assertThat(reviewRepository.count()).isZero();
        assertThat(companyRepository.count()).isOne();
        assertThat(userRepository.count()).isOne();

        // User Logs in
        UserModel loggedInUser = userRepository.findById(testUserId)
                .orElseThrow(() -> new AssertionError("User should exist in the DB"));
        String authorName = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();

        // Post a Review (Rating: 1)
        ReviewModel initialReview = reviewService.createReview(
                testCompanyId,
                testUserId,
                authorName,
                1,
                "Terrible experience, poorly managed.",
                "Junior Dev",
                "Fall",
                2023
        );

        assertThat(initialReview).isNotNull();
        assertThat(initialReview.getRating()).isEqualTo(1);
        assertThat(reviewRepository.count()).isOne();

        // Verify Company avgRating correctly calculated to 1.0
        CompanyResponse companyAfterFirstReview = companyService.getCompanyById(testCompanyId);
        assertThat(companyAfterFirstReview.getAvgRating()).isEqualTo(1.0);

        // Post another Review for the same company
        assertThrows(ReviewAlreadyExistsException.class, () -> {
            reviewService.createReview(
                    testCompanyId,
                    testUserId,
                    authorName,
                    5,
                    "Wait, nevermind it was great!",
                    "Junior Dev",
                    "Fall",
                    2023
            );
        }, "Expected service to throw an exception when a user tries to leave a second review for the same company.");

        // Verify the second review was not saved
        assertThat(reviewRepository.count()).isOne();

        // Update the existing Review (Rating: 4)
        ReviewModel updatedReview = reviewService.updateReview(
                testCompanyId,
                testUserId,
                4,
                "Actually, things improved drastically after I spoke to management.",
                "Junior Dev",
                "Fall",
                2023
        );

        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getRating()).isEqualTo(4);
        assertThat(updatedReview.getComment()).contains("improved drastically");

        // Verify the db still has only 1 review
        assertThat(reviewRepository.count()).isOne();

        // Cross-feature check
        assertThat(companyRepository.count()).isOne();
        assertThat(userRepository.count()).isOne();

        // Check updated rating
        CompanyResponse companyAfterUpdate = companyService.getCompanyById(testCompanyId);
        assertThat(companyAfterUpdate.getAvgRating()).isEqualTo(4.0);
    }
}
