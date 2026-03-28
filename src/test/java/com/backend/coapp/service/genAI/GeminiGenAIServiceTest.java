package com.backend.coapp.service.genAI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.genai.GenAIOutOfServiceException;
import com.backend.coapp.exception.genai.GenAIServiceException;
import com.backend.coapp.exception.genai.OverCharacterLimitException;
import com.backend.coapp.util.GenAIConstants;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.errors.ApiException;
import com.google.genai.types.GenerateContentResponse;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class GeminiGenAIServiceTest {

  private Client geminiClient;
  private Models models;
  private GeminiGenAIService geminiGenAIService;

  private final String MODEL = "gemini-2.0-flash";
  private final String VALID_PROMPT = "Tell me about Spring Boot";
  private final String VALID_RESPONSE = "Spring Boot is a framework...";

  @BeforeEach
  void setUp() throws Exception {
    geminiClient = Mockito.mock(Client.class);
    models = Mockito.mock(Models.class);

    geminiGenAIService = new GeminiGenAIService(geminiClient);

    Field modelField = GeminiGenAIService.class.getDeclaredField("model");
    modelField.setAccessible(true);
    modelField.set(geminiGenAIService, MODEL);

    Field modelsField = Client.class.getDeclaredField("models");
    modelsField.setAccessible(true);
    modelsField.set(geminiClient, models);
  }

  @Test
  void generateResponse_whenValidPrompt_expectReturnResponse() throws Exception {
    GenerateContentResponse mockResponse = Mockito.mock(GenerateContentResponse.class);
    when(mockResponse.text()).thenReturn(VALID_RESPONSE);
    when(models.generateContent(eq(MODEL), eq(VALID_PROMPT), isNull())).thenReturn(mockResponse);

    String result = geminiGenAIService.generateResponse(VALID_PROMPT);

    assertEquals(VALID_RESPONSE, result);
    verify(models, times(1)).generateContent(MODEL, VALID_PROMPT, null);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void generateResponse_whenPromptIsInvalid_expectIllegalArgumentException(String prompt) {
    assertThrows(IllegalArgumentException.class, () -> geminiGenAIService.generateResponse(prompt));

    verifyNoInteractions(models);
  }

  @Test
  void generateResponse_whenGeminiClientThrows_expectGenAIServiceException() {
    when(models.generateContent(eq(MODEL), eq(VALID_PROMPT), isNull()))
        .thenThrow(new RuntimeException("Gemini API failed"));

    GenAIServiceException ex =
        assertThrows(
            GenAIServiceException.class, () -> geminiGenAIService.generateResponse(VALID_PROMPT));

    assertNotNull(ex.getMessage());
  }

  @Test
  void generateResponse_whenPromptExceedsMaxCharacters_expectOverCharacterLimitException() {
    String oversizedPrompt = "a".repeat(GenAIConstants.MAX_TOTAL_CHARACTERS + 1);

    OverCharacterLimitException ex =
        assertThrows(
            OverCharacterLimitException.class,
            () -> geminiGenAIService.generateResponse(oversizedPrompt));

    verifyNoInteractions(models);
    assertNotNull(ex);
  }

  @Test
  void generateResponse_whenGeminiClientThrows429_expectGenAIOutOfServiceException() {
    when(models.generateContent(eq(MODEL), eq(VALID_PROMPT), isNull()))
        .thenThrow(
            new ApiException(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Please try again"));

    GenAIOutOfServiceException ex =
        assertThrows(
            GenAIOutOfServiceException.class,
            () -> geminiGenAIService.generateResponse(VALID_PROMPT));

    assertNotNull(ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {429, 503, 500})
  void generateResponse_whenGeminiClientThrowsOutOfServiceStatus_expectGenAIOutOfServiceException(
      int statusCode) {
    HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
    when(models.generateContent(eq(MODEL), eq(VALID_PROMPT), isNull()))
        .thenThrow(new ApiException(statusCode, httpStatus.getReasonPhrase(), "Please try again"));

    GenAIOutOfServiceException ex =
        assertThrows(
            GenAIOutOfServiceException.class,
            () -> geminiGenAIService.generateResponse(VALID_PROMPT));

    assertNotNull(ex.getMessage());
  }

  @Test
  void generateResponse_whenGeminiClientThrows400_expectGenAIServiceException() {
    when(models.generateContent(eq(MODEL), eq(VALID_PROMPT), isNull()))
        .thenThrow(
            new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Invalid API key"));

    GenAIServiceException ex =
        assertThrows(
            GenAIServiceException.class, () -> geminiGenAIService.generateResponse(VALID_PROMPT));

    assertNotNull(ex.getMessage());
  }

  @Test
  void generateResponse_whenGeminiClientThrowsRuntimeException_expectGenAIServiceException() {
    when(models.generateContent(eq(MODEL), eq(VALID_PROMPT), isNull()))
        .thenThrow(new RuntimeException());

    GenAIServiceException ex =
        assertThrows(
            GenAIServiceException.class, () -> geminiGenAIService.generateResponse(VALID_PROMPT));

    assertNotNull(ex.getMessage());
  }
}
