package com.backend.coapp._integration;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.ApplicationService;
import com.backend.coapp.service.CompanyService;
import com.backend.coapp.service.ReviewService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class CrossFeatureSearchToApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ApplicationService applicationService;

    private String testUserId;

    @BeforeEach
    void setUp() {

        this.applicationRepository.deleteAll();
        this.reviewRepository.deleteAll();
        this.companyRepository.deleteAll();
        this.userRepository.deleteAll();

        // Create the main user
        UserModel testUser = new UserModel(
                "user_001", "test@example.com", "password123", "Test", "User", true, 1234
        );
        this.userRepository.save(testUser);
        this.testUserId = testUser.getId();

        // existing companies
        companyService.createCompany("Tech Alpha", "San Francisco", "https://techalpha.com");
        companyService.createCompany("Tech Beta", "New York", "https://techbeta.com");
        companyService.createCompany("Finance Delta", "Chicago", "https://financedelta.com");
    }

    @Test
    void searchToApplyFlow_whenUserSearchesPaginatesReviewsAndApplies_expectUserDashboardSliceUpdatedWithIsolation() {

        assertThat(companyRepository.count()).isEqualTo(3);
        assertThat(userRepository.count()).isOne();
        assertThat(reviewRepository.count()).isZero();
        assertThat(applicationRepository.count()).isZero();

        UserModel loggedInUser = userRepository.findById(testUserId)
                .orElseThrow(() -> new AssertionError("User should exist in the DB"));

        // The user searches for "Tech" and paginates companies
        Pageable companyPageRequest = PageRequest.of(0, 1);
        Page<CompanyResponse> companyPage = companyService.getAllCompanies("Tech", companyPageRequest);

        assertThat(companyPage.getTotalElements()).isEqualTo(2);
        assertThat(companyPage.getTotalPages()).isEqualTo(2);
        assertThat(companyPage.getContent()).hasSize(1);

        String targetCompanyId = companyPage.getContent().get(0).getCompanyId();

        // Multiple users leave reviews
        reviewService.createReview(targetCompanyId, testUserId, "Test User", 5, "Great place!", "Dev", "Fall", 2023);
        reviewService.createReview(targetCompanyId, "random_user_1", "Alice", 4, "Good perks.", "Dev", "Fall", 2023);
        reviewService.createReview(targetCompanyId, "random_user_2", "Bob", 3, "Average.", "Dev", "Fall", 2023);

        // Paginated reviews on company profile
        Pageable reviewPageRequest = PageRequest.of(0, 2);
        Page<ReviewModel> reviewPage = reviewService.getReviewsByCompanyId(targetCompanyId, reviewPageRequest);
        assertThat(reviewPage.getTotalElements()).isEqualTo(3);
        assertThat(reviewPage.getTotalPages()).isEqualTo(2);
        assertThat(reviewPage.getContent()).hasSize(2);

        // User applies
        ApplicationResponse createdApp = applicationService.createApplication(
                loggedInUser.getId(),
                targetCompanyId,
                "Backend Engineer",
                ApplicationStatus.APPLIED,
                LocalDate.now().plusDays(10),
                "Software engineering role",
                3,
                "https://techalpha.com/jobs",
                LocalDate.now(),
                "Note: Found via paginated search + reviews."
        );

        // Users application dashboard has updated to include their new application
        assertThat(applicationRepository.findByUserId(testUserId)).hasSize(1); // Proxy for dashboard

        // Cross-feature check
        assertThat(companyRepository.count()).isEqualTo(3);
        assertThat(reviewRepository.count()).isEqualTo(3);
        assertThat(applicationRepository.count()).isOne();
    }
}
