package com.backend.coapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.CreateApplicationRequest;
import com.backend.coapp.dto.request.UpdateApplicationRequest;
import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.service.ApplicationService;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ApplicationControllerTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ApplicationService applicationService;

  private CreateApplicationRequest validCreateRequest;
  private UpdateApplicationRequest validUpdateRequest;
  private ApplicationResponse mockResponse;
  private Authentication mockAuth;

  @BeforeEach
  public void setUp() {
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
    // added to prevent delete test failure due to dirty test context when running all tests

    UserModel mockUser = mock(UserModel.class);
    when(mockUser.getId()).thenReturn("user123");
    when(mockUser.getFirstName()).thenReturn("John");
    when(mockUser.getLastName()).thenReturn("Doe");

    this.mockAuth =
        new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

    this.validCreateRequest =
        new CreateApplicationRequest(
            "comp456",
            "Software Engineer",
            ApplicationStatus.APPLIED,
            LocalDate.of(2024, 1, 1),
            "Great role",
            1,
            "https://linkedin.com",
            LocalDate.of(2024, 1, 1),
            "I think it's not that good.");

    this.validUpdateRequest = new UpdateApplicationRequest();
    this.validUpdateRequest.setCompanyId("comp456");
    this.validUpdateRequest.setJobTitle("Senior Engineer");
    this.validUpdateRequest.setJobDescription("Great role");
    this.validUpdateRequest.setSourceLink("https://linkedin.com");

    this.mockResponse = mock(ApplicationResponse.class);
    when(this.mockResponse.toMap())
        .thenReturn(Map.of("id", "app789", "jobTitle", "Software Engineer"));
  }

  // === CREATE APPLICATION TESTS ===

  @Test
  public void createApplication_whenValidRequest_expectCreated() throws Exception {
    when(this.applicationService.createApplication(
            anyString(),
            anyString(),
            anyString(),
            any(),
            any(),
            anyString(),
            anyInt(),
            anyString(),
            any(),
            anyString()))
        .thenReturn(this.mockResponse);

    mockMvc
        .perform(
            post("/api/application")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validCreateRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("app789"));

    verify(this.applicationService)
        .createApplication(
            eq("user123"),
            anyString(),
            anyString(),
            any(),
            any(),
            anyString(),
            anyInt(),
            anyString(),
            any(),
            anyString());
  }

  @Test
  public void createApplication_whenUnauthenticated_expectForbidden() throws Exception {
    mockMvc
        .perform(
            post("/api/application")
                .with(anonymous()) // Explicitly set to anonymous to prevent leakage
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validCreateRequest)))
        .andExpect(status().isForbidden());

    verifyNoInteractions(this.applicationService);
  }

  @Test
  public void createApplication_whenValidationFails_expectBadRequest() throws Exception {
    CreateApplicationRequest invalidRequest = new CreateApplicationRequest();

    mockMvc
        .perform(
            post("/api/application")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(this.applicationService);
  }

  @Test
  public void createApplication_whenServiceThrows_expectInternalServerError() throws Exception {
    when(this.applicationService.createApplication(
            anyString(),
            anyString(),
            anyString(),
            any(),
            any(),
            anyString(),
            anyInt(),
            anyString(),
            any(),
            anyString()))
        .thenThrow(new RuntimeException("Unexpected error"));

    mockMvc
        .perform(
            post("/api/application")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validCreateRequest)))
        .andExpect(status().isInternalServerError());
  }

  // === UPDATE APPLICATION TESTS ===

  @Test
  public void updateApplication_whenValidRequest_expectOk() throws Exception {
    when(this.mockResponse.toMap()).thenReturn(Map.of("jobTitle", "Senior Engineer"));
    when(this.applicationService.updateApplication(
            anyString(), anyString(), anyString(), anyString(), any(), any(), any()))
        .thenReturn(this.mockResponse);

    mockMvc
        .perform(
            put("/api/application/app789")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validUpdateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.jobTitle").value("Senior Engineer"));
  }

  @Test
  public void updateApplication_whenUnauthenticated_expectForbidden() throws Exception {
    mockMvc
        .perform(
            put("/api/application/app789")
                .with(anonymous()) // Explicitly set to anonymous
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validUpdateRequest)))
        .andExpect(status().isForbidden());

    verifyNoInteractions(this.applicationService);
  }

  @Test
  public void updateApplication_whenValidationFails_expectBadRequest() throws Exception {
    UpdateApplicationRequest invalidRequest = new UpdateApplicationRequest();

    mockMvc
        .perform(
            put("/api/application/app789")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(this.applicationService);
  }

  @Test
  public void updateApplication_whenServiceThrowsException_expectInternalServerError()
      throws Exception {
    when(this.applicationService.updateApplication(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()))
        .thenThrow(new RuntimeException("Update failed"));

    mockMvc
        .perform(
            put("/api/application/app789")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validUpdateRequest)))
        .andExpect(status().isInternalServerError());
  }

  // === DELETE APPLICATION TESTS ===

  @Test
  public void deleteApplication_whenValidRequest_expectOk() throws Exception {
    doNothing().when(this.applicationService).deleteApplication("app789", "user123");

    mockMvc
        .perform(delete("/api/application/app789").with(authentication(this.mockAuth)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Application successfully deleted."));

    verify(this.applicationService).deleteApplication("app789", "user123");
  }

  @Test
  public void deleteApplication_whenUnauthenticated_expectForbidden() throws Exception {
    mockMvc
        .perform(delete("/api/application/app789").with(anonymous())) // fix for the test pollution
        .andExpect(status().isForbidden());

    verifyNoInteractions(this.applicationService);
  }

  @Test
  public void deleteApplication_whenServiceThrows_expectInternalServerError() throws Exception {
    doThrow(new RuntimeException("Delete failed"))
        .when(this.applicationService)
        .deleteApplication(anyString(), anyString());

    mockMvc
        .perform(delete("/api/application/app789").with(authentication(this.mockAuth)))
        .andExpect(status().isInternalServerError());
  }
}
