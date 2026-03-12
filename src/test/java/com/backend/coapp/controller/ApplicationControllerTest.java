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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
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

  private final LocalDate DATE = LocalDate.of(2800, 1, 1);

  @BeforeEach
  public void setUp() {
    UserModel mockUser = mock(UserModel.class);
    when(mockUser.getId()).thenReturn("user1");
    when(mockUser.getFirstName()).thenReturn("John");
    when(mockUser.getLastName()).thenReturn("Doe");

    this.mockResponse =
        new ApplicationResponse(
            "app789",
            "comp456",
            "Software Engineer",
            ApplicationStatus.APPLIED,
            DATE,
            "Great role",
            1,
            "https://linkedin.com",
            DATE,
            "I think it's not that good.",
            DATE);

    this.createRequest =
        CreateApplicationRequest.builder()
            .companyId("comp456")
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(DATE)
            .jobDescription("Great role")
            .numPositions(1)
            .sourceLink("https://linkedin.com")
            .dateApplied(DATE)
            .notes("I think it's not that good.")
            .interviewDate(DATE)
            .build();

    this.updateRequest =
        UpdateApplicationRequest.builder()
            .companyId("comp456")
            .jobTitle("Senior Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(DATE)
            .jobDescription("Great role")
            .numPositions(1)
            .sourceLink("https://linkedin.com")
            .dateApplied(DATE)
            .notes("Updated notes.")
            .interviewDate(DATE)
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
            eq(DATE),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(DATE),
            eq("I think it's not that good."),
            eq(DATE)))
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
            eq(DATE),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(DATE),
            eq("I think it's not that good."),
            eq(DATE));
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
            anyInt(),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class)))
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
            anyInt(),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class)))
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
            anyInt(),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class));
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
            anyInt(),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class)))
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
            DATE,
            "Great role",
            1,
            "https://linkedin.com",
            DATE,
            "Updated notes.",
            DATE);

    when(this.applicationService.updateApplication(
            eq("user1"),
            eq("app789"),
            eq("comp456"),
            eq("Senior Engineer"),
            eq(ApplicationStatus.APPLIED),
            eq(DATE),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(DATE),
            eq("Updated notes."),
            eq(DATE)))
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
            eq(DATE),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(DATE),
            eq("Updated notes."),
            eq(DATE));
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
            anyInt(),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class)))
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
            anyInt(),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class)))
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
            anyInt(),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class));
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
            anyInt(),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class)))
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

  @Test
  public void updateApplication_whenNoChanges_expect400() throws Exception {
    when(this.applicationService.updateApplication(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(),
            any(),
            any(),
            anyInt(),
            any(),
            any(),
            any(),
            any()))
        .thenThrow(new NoChangesDetectedException("No fields were changed."));

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.updateRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("NO_CHANGE_DETECTED_TO_UPDATE"))
        .andExpect(jsonPath("$.message").value("No fields were changed."));

    verify(this.applicationService, times(1))
        .updateApplication(
            anyString(),
            eq("app789"),
            anyString(),
            anyString(),
            any(),
            any(),
            any(),
            anyInt(),
            any(),
            any(),
            any(),
            any());
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
            .companyId("comp456")
            .jobTitle("Software Engineer")
            .status(ApplicationStatus.APPLIED)
            .applicationDeadline(DATE)
            .jobDescription("Great role")
            .numPositions(1)
            .sourceLink("https://linkedin.com")
            .dateApplied(DATE)
            .build();

    ApplicationResponse responseWithoutNotes =
        new ApplicationResponse(
            "app789",
            "comp456",
            "Software Engineer",
            ApplicationStatus.APPLIED,
            DATE,
            "Great role",
            1,
            "https://linkedin.com",
            DATE,
            null,
            null);

    when(this.applicationService.createApplication(
            eq("user1"),
            eq("comp456"),
            eq("Software Engineer"),
            eq(ApplicationStatus.APPLIED),
            eq(DATE),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(DATE),
            isNull(),
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
            eq(DATE),
            eq("Great role"),
            eq(1),
            eq("https://linkedin.com"),
            eq(DATE),
            isNull(),
            isNull());
  }

  // test get applications (filtered)

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenNoParams_expect200WithApplicationsAndPagination()
      throws Exception {
    Map<String, Object> mockServiceResponse =
        Map.of(
            "applications",
            List.of(this.mockResponse.toMap()),
            "pagination",
            Map.of(
                "currentPage",
                0,
                "totalPages",
                1,
                "totalItems",
                1L,
                "itemsPerPage",
                20,
                "hasNext",
                false,
                "hasPrevious",
                false));

    when(this.applicationService.getFilteredApplications(
            eq("user1"), isNull(), isNull(), anyString(), anyString(), eq(0), eq(20)))
        .thenReturn(mockServiceResponse);

    mockMvc
        .perform(get("/api/application"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applications[0].applicationId").value("app789"))
        .andExpect(jsonPath("$.applications[0].jobTitle").value("Software Engineer"))
        .andExpect(jsonPath("$.pagination.currentPage").value(0))
        .andExpect(jsonPath("$.pagination.totalItems").value(1));

    verify(this.applicationService, times(1))
        .getFilteredApplications(
            eq("user1"), isNull(), isNull(), anyString(), anyString(), eq(0), eq(20));
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenSearchParam_expectPassedToService() throws Exception {
    when(this.applicationService.getFilteredApplications(
            eq("user1"), eq("Google"), isNull(), anyString(), anyString(), anyInt(), anyInt()))
        .thenReturn(Map.of("applications", Collections.emptyList(), "pagination", Map.of()));

    mockMvc.perform(get("/api/application").param("search", "Google")).andExpect(status().isOk());

    verify(this.applicationService, times(1))
        .getFilteredApplications(
            eq("user1"), eq("Google"), isNull(), anyString(), anyString(), anyInt(), anyInt());
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenStatusParam_expectPassedToService() throws Exception {
    when(this.applicationService.getFilteredApplications(
            eq("user1"),
            isNull(),
            eq(List.of(ApplicationStatus.APPLIED)),
            anyString(),
            anyString(),
            anyInt(),
            anyInt()))
        .thenReturn(Map.of("applications", Collections.emptyList(), "pagination", Map.of()));

    mockMvc.perform(get("/api/application").param("status", "APPLIED")).andExpect(status().isOk());

    verify(this.applicationService, times(1))
        .getFilteredApplications(
            eq("user1"),
            isNull(),
            eq(List.of(ApplicationStatus.APPLIED)),
            anyString(),
            anyString(),
            anyInt(),
            anyInt());
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenMultipleStatuses_expectPassedToService() throws Exception {
    when(this.applicationService.getFilteredApplications(
            eq("user1"),
            isNull(),
            eq(List.of(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED)),
            anyString(),
            anyString(),
            anyInt(),
            anyInt()))
        .thenReturn(Map.of("applications", Collections.emptyList(), "pagination", Map.of()));

    mockMvc
        .perform(get("/api/application").param("status", "APPLIED,REJECTED"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenInvalidStatus_expect400() throws Exception {
    mockMvc
        .perform(get("/api/application").param("status", "INVALID_STATUS"))
        .andExpect(status().isBadRequest());

    verify(this.applicationService, never())
        .getFilteredApplications(
            anyString(), any(), any(), anyString(), anyString(), anyInt(), anyInt());
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenInvalidSortBy_expect400() throws Exception {
    mockMvc
        .perform(get("/api/application").param("sortBy", "invalidField"))
        .andExpect(status().isBadRequest());

    verify(this.applicationService, never())
        .getFilteredApplications(
            anyString(), any(), any(), anyString(), anyString(), anyInt(), anyInt());
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenInvalidSortOrder_expect400() throws Exception {
    mockMvc
        .perform(get("/api/application").param("sortOrder", "sideways"))
        .andExpect(status().isBadRequest());

    verify(this.applicationService, never())
        .getFilteredApplications(
            anyString(), any(), any(), anyString(), anyString(), anyInt(), anyInt());
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenSortParams_expectPassedToService() throws Exception {
    when(this.applicationService.getFilteredApplications(
            eq("user1"), isNull(), isNull(), eq("dateApplied"), eq("asc"), anyInt(), anyInt()))
        .thenReturn(Map.of("applications", Collections.emptyList(), "pagination", Map.of()));

    mockMvc
        .perform(get("/api/application").param("sortBy", "dateApplied").param("sortOrder", "asc"))
        .andExpect(status().isOk());

    verify(this.applicationService, times(1))
        .getFilteredApplications(
            eq("user1"), isNull(), isNull(), eq("dateApplied"), eq("asc"), anyInt(), anyInt());
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenPaginationParams_expectPassedToService() throws Exception {
    when(this.applicationService.getFilteredApplications(
            eq("user1"), isNull(), isNull(), anyString(), anyString(), eq(2), eq(10)))
        .thenReturn(Map.of("applications", Collections.emptyList(), "pagination", Map.of()));

    mockMvc
        .perform(get("/api/application").param("page", "2").param("size", "10"))
        .andExpect(status().isOk());

    verify(this.applicationService, times(1))
        .getFilteredApplications(
            eq("user1"), isNull(), isNull(), anyString(), anyString(), eq(2), eq(10));
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenServiceFails_expect500() throws Exception {
    when(this.applicationService.getFilteredApplications(
            anyString(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
        .thenThrow(new ApplicationServiceFailException("Database error"));

    mockMvc
        .perform(get("/api/application"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @WithMockUser(username = "user1")
  public void getApplications_whenEmpty_expect200WithEmptyList() throws Exception {
    when(this.applicationService.getFilteredApplications(
            eq("user1"), isNull(), isNull(), anyString(), anyString(), anyInt(), anyInt()))
        .thenReturn(
            Map.of(
                "applications",
                Collections.emptyList(),
                "pagination",
                Map.of(
                    "currentPage",
                    0,
                    "totalPages",
                    0,
                    "totalItems",
                    0L,
                    "itemsPerPage",
                    20,
                    "hasNext",
                    false,
                    "hasPrevious",
                    false)));

    mockMvc
        .perform(get("/api/application"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applications").isEmpty())
        .andExpect(jsonPath("$.pagination.totalItems").value(0));
  }
}
