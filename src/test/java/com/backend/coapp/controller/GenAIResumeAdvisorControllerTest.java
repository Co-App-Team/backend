package com.backend.coapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.dto.request.GenAIResumeAdvisorRequest;
import com.backend.coapp.exception.application.ApplicationNotFoundException;
import com.backend.coapp.exception.application.ApplicationNotOwnedException;
import com.backend.coapp.exception.genai.*;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.GenAIErrorCode;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import com.backend.coapp.model.enumeration.UserErrorCode;
import com.backend.coapp.service.GenAIResumeAdvisorService;
import com.backend.coapp.service.GenAIUsageManagementService;
import com.backend.coapp.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/** Parts of the unit test are written with help of Claude (Sonnet 4.6) */
@WebMvcTest(GenAIResumeAdvisorController.class)
@AutoConfigureMockMvc(addFilters = false)
class GenAIResumeAdvisorControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private GenAIResumeAdvisorService genAIResumeAdvisorService;
  @MockitoBean private GenAIUsageManagementService genAIUsageManagementService;
  @MockitoBean private JwtService jwtService;

  @MockitoBean private Authentication authentication;
  private UserModel mockUser;

  private GenAIResumeAdvisorRequest validRequest;
  private GenAIResumeAdvisorRequest validRequestNoApplication;

  @BeforeEach
  void setUp() {
    mockUser = mock(UserModel.class);
    when(mockUser.getId()).thenReturn("testUserID");
    when(mockUser.getFirstName()).thenReturn("Foo");
    when(mockUser.getLastName()).thenReturn("User");

    validRequest = new GenAIResumeAdvisorRequest("Help me improve my resume", "someApplicationId");
    validRequestNoApplication = new GenAIResumeAdvisorRequest("Help me improve my resume", null);

    when(this.authentication.getPrincipal()).thenReturn(mockUser);
  }

  @Test
  void resumeAdvisor_whenSuccess_expectOkAndResponse() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenReturn("Here is your advice");

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.response").value("Here is your advice"));

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenNoApplicationId_expectOkAndResponse() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), isNull(), anyString()))
        .thenReturn("Here is your advice");

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestNoApplication))
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.response").value("Here is your advice"));

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), null, validRequestNoApplication.getUserPrompt());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void resumeAdvisor_whenPromptIsInvalid_expect400(String prompt) throws Exception {
    GenAIResumeAdvisorRequest invalidRequest =
        new GenAIResumeAdvisorRequest(prompt, "someApplicationId");

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(genAIResumeAdvisorService);
  }

  @Test
  void resumeAdvisor_whenApplicationIdIsBlank_expect400() throws Exception {
    GenAIResumeAdvisorRequest invalidRequest = new GenAIResumeAdvisorRequest("Help me", "   ");

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(genAIResumeAdvisorService);
  }

  @Test
  void resumeAdvisor_whenPromptOverLimit_expect400() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new OverCharacterLimitException("Prompt too long"));

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenApplicationNotFound_expect404() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new ApplicationNotFoundException());

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isNotFound());

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenApplicationNotOwned_expect403() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new ApplicationNotOwnedException());

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isForbidden());

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenUserNotExist_expect400() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new UserNotFoundException());

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(UserErrorCode.USER_NOT_EXIST.name()));

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenQuotaExceeded_expect429() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new GenAIQuotaExceededException());

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isTooManyRequests());

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenConcurrentRequest_expect409() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new ConcurrencyException("Another request in progress"));

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isConflict());

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenServiceFails_expect500() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new GenAIUsageManagementServiceException("Internal error"));

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenGenAIServiceFails_expect500() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new GenAIServiceException("Internal error"));

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void resumeAdvisor_whenReachLimit_expect503() throws Exception {
    when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
        .thenThrow(new GenAIOutOfServiceException("foo exception"));

    mockMvc
        .perform(
            post("/api/resume-ai-advisor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .principal(this.authentication))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value(GenAIErrorCode.SERVICE_UNAVAILABLE.name()));

    verify(genAIResumeAdvisorService, times(1))
        .getAdvice(mockUser.getId(), validRequest.getApplicationId(), validRequest.getUserPrompt());
  }

  @Test
  void getRemainingQuota_whenSuccess_expectOkAndRemainingQuota() throws Exception {
    when(genAIUsageManagementService.getNumberOfRequestLeft(anyString())).thenReturn(7);

    mockMvc
        .perform(
            get("/api/resume-ai-advisor/remaining-quota")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.remainingQuota").value(7));

    verify(genAIUsageManagementService, times(1)).getNumberOfRequestLeft(mockUser.getId());
  }

  @Test
  void getRemainingQuota_whenQuotaIsZero_expectOkAndZero() throws Exception {
    when(genAIUsageManagementService.getNumberOfRequestLeft(anyString())).thenReturn(0);

    mockMvc
        .perform(
            get("/api/resume-ai-advisor/remaining-quota")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.remainingQuota").value(0));

    verify(genAIUsageManagementService, times(1)).getNumberOfRequestLeft(mockUser.getId());
  }

  @Test
  void getRemainingQuota_whenServiceFails_expect500() throws Exception {
    when(genAIUsageManagementService.getNumberOfRequestLeft(anyString()))
        .thenThrow(new GenAIUsageManagementServiceException("DB failed"));

    mockMvc
        .perform(
            get("/api/resume-ai-advisor/remaining-quota")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));

    verify(genAIUsageManagementService, times(1)).getNumberOfRequestLeft(mockUser.getId());
  }
}
