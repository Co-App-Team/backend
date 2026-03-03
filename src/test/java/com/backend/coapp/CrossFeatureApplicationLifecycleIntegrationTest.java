package com.backend.coapp;

import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserModel;

import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.ApplicationService;

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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class CrossFeatureApplicationLifecycleIntegrationTest {

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

    private String testCompanyId;
    private String testUserId;

    @BeforeEach
    void setUp() {

        this.applicationRepository.deleteAll();
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
    @DisplayName("createApplication_whenUserAndCompanyExist_expectSuccessfulCRUDWithCrossFeatureIsolation")
    void createApplication_whenUserAndCompanyExist_expectSuccessfulCRUDWithCrossFeatureIsolation() {

        // A user exists and a company exists in the database
        assertThat(applicationRepository.count()).isZero();
        assertThat(companyRepository.count()).isOne();
        assertThat(userRepository.count()).isOne();


        // The user logs in
        userRepository.findById(testUserId)
                .orElseThrow(() -> new AssertionError("User should exist in the DB"));

        // The user creates a new application using that companies id
        ApplicationResponse createdApp = applicationService.createApplication(
                testUserId,
                testCompanyId,
                "Software Engineer",
                ApplicationStatus.APPLIED,
                LocalDate.now().plusDays(30),
                "Full-stack Spring Boot + React role.",
                2,
                "https://testcorp.com/jobs/123",
                LocalDate.now(),
                "Created during integration test flow."
        );

        // The user wants to see what is in the application they created
        assertThat(createdApp).isNotNull();
        assertThat(createdApp.getApplicationId()).isNotBlank();
        String appId = createdApp.getApplicationId();
        ApplicationModel fetchedApp = applicationRepository.findById(appId)
                .orElseThrow(() -> new AssertionError("Application should exist in the DB"));
        assertThat(fetchedApp.getUserId()).isEqualTo(testUserId);
        assertThat(fetchedApp.getCompanyId()).isEqualTo(testCompanyId);
        assertThat(fetchedApp.getJobTitle()).isEqualTo("Software Engineer");
        assertThat(fetchedApp.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(applicationRepository.count()).isOne();
        // Cross-feature check
        assertThat(companyRepository.count()).isOne();
        assertThat(userRepository.count()).isOne();

        // The user then updates the application
        ApplicationResponse updatedApp = applicationService.updateApplication(
                testUserId,
                appId,
                testCompanyId,
                "Senior Software Engineer",
                ApplicationStatus.INTERVIEWING,
                null,
                null,
                null,
                null,
                null,
                "Updated: Scheduled interview."
        );
        assertThat(updatedApp.getJobTitle()).isEqualTo("Senior Software Engineer");
        assertThat(updatedApp.getStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);

        ApplicationModel refetchedApplication = applicationRepository.findById(appId).get();
        assertThat(refetchedApplication.getJobTitle()).isEqualTo("Senior Software Engineer");

        // The user then decides to delete the Application
        applicationService.deleteApplication(appId, testUserId);

        // Verify deleted
        assertThat(applicationRepository.count()).isZero();
        assertThat(applicationRepository.findById(appId)).isEmpty();
        // Verify Cross-feature isolation
        assertThat(companyRepository.count()).isOne();
        assertThat(userRepository.count()).isOne();
    }
}
