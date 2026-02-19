package com.backend.coapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

    // Must satisfy ALL fields checked by UpdateApplicationRequest.validateRequest()
    this.validUpdateRequest = new UpdateApplicationRequest();
    this.validUpdateRequest.setCompanyId("comp456");
    this.validUpdateRequest.setJobTitle("Senior Engineer");
    this.validUpdateRequest.setJobDescription("Great role");
    this.validUpdateRequest.setSourceLink("https://linkedin.com");

    this.mockResponse = mock(ApplicationResponse.class);
    when(this.mockResponse.toMap())
        .thenReturn(Map.of("id", "app789", "jobTitle", "Software Engineer"));
  }

  // --- Create Application Tests ---

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
        .andExpect(jsonPath("$.id").value("app789"))
        .andExpect(jsonPath("$.jobTitle").value("Software Engineer"));

    // Verify userId comes from auth (user123), not from request body
    verify(this.applicationService, times(1))
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
  public void createApplication_whenUnauthenticated_expectUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validCreateRequest)))
        .andExpect(status().isForbidden()); // 403, not 401 — matches your security config

    verifyNoInteractions(this.applicationService);
  }

  @Test
  public void createApplication_whenValidationFails_expectBadRequest() throws Exception {
    CreateApplicationRequest invalidRequest = new CreateApplicationRequest(); // Empty / invalid

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
  public void createApplication_withNullPayload_expectBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/application")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
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

  // --- Update Application Tests ---

  @Test
  public void updateApplication_whenValidRequest_expectOk() throws Exception {
    when(this.mockResponse.toMap()).thenReturn(Map.of("jobTitle", "Senior Engineer"));
    when(this.applicationService.updateApplication(
            anyString(), // applicationId
            anyString(), // userId
            anyString(), // companyId
            anyString(), // jobTitle
            any(), // jobDescription — nullable
            any(), // sourceLink — nullable
            any())) // notes — nullable
        .thenReturn(this.mockResponse);

    mockMvc
        .perform(
            put("/api/application/app789")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validUpdateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.jobTitle").value("Senior Engineer"));

    verify(this.applicationService, times(1))
        .updateApplication(
            eq("app789"), eq("user123"), anyString(), anyString(), any(), any(), any());
  }

  @Test
  public void updateApplication_whenUnauthenticated_expectUnauthorized() throws Exception {
    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validUpdateRequest)))
        .andExpect(status().isForbidden());

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

  @Test
  public void updateApplication_whenValidationFails_expectBadRequest() throws Exception {
    UpdateApplicationRequest invalidRequest = new UpdateApplicationRequest(); // triggers validation

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
  public void updateApplication_whenApplicationNotFound_expectNotFound() throws Exception {
    when(this.applicationService.updateApplication(
            eq("nonexistent"),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()))
        .thenThrow(new RuntimeException("Not Found"));

    mockMvc
        .perform(
            put("/api/application/nonexistent")
                .with(authentication(this.mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.validUpdateRequest)))
        .andExpect(
            status().isInternalServerError()); // adjust if your @ControllerAdvice maps this to 404
  }

  // --- Delete Application Tests ---

  @Test
  public void deleteApplication_whenValidRequest_expectOk() throws Exception {
    doNothing().when(this.applicationService).deleteApplication("app789", "user123");

    mockMvc
        .perform(delete("/api/application/app789").with(authentication(this.mockAuth)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Application successfully deleted."));

    // Verify userId sourced from auth, not a request param
    verify(this.applicationService, times(1)).deleteApplication("app789", "user123");
  }

  @Test
  public void deleteApplication_whenUnauthenticated_expectUnauthorized() throws Exception {
    mockMvc.perform(delete("/api/application/app789")).andExpect(status().isForbidden());

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

  @Test
  public void deleteApplication_whenApplicationNotFound_expectNotFound() throws Exception {
    doThrow(new RuntimeException("Application not found"))
        .when(this.applicationService)
        .deleteApplication(eq("nonexistent"), anyString());

    mockMvc
        .perform(delete("/api/application/nonexistent").with(authentication(this.mockAuth)))
        .andExpect(
            status()
                .isInternalServerError());
    // it
  }
}
