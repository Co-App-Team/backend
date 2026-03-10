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

  private static final String USER_ID = "user1";
  private static final String APP_ID = "app789";
  private static final String COMPANY_ID = "comp456";
  private static final String JOB_TITLE = "Software Engineer";
  private static final String SENIOR_TITLE = "Senior Engineer";
  private static final String DESCRIPTION = "Great role";
  private static final String NOTES = "I think it's not that good.";
  private static final String LINK = "https://linkedin.com";
  private static final LocalDate DATE = LocalDate.of(2027, 1, 1);

  private ApplicationResponse mockResponse;
  private CreateApplicationRequest createRequest;
  private UpdateApplicationRequest updateRequest;

  @BeforeEach
  public void setUp() {
    UserModel mockUser = mock(UserModel.class);
    when(mockUser.getId()).thenReturn(USER_ID);
    when(authentication.getPrincipal()).thenReturn(mockUser);

    this.mockResponse = getValidResponseBuilder().build();
    this.createRequest = getValidCreateRequestBuilder().build();
    this.updateRequest = getValidUpdateRequestBuilder().build();
  }

  // Helper Methods for Builders

  private ApplicationResponse.ApplicationResponseBuilder getValidResponseBuilder() {
    return ApplicationResponse.builder()
        .applicationId(APP_ID)
        .companyId(COMPANY_ID)
        .jobTitle(JOB_TITLE)
        .status(ApplicationStatus.APPLIED)
        .applicationDeadline(DATE)
        .jobDescription(DESCRIPTION)
        .numPositions(1)
        .sourceLink(LINK)
        .dateApplied(DATE)
        .notes(NOTES)
        .interviewDate(DATE);
  }

  private CreateApplicationRequest.CreateApplicationRequestBuilder getValidCreateRequestBuilder() {
    return CreateApplicationRequest.builder()
        .companyId(COMPANY_ID)
        .jobTitle(JOB_TITLE)
        .status(ApplicationStatus.APPLIED)
        .applicationDeadline(DATE)
        .jobDescription(DESCRIPTION)
        .numPositions(1)
        .sourceLink(LINK)
        .dateApplied(DATE)
        .notes(NOTES)
        .interviewDate(DATE);
  }

  private UpdateApplicationRequest.UpdateApplicationRequestBuilder getValidUpdateRequestBuilder() {
    return UpdateApplicationRequest.builder()
        .companyId(COMPANY_ID)
        .jobTitle(SENIOR_TITLE)
        .status(ApplicationStatus.APPLIED)
        .applicationDeadline(DATE)
        .jobDescription(DESCRIPTION)
        .numPositions(1)
        .sourceLink(LINK)
        .dateApplied(DATE)
        .notes("Updated notes.")
        .interviewDate(DATE);
  }

  // Helper Methods for Mocking

  private void mockCreateApplicationThrow(RuntimeException exception) {
    when(applicationService.createApplication(
            anyString(),
            anyString(),
            anyString(),
            any(ApplicationStatus.class),
            any(LocalDate.class),
            anyString(),
            any(Integer.class),
            anyString(),
            any(LocalDate.class),
            anyString(),
            any(LocalDate.class)))
        .thenThrow(exception);
  }

  private void mockUpdateApplicationThrow(RuntimeException exception) {
    when(applicationService.updateApplication(
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
            anyString(),
            any(LocalDate.class)))
        .thenThrow(exception);
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertEquals(applicationController.getApplicationService(), applicationService);
  }

  // test create application

  @Test
  public void createApplication_whenValid_expect201AndApplication() throws Exception {
    when(applicationService.createApplication(
            eq(USER_ID),
            eq(COMPANY_ID),
            eq(JOB_TITLE),
            eq(ApplicationStatus.APPLIED),
            eq(DATE),
            eq(DESCRIPTION),
            eq(1),
            eq(LINK),
            eq(DATE),
            eq(NOTES),
            eq(DATE)))
        .thenReturn(mockResponse);

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .principal(authentication))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.applicationId").value(APP_ID))
        .andExpect(jsonPath("$.companyId").value(COMPANY_ID))
        .andExpect(jsonPath("$.jobTitle").value(JOB_TITLE));

    verify(applicationService, times(1))
        .createApplication(
            eq(USER_ID),
            eq(COMPANY_ID),
            eq(JOB_TITLE),
            eq(ApplicationStatus.APPLIED),
            eq(DATE),
            eq(DESCRIPTION),
            eq(1),
            eq(LINK),
            eq(DATE),
            eq(NOTES),
            eq(DATE));
  }

  @Test
  public void createApplication_whenCompanyNotFound_expect404() throws Exception {
    mockCreateApplicationThrow(new CompanyNotFoundException());

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .principal(authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("COMPANY_NOT_FOUND"));
  }

  @Test
  public void createApplication_whenApplicationAlreadyExists_expect409() throws Exception {
    mockCreateApplicationThrow(new DuplicateApplicationException("job", "comp"));

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .principal(authentication))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("DUPLICATE_APPLICATION"));
  }

  @Test
  public void createApplication_whenMissingRequiredFields_expect400() throws Exception {
    CreateApplicationRequest invalidRequest = new CreateApplicationRequest();

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .principal(authentication))
        .andExpect(status().isBadRequest());

    verify(applicationService, never())
        .createApplication(
            anyString(),
            anyString(),
            anyString(),
            any(),
            any(),
            anyString(),
            any(),
            anyString(),
            any(),
            anyString(),
            any());
  }

  @Test
  public void createApplication_whenServiceFails_expect500() throws Exception {
    mockCreateApplicationThrow(new ApplicationServiceFailException("Database error"));

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .principal(authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
  }

  @Test
  public void createApplication_withoutNotes_expect201() throws Exception {
    CreateApplicationRequest requestWithoutNotes =
        getValidCreateRequestBuilder().notes(null).build();
    ApplicationResponse responseWithoutNotes = getValidResponseBuilder().notes(null).build();

    when(applicationService.createApplication(
            eq(USER_ID),
            eq(COMPANY_ID),
            eq(JOB_TITLE),
            eq(ApplicationStatus.APPLIED),
            eq(DATE),
            eq(DESCRIPTION),
            eq(1),
            eq(LINK),
            eq(DATE),
            isNull(),
            eq(DATE)))
        .thenReturn(responseWithoutNotes);

    mockMvc
        .perform(
            post("/api/application")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithoutNotes))
                .principal(authentication))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.applicationId").value(APP_ID));
  }

  // test update application

  @Test
  public void updateApplication_whenValid_expect200AndUpdatedApplication() throws Exception {
    ApplicationResponse updatedResponse =
        getValidResponseBuilder().jobTitle(SENIOR_TITLE).notes("Updated notes.").build();

    when(applicationService.updateApplication(
            eq(USER_ID),
            eq(APP_ID),
            eq(COMPANY_ID),
            eq(SENIOR_TITLE),
            eq(ApplicationStatus.APPLIED),
            eq(DATE),
            eq(DESCRIPTION),
            eq(1),
            eq(LINK),
            eq(DATE),
            eq("Updated notes."),
            eq(DATE)))
        .thenReturn(updatedResponse);

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.jobTitle").value(SENIOR_TITLE))
        .andExpect(jsonPath("$.notes").value("Updated notes."));
  }

  @Test
  public void updateApplication_whenApplicationNotFound_expect404() throws Exception {
    mockUpdateApplicationThrow(new ApplicationNotFoundException());

    mockMvc
        .perform(
            put("/api/application/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .principal(authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("APPLICATION_NOT_FOUND"));
  }

  @Test
  public void updateApplication_whenNotOwned_expect403() throws Exception {
    mockUpdateApplicationThrow(new UnauthorizedApplicationAccessException("update"));

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .principal(authentication))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("UNAUTHORIZED_APPLICATION_ACCESS"));
  }

  @Test
  public void updateApplication_whenNoFieldsProvided_expect400() throws Exception {
    UpdateApplicationRequest emptyRequest = new UpdateApplicationRequest();

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest))
                .principal(authentication))
        .andExpect(status().isBadRequest());

    verify(applicationService, never())
        .updateApplication(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(),
            any(),
            anyString(),
            any(),
            anyString(),
            any(),
            anyString(),
            any());
  }

  @Test
  public void updateApplication_whenServiceFails_expect500() throws Exception {
    mockUpdateApplicationThrow(new ApplicationServiceFailException("Database error"));

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .principal(authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
  }

  @Test
  public void updateApplication_whenNoChanges_expect400() throws Exception {
    mockUpdateApplicationThrow(new NoChangesDetectedException("No fields were changed."));

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .principal(authentication))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("NO_CHANGE_DETECTED_TO_UPDATE"))
        .andExpect(jsonPath("$.message").value("No fields were changed."));
  }

  @Test
  public void updateApplication_withOnlyJobTitle_expect200() throws Exception {
    UpdateApplicationRequest partialUpdate =
        UpdateApplicationRequest.builder().jobTitle("Data Scientist").build();
    ApplicationResponse updatedResponse =
        getValidResponseBuilder().jobTitle("Data Scientist").build();

    // Matching Service signature order for partial update (mostly nulls)
    when(applicationService.updateApplication(
            eq(USER_ID),
            eq(APP_ID),
            isNull(),
            eq("Data Scientist"),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull()))
        .thenReturn(updatedResponse);

    mockMvc
        .perform(
            put("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate))
                .principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.jobTitle").value("Data Scientist"));
  }

  // test delete application

  @Test
  public void deleteApplication_whenValid_expect200WithSuccessMessage() throws Exception {
    doNothing().when(applicationService).deleteApplication(eq(APP_ID), eq(USER_ID));

    mockMvc
        .perform(
            delete("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Application successfully deleted."));
  }

  @Test
  public void deleteApplication_whenApplicationNotFound_expect404() throws Exception {
    doThrow(new ApplicationNotFoundException())
        .when(applicationService)
        .deleteApplication(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/application/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("APPLICATION_NOT_FOUND"));
  }

  @Test
  public void deleteApplication_whenNotOwned_expect403() throws Exception {
    doThrow(new UnauthorizedApplicationAccessException("delete"))
        .when(applicationService)
        .deleteApplication(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("UNAUTHORIZED_APPLICATION_ACCESS"));
  }

  @Test
  public void deleteApplication_whenServiceFails_expect500() throws Exception {
    doThrow(new ApplicationServiceFailException("Database error"))
        .when(applicationService)
        .deleteApplication(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/application/app789")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
  }

  // get application tests

  @Test
  public void getApplications_whenValid_expect200AndApplicationList() throws Exception {
    when(applicationService.getApplications(eq(USER_ID))).thenReturn(List.of(mockResponse));

    mockMvc
        .perform(get("/api/application").principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].applicationId").value(APP_ID))
        .andExpect(jsonPath("$[0].jobTitle").value(JOB_TITLE));
  }

  @Test
  public void getApplications_whenEmpty_expect200AndEmptyList() throws Exception {
    when(applicationService.getApplications(eq(USER_ID))).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/application").principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  public void getApplications_whenServiceFails_expect500() throws Exception {
    when(applicationService.getApplications(anyString()))
        .thenThrow(new ApplicationServiceFailException("Database error"));

    mockMvc
        .perform(get("/api/application").principal(authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
  }
}
