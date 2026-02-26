package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.dto.request.CreateApplicationRequest;
import com.backend.coapp.dto.request.UpdateApplicationRequest;
import com.backend.coapp.dto.response.ApplicationResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.service.ApplicationService;
import com.backend.coapp.service.JwtService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ApplicationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private ApplicationService applicationService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private Authentication authentication;
  @Autowired private ApplicationController applicationController;

  private ApplicationResponse mockResponse;
  private CreateApplicationRequest createRequest;
  private UpdateApplicationRequest updateRequest;

  @BeforeEach
  public void setUp() {
    UserModel mockUser = mock(UserModel.class);
    when(mockUser.getId()).thenReturn("user1");
    when(mockUser.getFirstName()).thenReturn("John");
    when(mockUser.getLastName()).thenReturn("Doe");

    // Match the constructor order: applicationId, companyId, jobTitle, status, applicationDeadline,
    // jobDescription, numPositions, sourceLink, dateApplied, notes
    this.mockResponse =
        new ApplicationResponse(
            "app789",
            "comp456",
            "Software Engineer",
            ApplicationStatus.APPLIED,
            LocalDate.of(2024, 1, 1),
            "Great role",
            1,
            "https://linkedin.com",
            LocalDate.of(2024, 1, 1),
            "I think it's not that good.");

    this.createRequest =
        CreateApplicationRequest.builder()
            .userId("user1") // Required by DTO validation in provided code
            .companyId("comp456")
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.of(2024, 1, 1))
            .jobDescription("Great role")
            .numPositions(1)
            .sourceLink("https://linkedin.com")
            .dateApplied(LocalDate.of(2024, 1, 1))
            .notes("I think it's not that good.")
            .build();

    this.updateRequest =
        UpdateApplicationRequest.builder()
            .companyId("comp456")
            .jobTitle("Senior Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.of(2024, 1, 1))
            .jobDescription("Great role")
            .numPositions(1)
            .sourceLink("https://linkedin.com")
            .dateApplied(LocalDate.of(2024, 1, 1))
            .notes("Updated notes.")
            .build();

    when(this.authentication.getPrincipal()).thenReturn(mockUser);
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertEquals(this.applicationController.getApplicationService(), this.applicationService);
  }

  // test create application

  @Test
  public void createApplication_whenValid_expect201AndApplication() throws Exception {
    when(this.applicationService.createApplication(
            eq("user1"),
            eq("comp456"),
            eq("Software Engineer"),
            eq(ApplicationStatus.APPLIED),
            eq(LocalDate.of(2024, 1, 1)),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(LocalDate.of(2024, 1, 1)),
            eq("I think it's not that good.")))
        .thenReturn(this.mockResponse);

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest))
                .principal(this.authentication))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.applicationId").value("app789"))
        .andExpect(jsonPath("$.companyId").value("comp456"))
        .andExpect(jsonPath("$.jobTitle").value("Software Engineer"))
        .andExpect(jsonPath("$.status").value("APPLIED"))
        .andExpect(jsonPath("$.notes").value("I think it's not that good."));

    verify(this.applicationService, times(1))
        .createApplication(
            eq("user1"),
            eq("comp456"),
            eq("Software Engineer"),
            eq(ApplicationStatus.APPLIED),
            eq(LocalDate.of(2024, 1, 1)),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(LocalDate.of(2024, 1, 1)),
            eq("I think it's not that good."));
  }

  @Test
  public void createApplication_whenCompanyNotFound_expect404() throws Exception {
    when(this.applicationService.createApplication(
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString()))
        .thenThrow(new CompanyNotFoundException());

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest))
                .principal(this.authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("COMPANY_NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void createApplication_whenApplicationAlreadyExists_expect409() throws Exception {
    when(this.applicationService.createApplication(
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString()))
        .thenThrow(new DuplicateApplicationException("job", "comp"));

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest))
                .principal(this.authentication))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("DUPLICATE_APPLICATION"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void createApplication_whenMissingRequiredFields_expect400() throws Exception {
    CreateApplicationRequest invalidRequest = new CreateApplicationRequest();

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());

    verify(this.applicationService, never())
        .createApplication(
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString());
  }

  @Test
  public void createApplication_whenServiceFails_expect500() throws Exception {
    when(this.applicationService.createApplication(
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString()))
        .thenThrow(new ApplicationServiceFailException("Database error"));

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest))
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());
  }

  // test update application

  @Test
  public void updateApplication_whenValid_expect200AndUpdatedApplication() throws Exception {
    ApplicationResponse updatedResponse =
        new ApplicationResponse(
            "app789",
            "comp456",
            "Senior Engineer",
            ApplicationStatus.APPLIED,
            LocalDate.of(2024, 1, 1),
            "Great role",
            1,
            "https://linkedin.com",
            LocalDate.of(2024, 1, 1),
            "Updated notes.");

    // Match Service signature: userId, applicationId, newCompanyId, newJobTitle, newStatus,
    // newDeadline...
    when(this.applicationService.updateApplication(
            eq("user1"),
            eq("app789"),
            eq("comp456"),
            eq("Senior Engineer"),
            eq(ApplicationStatus.APPLIED),
            eq(LocalDate.of(2024, 1, 1)),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(LocalDate.of(2024, 1, 1)),
            eq("Updated notes.")))
        .thenReturn(updatedResponse);

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.updateRequest))
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applicationId").value("app789"))
        .andExpect(jsonPath("$.jobTitle").value("Senior Engineer"))
        .andExpect(jsonPath("$.notes").value("Updated notes."));

    verify(this.applicationService, times(1))
        .updateApplication(
            eq("user1"),
            eq("app789"),
            eq("comp456"),
            eq("Senior Engineer"),
            eq(ApplicationStatus.APPLIED),
            eq(LocalDate.of(2024, 1, 1)),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(LocalDate.of(2024, 1, 1)),
            eq("Updated notes."));
  }

  @Test
  public void updateApplication_whenApplicationNotFound_expect404() throws Exception {
    when(this.applicationService.updateApplication(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString()))
        .thenThrow(new ApplicationNotFoundException());

    mockMvc
        .perform(
            put("/api/application/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.updateRequest))
                .principal(this.authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("APPLICATION_NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void updateApplication_whenNotOwned_expect403() throws Exception {
    when(this.applicationService.updateApplication(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString()))
        .thenThrow(new UnauthorizedApplicationAccessException("update"));

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.updateRequest))
                .principal(this.authentication))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("UNAUTHORIZED_APPLICATION_ACCESS"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void updateApplication_whenNoFieldsProvided_expect400() throws Exception {
    UpdateApplicationRequest emptyRequest = new UpdateApplicationRequest();

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(emptyRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());

    verify(this.applicationService, never())
        .updateApplication(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString());
  }

  @Test
  public void updateApplication_whenServiceFails_expect500() throws Exception {
    when(this.applicationService.updateApplication(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString()))
        .thenThrow(new ApplicationServiceFailException("Database error"));

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.updateRequest))
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());
  }

  // test delete application

  @Test
  public void deleteApplication_whenValid_expect200WithSuccessMessage() throws Exception {
    doNothing().when(this.applicationService).deleteApplication(eq("app789"), eq("user1"));

    mockMvc
        .perform(
            delete("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Application successfully deleted."));

    verify(this.applicationService, times(1)).deleteApplication(eq("app789"), eq("user1"));
  }

  @Test
  public void deleteApplication_whenApplicationNotFound_expect404() throws Exception {
    doThrow(new ApplicationNotFoundException())
        .when(this.applicationService)
        .deleteApplication(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/application/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("APPLICATION_NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void deleteApplication_whenNotOwned_expect403() throws Exception {
    doThrow(new UnauthorizedApplicationAccessException("delete"))
        .when(this.applicationService)
        .deleteApplication(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("UNAUTHORIZED_APPLICATION_ACCESS"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void deleteApplication_whenServiceFails_expect500() throws Exception {
    doThrow(new ApplicationServiceFailException("Database error"))
        .when(this.applicationService)
        .deleteApplication(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void createApplication_withoutNotes_expect201() throws Exception {
    CreateApplicationRequest requestWithoutNotes =
        CreateApplicationRequest.builder()
            .userId("user1")
            .companyId("comp456")
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(LocalDate.of(2024, 1, 1))
            .jobDescription("Great role")
            .numPositions(1)
            .sourceLink("https://linkedin.com")
            .dateApplied(LocalDate.of(2024, 1, 1))
            .build();

    ApplicationResponse responseWithoutNotes =
        new ApplicationResponse(
            "app789",
            "comp456",
            "Software Engineer",
            ApplicationStatus.APPLIED,
            LocalDate.of(2024, 1, 1),
            "Great role",
            1,
            "https://linkedin.com",
            LocalDate.of(2024, 1, 1),
            null);

    when(this.applicationService.createApplication(
            eq("user1"),
            eq("comp456"),
            eq("Software Engineer"),
            eq(ApplicationStatus.APPLIED),
            eq(LocalDate.of(2024, 1, 1)),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(LocalDate.of(2024, 1, 1)),
            isNull()))
        .thenReturn(responseWithoutNotes);

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(requestWithoutNotes))
                .principal(this.authentication))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.applicationId").value("app789"))
        .andExpect(jsonPath("$.jobTitle").value("Software Engineer"));

    verify(this.applicationService, times(1))
        .createApplication(
            eq("user1"),
            eq("comp456"),
            eq("Software Engineer"),
            eq(ApplicationStatus.APPLIED),
            eq(LocalDate.of(2024, 1, 1)),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(LocalDate.of(2024, 1, 1)),
            isNull());
  }

  @Test
  public void updateApplication_withOnlyJobTitle_expect200() throws Exception {
    UpdateApplicationRequest partialUpdate =
        UpdateApplicationRequest.builder().jobTitle("Data Scientist").build();

    ApplicationResponse updatedResponse =
        new ApplicationResponse(
            "app789",
            "comp456",
            "Data Scientist",
            ApplicationStatus.APPLIED,
            LocalDate.of(2024, 1, 1),
            "Great role",
            1,
            "https://linkedin.com",
            LocalDate.of(2024, 1, 1),
            "I think it's not that good.");

    // Matching Service signature order
    when(this.applicationService.updateApplication(
            eq("user1"),
            eq("app789"),
            isNull(), // companyId
            eq("Data Scientist"), // jobTitle
            isNull(), // status
            isNull(), // deadline
            isNull(), // description
            isNull(), // positions
            isNull(), // link
            isNull(), // dateApplied
            isNull())) // notes
        .thenReturn(updatedResponse);

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(partialUpdate))
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.jobTitle").value("Data Scientist"));
  }
}
