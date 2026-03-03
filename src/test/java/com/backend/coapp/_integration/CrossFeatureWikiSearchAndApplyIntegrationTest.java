package com.backend.coapp._integration;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.ApplicationService;
import com.backend.coapp.service.CompanyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class CrossFeatureWikiSearchAndApplyIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CompanyService companyService;

    private String testUserId;
    private String amazonId;
    private String googleId;

    @BeforeEach
    void setUp() {

        this.applicationRepository.deleteAll();
        this.companyRepository.deleteAll();
        this.userRepository.deleteAll();

        // Create searchable companies
        CompanyModel amazon = new CompanyModel("Amazon", "Seattle", "https://amazon.com");
        this.companyRepository.save(amazon);
        this.amazonId = amazon.getId();

        CompanyModel google = new CompanyModel("Google", "Mountain View", "https://google.com");
        this.companyRepository.save(google);
        this.googleId = google.getId();

        CompanyModel microsoft = new CompanyModel("Microsoft", "Redmond", "https://microsoft.com");
        this.companyRepository.save(microsoft);

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
    void wikiBulkApplyFlow_whenUserSearchesAppliesMultipleAndProgressesStatus_expectCompanyIsolation() {

        assertThat(applicationRepository.count()).isZero();
        assertThat(companyRepository.count()).isEqualTo(3);
        assertThat(userRepository.count()).isOne();

        UserModel loggedInUser = userRepository.findById(testUserId)
                .orElseThrow(() -> new AssertionError("User should exist in the DB"));

        // Wiki search for "o"
        List<CompanyResponse> searchResults = companyService.getAllCompanies("o");
        assertThat(searchResults).hasSize(2); // Google + Microsoft

        // User applies to 2 companies from the search page
        ApplicationResponse amazonApp = applicationService.createApplication(
                testUserId, amazonId, "SDE II", ApplicationStatus.APPLIED,
                LocalDate.now().plusDays(14), "AWS role", 4, "https://amazon.jobs/123",
                LocalDate.now(), "apply #1 via wiki."
        );
        ApplicationResponse googleApp = applicationService.createApplication(
                testUserId, googleId, "Software Engineer", ApplicationStatus.INTERVIEWING, // Different status
                LocalDate.now().plusDays(21), "Cloud role", 5, "https://google.jobs/456",
                LocalDate.now(), "apply #2 via wiki."
        );

        assertThat(applicationRepository.count()).isEqualTo(2);

        // The user gets an offer from amazon
        applicationService.updateApplication(testUserId, amazonApp.getApplicationId(), amazonId,
                "SDE II", ApplicationStatus.OFFER_RECEIVED, null, null, null, null, null, "Offer received!");
        // The user deletes their google application
        applicationService.deleteApplication(googleApp.getApplicationId(), testUserId);

        // 1 application remains
        assertThat(applicationRepository.count()).isOne();
        ApplicationModel remainingApp = applicationRepository.findByUserId(testUserId).getFirst();
        assertThat(remainingApp.getStatus()).isEqualTo(ApplicationStatus.OFFER_RECEIVED);
        assertThat(remainingApp.getCompanyId()).isEqualTo(amazonId);

        // Cross-feature check
        assertThat(companyRepository.count()).isEqualTo(3);
        assertThat(userRepository.count()).isOne();
    }
}
