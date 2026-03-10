package com.backend.coapp.controller;

import com.backend.coapp.dto.request.GenAIResumeAdvisorRequest;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import com.backend.coapp.model.enumeration.UserErrorCode;
import com.backend.coapp.service.GenAIResumeAdvisorService;
import com.backend.coapp.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenAIResumeAdvisorController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GenAIResumeAdvisorControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private GenAIResumeAdvisorService genAIResumeAdvisorService;
    @MockitoBean private JwtService jwtService;

    @Autowired private GenAIResumeAdvisorController genAIResumeAdvisorController;

    private GenAIResumeAdvisorRequest validRequest;
    private GenAIResumeAdvisorRequest validRequestNoApplication;

    @BeforeEach
    public void setUp() {
        validRequest = new GenAIResumeAdvisorRequest("Help me improve my resume", "someApplicationId");
        validRequestNoApplication = new GenAIResumeAdvisorRequest("Help me improve my resume", null);
    }
    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenSuccess_expectOkAndResponse() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
                .thenReturn("Here is your advice");

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Here is your advice"));

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", validRequest.getApplicationId(), validRequest.getUserPrompt());
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenNoApplicationId_expectOkAndResponse() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), isNull(), anyString()))
                .thenReturn("Here is your advice");

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestNoApplication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Here is your advice"));

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", null, validRequestNoApplication.getUserPrompt());
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenPromptIsNull_expect400() throws Exception {
        GenAIResumeAdvisorRequest invalidRequest = new GenAIResumeAdvisorRequest(null, "someApplicationId");

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(genAIResumeAdvisorService);
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenPromptIsBlank_expect400() throws Exception {
        GenAIResumeAdvisorRequest invalidRequest = new GenAIResumeAdvisorRequest("   ", "someApplicationId");

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(genAIResumeAdvisorService);
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenApplicationIdIsBlank_expect400() throws Exception {
        GenAIResumeAdvisorRequest invalidRequest = new GenAIResumeAdvisorRequest("Help me", "   ");

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(genAIResumeAdvisorService);
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenPromptOverLimit_expect400() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
                .thenThrow(new OverCharacterLimitException("Prompt too long"));

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", validRequest.getApplicationId(), validRequest.getUserPrompt());
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenApplicationNotFound_expect404() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
                .thenThrow(new ApplicationNotFoundException());

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", validRequest.getApplicationId(), validRequest.getUserPrompt());
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenApplicationNotOwned_expect403() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
                .thenThrow(new ApplicationNotOwnedException());

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", validRequest.getApplicationId(), validRequest.getUserPrompt());
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenUserNotExist_expect400() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
                .thenThrow(new UserNotExistException());

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(UserErrorCode.USER_NOT_EXIST.name()));

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", validRequest.getApplicationId(), validRequest.getUserPrompt());
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenQuotaExceeded_expect429() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
                .thenThrow(new GenAIQuotaExceededException());

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isTooManyRequests());

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", validRequest.getApplicationId(), validRequest.getUserPrompt());
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenConcurrentRequest_expect409() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
                .thenThrow(new ConcurrencyException("Another request in progress"));

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", validRequest.getApplicationId(), validRequest.getUserPrompt());
    }

    @Test
    @WithMockUser(username = "testUserID")
    public void resumeAdvisor_whenServiceFails_expect500() throws Exception {
        when(genAIResumeAdvisorService.getAdvice(anyString(), any(), anyString()))
                .thenThrow(new GenAIUsageManagementServiceException("Internal error"));

        mockMvc.perform(post("/api/resume-ai-advisor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));

        verify(genAIResumeAdvisorService, times(1))
                .getAdvice("testUserID", validRequest.getApplicationId(), validRequest.getUserPrompt());
    }
}
