package com.backend.coapp.controller;

import com.backend.coapp.dto.request.CreateCompanyRequest;
import com.backend.coapp.dto.request.CreateReviewRequest;
import com.backend.coapp.dto.request.UpdateReviewRequest;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Feature 4: Rate My Co-op
 * 
 * Tests all API endpoints for Company and Review functionality:
 * - GET /api/companies (with and without pagination, search)
 * - GET /api/companies/{companyId}
 * - POST /api/companies
 * - POST /api/companies/{companyId}/reviews
 * - PUT /api/companies/{companyId}/reviews/{reviewId}
 * - DELETE /api/companies/{companyId}/reviews/{reviewId}
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=3025"
})
public class RateMyCoopIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Create ObjectMapper manually instead of autowiring to avoid bean issues
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserModel testUser1;
    private UserModel testUser2;
    private String jwtToken1;
    private String jwtToken2;
    private Cookie authCookie1;
    private Cookie authCookie2;

    @BeforeEach
    public void setUp() {
        // Clean up all data before each test
        reviewRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser1 = new UserModel(
                "testuser1@example.com",
                passwordEncoder.encode("password123"),
                "Test",
                "User1",
                UserModel.DEFAULT_VERIFICATION_CODE
        );
        testUser1.setVerified(true);
        testUser1 = userRepository.save(testUser1);

        testUser2 = new UserModel(
                "testuser2@example.com",
                passwordEncoder.encode("password123"),
                "Test",
                "User2",
                UserModel.DEFAULT_VERIFICATION_CODE
        );
        testUser2.setVerified(true);
        testUser2 = userRepository.save(testUser2);

        // Generate JWT tokens for authentication
        jwtToken1 = jwtService.generateToken(testUser1);
        jwtToken2 = jwtService.generateToken(testUser2);

        // Create auth cookies
        authCookie1 = new Cookie("token", jwtToken1);
        authCookie2 = new Cookie("token", jwtToken2);
    }

    @AfterEach
    public void tearDown() {
        // Clean up all test data
        reviewRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ==================== COMPANY TESTS ====================

    @Test
    public void testCreateCompany_Success() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "Niche Technology",
                "Winnipeg, MB",
                "https://www.niche.com"
        );

        mockMvc.perform(post("/api/companies")
                        .with(user(testUser1))  // Use Spring Security test support
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").exists())
                .andExpect(jsonPath("$.companyName").value("Niche Technology"))
                .andExpect(jsonPath("$.location").value("Winnipeg, MB"))
                .andExpect(jsonPath("$.website").value("https://www.niche.com"))
                .andExpect(jsonPath("$.avgRating").value(0.0));
    }

    @Test
    public void testCreateCompany_MissingRequiredFields() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "",  // Empty company name
                "Winnipeg, MB",
                "https://www.example.com"
        );

        mockMvc.perform(post("/api/companies")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_HAS_NULL_OR_EMPTY_FIELD"))
                .andExpect(jsonPath("$.message").value("Invalid inputs of the request. Fields (companyName, location, website) cannot be null or empty."));
    }

    @Test
    public void testCreateCompany_InvalidWebsite() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "Test Company",
                "Winnipeg, MB",
                "not-a-valid-url"
        );

        mockMvc.perform(post("/api/companies")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_WEBSITE"))
                .andExpect(jsonPath("$.message").value("The website URL is not valid."));
    }

    @Test
    public void testCreateCompany_DuplicateCompany() throws Exception {
        // Create first company
        CompanyModel existingCompany = new CompanyModel(
                "Niche Technology",
                "Winnipeg, MB",
                "https://www.niche.com"
        );
        companyRepository.save(existingCompany);

        // Try to create duplicate
        CreateCompanyRequest request = new CreateCompanyRequest(
                "Niche Technology",
                "Different Location",
                "https://www.different.com"
        );

        mockMvc.perform(post("/api/companies")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("COMPANY_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("A company with this name already exists."));
    }

    @Test
    public void testCreateCompany_Unauthorized() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "Test Company",
                "Winnipeg, MB",
                "https://www.test.com"
        );

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllCompanies_WithoutPagination() throws Exception {
        // Create test companies
        CompanyModel company1 = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
        CompanyModel company2 = new CompanyModel("Varian", "Winnipeg", "https://varian.com");
        CompanyModel company3 = new CompanyModel("Amazon", "Seattle", "https://amazon.com");
        companyRepository.save(company1);
        companyRepository.save(company2);
        companyRepository.save(company3);

        mockMvc.perform(get("/api/companies")
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies").isArray())
                .andExpect(jsonPath("$.companies", hasSize(3)))
                .andExpect(jsonPath("$.companies[*].companyName", containsInAnyOrder("Niche", "Varian", "Amazon")));
    }

    @Test
    public void testGetAllCompanies_WithPagination() throws Exception {
        // Create multiple companies
        for (int i = 1; i <= 5; i++) {
            CompanyModel company = new CompanyModel(
                    "Company " + i,
                    "Location " + i,
                    "https://company" + i + ".com"
            );
            companyRepository.save(company);
        }

        mockMvc.perform(get("/api/companies")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .param("usePagination", "true")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies", hasSize(2)))
                .andExpect(jsonPath("$.pagination.currentPage").value(0))
                .andExpect(jsonPath("$.pagination.totalPages").value(3))
                .andExpect(jsonPath("$.pagination.totalItems").value(5))
                .andExpect(jsonPath("$.pagination.itemsPerPage").value(2))
                .andExpect(jsonPath("$.pagination.hasNext").value(true))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(false));
    }

    @Test
    public void testGetAllCompanies_WithSearch() throws Exception {
        CompanyModel company1 = new CompanyModel("Niche Technology", "Winnipeg", "https://niche.com");
        CompanyModel company2 = new CompanyModel("Varian Medical", "Winnipeg", "https://varian.com");
        CompanyModel company3 = new CompanyModel("Amazon", "Seattle", "https://amazon.com");
        companyRepository.save(company1);
        companyRepository.save(company2);
        companyRepository.save(company3);

        mockMvc.perform(get("/api/companies")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .param("search", "Tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies", hasSize(1)))
                .andExpect(jsonPath("$.companies[0].companyName").value("Niche Technology"));
    }

    @Test
    public void testGetAllCompanies_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/api/companies")
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies").isArray())
                .andExpect(jsonPath("$.companies", hasSize(0)));
    }

    @Test
    public void testGetAllCompanies_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetCompanyById_Success() throws Exception {
        CompanyModel company = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
        company = companyRepository.save(company);

        mockMvc.perform(get("/api/companies/" + company.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.companyId").value(company.getId()))
                .andExpect(jsonPath("$.company.companyName").value("Niche"))
                .andExpect(jsonPath("$.company.location").value("Winnipeg"))
                .andExpect(jsonPath("$.company.website").value("https://niche.com"))
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews", hasSize(0)))
                .andExpect(jsonPath("$.reviewsPagination.totalItems").value(0));
    }

    @Test
    public void testGetCompanyById_WithReviews() throws Exception {
        CompanyModel company = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
        company = companyRepository.save(company);

        // Add reviews
        ReviewModel review1 = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                5,
                "Great company!",
                "Software Developer",
                "Summer",
                2025
        );
        ReviewModel review2 = new ReviewModel(
                company.getId(),
                testUser2.getId(),
                testUser2.getFirstName(),
                4,
                "Good experience",
                "QA Tester",
                "Fall",
                2024
        );
        reviewRepository.save(review1);
        reviewRepository.save(review2);

        mockMvc.perform(get("/api/companies/" + company.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.companyId").value(company.getId()))
                .andExpect(jsonPath("$.reviews", hasSize(2)))
                .andExpect(jsonPath("$.reviewsPagination.totalItems").value(2));
    }

    @Test
    public void testGetCompanyById_NotFound() throws Exception {
        mockMvc.perform(get("/api/companies/nonexistent123")
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("COMPANY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Company with this companyId does not exist."));
    }

    @Test
    public void testGetCompanyById_Unauthorized() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Test", "Location", "https://test.com")
        );

        mockMvc.perform(get("/api/companies/" + company.getId()))
                .andExpect(status().isForbidden());
    }

    // ==================== REVIEW TESTS ====================

    @Test
    public void testCreateReview_Success() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        CreateReviewRequest request = new CreateReviewRequest(
                5,
                "Excellent work environment and great mentorship!",
                "Software Developer",
                "Summer",
                2025
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").exists())
                .andExpect(jsonPath("$.companyId").value(company.getId()))
                .andExpect(jsonPath("$.userId").value(testUser1.getId()))
                .andExpect(jsonPath("$.authorName").value(testUser1.getFirstName() + " " + testUser1.getLastName()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent work environment and great mentorship!"))
                .andExpect(jsonPath("$.jobTitle").value("Software Developer"))
                .andExpect(jsonPath("$.workTermSeason").value("Summer"))
                .andExpect(jsonPath("$.workTermYear").value(2025));
    }

    @Test
    public void testCreateReview_WithoutComment() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        CreateReviewRequest request = new CreateReviewRequest(
                4,
                null,  // No comment
                "QA Tester",
                "Winter",
                2024
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").isEmpty());
    }

    @Test
    public void testCreateReview_MissingRating() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        CreateReviewRequest request = new CreateReviewRequest(
                null,  // Missing rating
                "Great company",
                "Developer",
                "Summer",
                2025
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_HAS_NULL_OR_EMPTY_FIELD"))
                .andExpect(jsonPath("$.message").value("Invalid inputs of the request. Rating is required and must be between 1 and 5."));
    }

    @Test
    public void testCreateReview_InvalidRatingRange() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        CreateReviewRequest request = new CreateReviewRequest(
                6,  // Rating out of range
                "Great company",
                "Developer",
                "Summer",
                2025
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_HAS_NULL_OR_EMPTY_FIELD"))
                .andExpect(jsonPath("$.message").value("Invalid inputs of the request. Rating is required and must be between 1 and 5."));
    }

    @Test
    public void testCreateReview_CommentTooLong() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        String longComment = "a".repeat(2001);  // Exceeds 2000 character limit
        CreateReviewRequest request = new CreateReviewRequest(
                5,
                longComment,
                "Developer",
                "Summer",
                2025
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_HAS_NULL_OR_EMPTY_FIELD"))
                .andExpect(jsonPath("$.message").value("Invalid inputs of the request. comment exceeds maximum length of 2000 characters."));
    }

    @Test
    public void testCreateReview_InvalidWorkTermSeason() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        CreateReviewRequest request = new CreateReviewRequest(
                5,
                "Great!",
                "Developer",
                "Spring",  // Invalid season
                2025
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_HAS_NULL_OR_EMPTY_FIELD"))
                .andExpect(jsonPath("$.message").value("Invalid inputs of the request. Work term season must be one of: Fall, Winter, Summer"));
    }

    @Test
    public void testCreateReview_InvalidWorkTermYear() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        CreateReviewRequest request = new CreateReviewRequest(
                5,
                "Great!",
                "Developer",
                "Summer",
                2030  // Future year
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_HAS_NULL_OR_EMPTY_FIELD"))
                .andExpect(jsonPath("$.message", containsString("Invalid inputs of the request. Work term year must be between 1950 and 2026")));
    }

    @Test
    public void testCreateReview_CompanyNotFound() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                5,
                "Great company",
                "Developer",
                "Summer",
                2025
        );

        mockMvc.perform(post("/api/companies/nonexistent123/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("COMPANY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Company with this companyId does not exist."));
    }

    @Test
    public void testCreateReview_DuplicateReview() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        // Create first review
        ReviewModel existingReview = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good company",
                "Developer",
                "Summer",
                2024
        );
        reviewRepository.save(existingReview);

        // Try to create duplicate review for same user and company
        CreateReviewRequest request = new CreateReviewRequest(
                5,
                "Changed my mind",
                "Developer",
                "Summer",
                2025
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("REVIEW_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("You have already submitted a review for this company."));
    }

    @Test
    public void testCreateReview_Unauthorized() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        CreateReviewRequest request = new CreateReviewRequest(
                5,
                "Great!",
                "Developer",
                "Summer",
                2025
        );

        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateReview_Success() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        ReviewModel review = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good company",
                "Developer",
                "Summer",
                2024
        );
        review = reviewRepository.save(review);

        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                5,
                "Actually, it was excellent!",
                "Senior Developer",
                "Fall",
                2025
        );

        mockMvc.perform(put("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(review.getId()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Actually, it was excellent!"))
                .andExpect(jsonPath("$.jobTitle").value("Senior Developer"))
                .andExpect(jsonPath("$.workTermSeason").value("Fall"))
                .andExpect(jsonPath("$.workTermYear").value(2025));
    }

    @Test
    public void testUpdateReview_PartialUpdate() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        ReviewModel review = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good company",
                "Developer",
                "Summer",
                2024
        );
        review = reviewRepository.save(review);

        // Update only rating
        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                5,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Good company"))  // Unchanged
                .andExpect(jsonPath("$.jobTitle").value("Developer"));  // Unchanged
    }

    @Test
    public void testUpdateReview_NoFieldsProvided() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        ReviewModel review = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good company",
                "Developer",
                "Summer",
                2024
        );
        review = reviewRepository.save(review);

        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                null, null, null, null, null
        );

        mockMvc.perform(put("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_HAS_NULL_OR_EMPTY_FIELD"))
                .andExpect(jsonPath("$.message").value("Invalid inputs of the request. At least one field must be provided to update the review."));
    }

    @Test
    public void testUpdateReview_InvalidRating() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        ReviewModel review = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good company",
                "Developer",
                "Summer",
                2024
        );
        review = reviewRepository.save(review);

        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                0,  // Invalid rating
                null,
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUEST_HAS_NULL_OR_EMPTY_FIELD"))
                .andExpect(jsonPath("$.message").value("Invalid inputs of the request. Rating must be between 1 and 5."));
    }

    @Test
    public void testUpdateReview_NotOwner() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        // User 1 creates review
        ReviewModel review = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good company",
                "Developer",
                "Summer",
                2024
        );
        review = reviewRepository.save(review);

        // User 2 tries to update
        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                5, null, null, null, null
        );

        mockMvc.perform(put("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                        .with(user(testUser2))  // Different user
                        .cookie(authCookie2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("REVIEW_NOT_OWNED"))
                .andExpect(jsonPath("$.message").value("You can only update your own reviews."));
    }

    @Test
    public void testUpdateReview_NotFound() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                5, null, null, null, null
        );

        mockMvc.perform(put("/api/companies/" + company.getId() + "/reviews/nonexistent123")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("REVIEW_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Review with the provided id does not exist for this company."));
    }

    @Test
    public void testUpdateReview_Unauthorized() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        ReviewModel review = reviewRepository.save(new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good",
                "Developer",
                "Summer",
                2024
        ));

        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                5, null, null, null, null
        );

        mockMvc.perform(put("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteReview_Success() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        ReviewModel review = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good company",
                "Developer",
                "Summer",
                2024
        );
        review = reviewRepository.save(review);

        mockMvc.perform(delete("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review deleted successfully."));

        // Verify review was deleted
        assert !reviewRepository.existsById(review.getId());
    }

    @Test
    public void testDeleteReview_NotOwner() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        // User 1 creates review
        ReviewModel review = new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good company",
                "Developer",
                "Summer",
                2024
        );
        review = reviewRepository.save(review);

        // User 2 tries to delete
        mockMvc.perform(delete("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                        .with(user(testUser2))  // Different user
                        .cookie(authCookie2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("REVIEW_NOT_OWNED"))
                .andExpect(jsonPath("$.message").value("You can only delete your own reviews."));

        // Verify review still exists
        assert reviewRepository.existsById(review.getId());
    }

    @Test
    public void testDeleteReview_NotFound() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        mockMvc.perform(delete("/api/companies/" + company.getId() + "/reviews/nonexistent123")
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("REVIEW_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Review with the provided id does not exist for this company."));
    }

    @Test
    public void testDeleteReview_Unauthorized() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        ReviewModel review = reviewRepository.save(new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                4,
                "Good",
                "Developer",
                "Summer",
                2024
        ));

        mockMvc.perform(delete("/api/companies/" + company.getId() + "/reviews/" + review.getId()))
                .andExpect(status().isForbidden());
    }

    // ==================== RATING CALCULATION TESTS ====================

    @Test
    public void testAverageRating_UpdatesOnReviewCreate() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        // Create first review with rating 5
        CreateReviewRequest request1 = new CreateReviewRequest(
                5, "Excellent!", "Dev", "Summer", 2025
        );
        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                .with(user(testUser1))
                .cookie(authCookie1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Create second review with rating 3
        CreateReviewRequest request2 = new CreateReviewRequest(
                3, "Average", "QA", "Fall", 2024
        );
        mockMvc.perform(post("/api/companies/" + company.getId() + "/reviews")
                .with(user(testUser2))
                .cookie(authCookie2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Check company average rating should be 4.0
        mockMvc.perform(get("/api/companies/" + company.getId())
                .with(user(testUser1))
                .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.avgRating").value(4.0));
    }

    @Test
    public void testAverageRating_UpdatesOnReviewUpdate() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        // Create review with rating 3
        ReviewModel review = reviewRepository.save(new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                3,
                "Average",
                "Developer",
                "Summer",
                2024
        ));

        // Update rating to 5
        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                5, null, null, null, null
        );
        mockMvc.perform(put("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                .with(user(testUser1))
                .cookie(authCookie1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Check updated average rating
        mockMvc.perform(get("/api/companies/" + company.getId())
                .with(user(testUser1))
                .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.avgRating").value(5.0));
    }

    @Test
    public void testAverageRating_UpdatesOnReviewDelete() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        // Create two reviews
        ReviewModel review1 = reviewRepository.save(new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                5,
                "Great!",
                "Developer",
                "Summer",
                2024
        ));

        ReviewModel review2 = reviewRepository.save(new ReviewModel(
                company.getId(),
                testUser2.getId(),
                testUser2.getFirstName(),
                3,
                "OK",
                "QA",
                "Fall",
                2024
        ));

        // Delete one review
        mockMvc.perform(delete("/api/companies/" + company.getId() + "/reviews/" + review1.getId())
                .with(user(testUser1))
                .cookie(authCookie1))
                .andExpect(status().isOk());

        // Check average rating should now be 3.0
        mockMvc.perform(get("/api/companies/" + company.getId())
                .with(user(testUser1))
                .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.avgRating").value(3.0));
    }

    @Test
    public void testAverageRating_ResetsToZeroWhenLastReviewDeleted() throws Exception {
        CompanyModel company = companyRepository.save(
                new CompanyModel("Niche", "Winnipeg", "https://niche.com")
        );

        // Create one review
        ReviewModel review = reviewRepository.save(new ReviewModel(
                company.getId(),
                testUser1.getId(),
                testUser1.getFirstName(),
                5,
                "Great!",
                "Developer",
                "Summer",
                2024
        ));

        // Delete the only review
        mockMvc.perform(delete("/api/companies/" + company.getId() + "/reviews/" + review.getId())
                .with(user(testUser1))
                .cookie(authCookie1))
                .andExpect(status().isOk());

        // Check average rating should be 0.0
        mockMvc.perform(get("/api/companies/" + company.getId())
                .with(user(testUser1))
                .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.avgRating").value(0.0));
    }

    // ==================== END-TO-END WORKFLOW TESTS ====================

    @Test
    public void testCompleteWorkflow_CreateCompanyAndReviews() throws Exception {
        // Step 1: User 1 creates a company
        CreateCompanyRequest companyRequest = new CreateCompanyRequest(
                "Niche Technology",
                "Winnipeg, MB",
                "https://www.niche.com"
        );

        MvcResult createCompanyResult = mockMvc.perform(post("/api/companies")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String companyId = objectMapper.readTree(createCompanyResult.getResponse().getContentAsString())
                .get("companyId").asText();

        // Step 2: User 1 adds a review
        CreateReviewRequest review1 = new CreateReviewRequest(
                5,
                "Amazing company with great culture!",
                "Software Developer",
                "Summer",
                2025
        );

        mockMvc.perform(post("/api/companies/" + companyId + "/reviews")
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review1)))
                .andExpect(status().isCreated());

        // Step 3: User 2 adds a review
        CreateReviewRequest review2 = new CreateReviewRequest(
                4,
                "Good experience, learned a lot",
                "QA Analyst",
                "Fall",
                2024
        );

        mockMvc.perform(post("/api/companies/" + companyId + "/reviews")
                        .with(user(testUser2))
                        .cookie(authCookie2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review2)))
                .andExpect(status().isCreated());

        // Step 4: Get company profile and verify both reviews are there
        mockMvc.perform(get("/api/companies/" + companyId)
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.avgRating").value(4.5))
                .andExpect(jsonPath("$.reviews", hasSize(2)));

        // Step 5: User 1 updates their review
        String review1Id = reviewRepository.findByUserIdAndCompanyId(testUser1.getId(), companyId)
                .orElseThrow().getId();

        UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                4,
                "Still great, but adjusting my rating",
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/companies/" + companyId + "/reviews/" + review1Id)
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Step 6: Verify average rating updated to 4.0
        mockMvc.perform(get("/api/companies/" + companyId)
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.avgRating").value(4.0));

        // Step 7: User 1 deletes their review
        mockMvc.perform(delete("/api/companies/" + companyId + "/reviews/" + review1Id)
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk());

        // Step 8: Verify only one review remains and average is 4.0
        mockMvc.perform(get("/api/companies/" + companyId)
                        .with(user(testUser1))
                        .cookie(authCookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.avgRating").value(4.0))
                .andExpect(jsonPath("$.reviews", hasSize(1)))
                .andExpect(jsonPath("$.reviews[0].authorName").value(testUser2.getFirstName() + " " + testUser2.getLastName()));
    }

    @Test
    public void testPaginationWorkflow() throws Exception {
        // Create a company
        CompanyModel company = companyRepository.save(
                new CompanyModel("Popular Company", "Location", "https://popular.com")
        );

        for (int i = 1; i <= 15; i++) {
            String fakeUserId = String.format("%024d", i);  // ✅ Unique userId for each

            ReviewModel review = new ReviewModel(
              company.getId(),
              fakeUserId,
              "User" + i,
              (i % 5) + 1,
              "Review " + i,
              "Job " + i,
              "Summer",
              2025
            );
            reviewRepository.save(review);
        }

        // Get first page (default size 10)
        mockMvc.perform(get("/api/companies/" + company.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews", hasSize(10)))
                .andExpect(jsonPath("$.reviewsPagination.currentPage").value(0))
                .andExpect(jsonPath("$.reviewsPagination.totalPages").value(2))
                .andExpect(jsonPath("$.reviewsPagination.totalItems").value(15))
                .andExpect(jsonPath("$.reviewsPagination.hasNext").value(true))
                .andExpect(jsonPath("$.reviewsPagination.hasPrevious").value(false));

        // Get second page
        mockMvc.perform(get("/api/companies/" + company.getId())
                        .with(user(testUser1))
                        .cookie(authCookie1)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews", hasSize(5)))
                .andExpect(jsonPath("$.reviewsPagination.currentPage").value(1))
                .andExpect(jsonPath("$.reviewsPagination.hasNext").value(false))
                .andExpect(jsonPath("$.reviewsPagination.hasPrevious").value(true));
    }
}
