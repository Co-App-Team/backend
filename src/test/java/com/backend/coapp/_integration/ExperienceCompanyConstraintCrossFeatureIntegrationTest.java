package com.backend.coapp._integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.LoginRequest;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
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
class ExperienceCompanyConstraintCrossFeatureIntegrationTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private CompanyRepository companyRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PasswordEncoder passwordEncoder;

  private String testUserEmail;

  @BeforeEach
  void setUp() {
    companyRepository.deleteAll();
    userRepository.deleteAll();

    UserModel testUser =
        new UserModel(
            "user_exp",
            "exp@example.com",
            passwordEncoder.encode("pass"),
            "Exp",
            "User",
            true,
            1234);
    userRepository.save(testUser);
    testUserEmail = testUser.getEmail();
  }

  @Test
  void experienceFlow_whenUserAddsExperienceForNonExistentCompany_expectFailureThenSuccess()
      throws Exception {
    // Login
    LoginRequest loginRequest = new LoginRequest(testUserEmail, "pass");
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

    Cookie authCookie = loginResult.getResponse().getCookie("Authorization");
    assertThat(authCookie).isNotNull();

    // Step 1: Attempt to add experience with random companyId (Feature 6 + Feature 4 check)
    Map<String, String> expReq = new HashMap<>();
    expReq.put("companyId", "non-existent-id");
    expReq.put("roleTitle", "Dev");
    expReq.put("startDate", "2023-01-01");
    expReq.put("roleDescription", "Test Role");

    mockMvc
        .perform(
            post("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expReq))
                .cookie(authCookie))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("COMPANY_NOT_FOUND"));

    // Step 2: Create Company (Feature 4)
    Map<String, String> companyReq = new HashMap<>();
    companyReq.put("companyName", "Valid Corp");
    companyReq.put("location", "Remote");
    companyReq.put("website", "https://valid.com");

    MvcResult companyResult =
        mockMvc
            .perform(
                post("/api/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(companyReq))
                    .cookie(authCookie))
            .andExpect(status().isCreated())
            .andReturn();

    String validCompanyId =
        JsonPath.read(companyResult.getResponse().getContentAsString(), "$.companyId");

    // Step 3: Retry adding experience with valid companyId (Feature 6)
    expReq.put("companyId", validCompanyId);

    mockMvc
        .perform(
            post("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expReq))
                .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.experienceId").exists());
  }
}
