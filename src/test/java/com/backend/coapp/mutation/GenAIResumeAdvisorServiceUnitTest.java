package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.UserExperienceModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.UserExperienceRepository;
import com.backend.coapp.service.GenAIResumeAdvisorService;
import com.backend.coapp.service.GenAIUsageManagementService;
import com.backend.coapp.service.genAI.GeminiGenAIService;
import com.backend.coapp.util.ApplicationConstants;
import com.backend.coapp.util.GenAIConstants;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class GenAIResumeAdvisorServiceUnitTest {

  private GenAIResumeAdvisorService genAIResumeAdvisorService;
  private GenAIUsageManagementService mockUsageManagementService;
  private GeminiGenAIService mockGeminiService;
  private ApplicationRepository mockApplicationRepository;
  private UserExperienceRepository mockUserExperienceRepository;

  private ApplicationModel fooApplication;

  private final String USER_ID = "user_001";
  private final String APP_ID = "app_001";
  private final LocalDate START_DATE = LocalDate.now().minusYears(1);
  private final LocalDate END_DATE = LocalDate.now();
  private final String VALID_PROMPT = "Help me improve my resume";
  private final String VALID_RESPONSE = "Here are some improvements...";

  @BeforeEach
  public void setUp() {
    mockUsageManagementService = Mockito.mock(GenAIUsageManagementService.class);
    mockGeminiService = Mockito.mock(GeminiGenAIService.class);
    mockApplicationRepository = Mockito.mock(ApplicationRepository.class);
    mockUserExperienceRepository = Mockito.mock(UserExperienceRepository.class);

    fooApplication =
        ApplicationModel.builder()
            .userId(USER_ID)
            .companyId("company_001")
            .jobTitle("Software Engineer")
            .jobDescription("Full stack role")
            .status(ApplicationStatus.APPLIED)
            .build();
    ReflectionTestUtils.setField(fooApplication, "id", APP_ID);

    when(mockUserExperienceRepository.findAllByUserId(USER_ID)).thenReturn(new ArrayList<>());

    genAIResumeAdvisorService =
        new GenAIResumeAdvisorService(
            mockGeminiService,
            mockUsageManagementService,
            mockApplicationRepository,
            mockUserExperienceRepository);
  }

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  @Test
  public void constructor_expectInitCorrectInstance() {
    assertEquals(mockGeminiService, genAIResumeAdvisorService.getGenAIService());
    assertEquals(
        mockUsageManagementService, genAIResumeAdvisorService.getGenAIUsageManagementService());
    assertEquals(mockApplicationRepository, genAIResumeAdvisorService.getApplicationRepository());
    assertEquals(
        mockUserExperienceRepository, genAIResumeAdvisorService.getUserExperienceRepository());
  }

  // -------------------------------------------------------------------------
  // getAdvice — no application
  // -------------------------------------------------------------------------

  @Test
  public void getAdvice_whenValidPromptNoApplicationNoExperience_expectReturnResponse() {
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    String result = genAIResumeAdvisorService.getAdvice(USER_ID, null, VALID_PROMPT);

    assertEquals(VALID_RESPONSE, result);
    verify(mockUsageManagementService, times(1)).checkAndIncrementUsage(USER_ID);
    verify(mockGeminiService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    !prompt.contains("Software Engineer") && prompt.contains("no experience")));
  }

  @Test
  public void getAdvice_expectInstructionIncludedInFinalPrompt() {
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    genAIResumeAdvisorService.getAdvice(USER_ID, null, VALID_PROMPT);

    verify(mockGeminiService)
        .generateResponse(argThat(prompt -> prompt.contains("professional career advisor")));
  }

  @Test
  public void getAdvice_whenPromptExceedsMaxCharacters_expectOverCharacterLimitException() {
    String oversizedPrompt = "a".repeat(GenAIConstants.MAX_PROMPT_CHARACTERS + 1);

    assertThrows(
        OverCharacterLimitException.class,
        () -> genAIResumeAdvisorService.getAdvice(USER_ID, null, oversizedPrompt));

    verifyNoInteractions(mockUsageManagementService);
    verifyNoInteractions(mockGeminiService);
  }

  @Test
  public void getAdvice_whenPromptExactlyAtLimit_expectNoException() {
    String exactPrompt = "a".repeat(GenAIConstants.MAX_PROMPT_CHARACTERS);
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    assertDoesNotThrow(() -> genAIResumeAdvisorService.getAdvice(USER_ID, null, exactPrompt));
  }

  // -------------------------------------------------------------------------
  // getAdvice — with application
  // -------------------------------------------------------------------------

  @Test
  public void getAdvice_whenValidPromptWithApplicationNoExperience_expectReturnResponse() {
    when(mockApplicationRepository.findById(APP_ID)).thenReturn(Optional.of(fooApplication));
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    String result = genAIResumeAdvisorService.getAdvice(USER_ID, APP_ID, VALID_PROMPT);

    assertEquals(VALID_RESPONSE, result);
    verify(mockGeminiService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    prompt.contains(fooApplication.getJobTitle())
                        && prompt.contains(fooApplication.getJobDescription())
                        && prompt.contains("no experience")));
    verify(mockUsageManagementService, times(1)).checkAndIncrementUsage(USER_ID);
  }

  @Test
  public void getAdvice_whenValidPromptWithApplicationNoDescription_expectReturnResponse() {
    ApplicationModel noDescApp =
        ApplicationModel.builder()
            .userId(USER_ID)
            .companyId("company_001")
            .jobTitle("Software Engineer II")
            .status(ApplicationStatus.APPLIED)
            .build();
    ReflectionTestUtils.setField(noDescApp, "id", "app_002");

    when(mockApplicationRepository.findById("app_002")).thenReturn(Optional.of(noDescApp));
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    String result = genAIResumeAdvisorService.getAdvice(USER_ID, "app_002", VALID_PROMPT);

    assertEquals(VALID_RESPONSE, result);
    verify(mockGeminiService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    prompt.contains(noDescApp.getJobTitle())
                        && prompt.contains("No job description")
                        && prompt.contains("no experience")));
  }

  @Test
  public void getAdvice_whenApplicationNotFound_expectApplicationNotFoundException() {
    when(mockApplicationRepository.findById("nonExistentAppId")).thenReturn(Optional.empty());

    assertThrows(
        ApplicationNotFoundException.class,
        () -> genAIResumeAdvisorService.getAdvice(USER_ID, "nonExistentAppId", VALID_PROMPT));

    verifyNoInteractions(mockUsageManagementService);
    verifyNoInteractions(mockGeminiService);
  }

  @Test
  public void getAdvice_whenApplicationNotOwnedByUser_expectApplicationNotOwnedException() {
    when(mockApplicationRepository.findById(APP_ID)).thenReturn(Optional.of(fooApplication));

    assertThrows(
        ApplicationNotOwnedException.class,
        () -> genAIResumeAdvisorService.getAdvice("anotherUserId", APP_ID, VALID_PROMPT));

    verifyNoInteractions(mockUsageManagementService);
    verifyNoInteractions(mockGeminiService);
  }

  @Test
  public void
      getAdvice_whenApplicationJobDescriptionExceedsLimit_expectOverCharacterLimitException() {
    ApplicationModel longDescApp =
        ApplicationModel.builder()
            .userId(USER_ID)
            .companyId("company_001")
            .jobTitle("Software Engineer")
            .jobDescription("a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH + 1))
            .status(ApplicationStatus.APPLIED)
            .build();
    ReflectionTestUtils.setField(longDescApp, "id", "app_long_desc");

    when(mockApplicationRepository.findById("app_long_desc")).thenReturn(Optional.of(longDescApp));

    OverCharacterLimitException ex =
        assertThrows(
            OverCharacterLimitException.class,
            () -> genAIResumeAdvisorService.getAdvice(USER_ID, "app_long_desc", VALID_PROMPT));

    verifyNoInteractions(mockUsageManagementService);
    verifyNoInteractions(mockGeminiService);
    assertTrue(
        ex.getMessage().contains(String.valueOf(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH)));
    assertTrue(ex.getMessage().contains(String.valueOf(ApplicationConstants.MAX_JOB_TITLE_LENGTH)));
  }

  @Test
  public void getAdvice_whenApplicationJobTitleExceedsLimit_expectOverCharacterLimitException() {
    ApplicationModel longTitleApp =
        ApplicationModel.builder()
            .userId(USER_ID)
            .companyId("company_001")
            .jobTitle("a".repeat(ApplicationConstants.MAX_JOB_TITLE_LENGTH + 1))
            .jobDescription("Normal job description")
            .status(ApplicationStatus.APPLIED)
            .build();
    ReflectionTestUtils.setField(longTitleApp, "id", "app_long_title");

    when(mockApplicationRepository.findById("app_long_title"))
        .thenReturn(Optional.of(longTitleApp));

    assertThrows(
        OverCharacterLimitException.class,
        () -> genAIResumeAdvisorService.getAdvice(USER_ID, "app_long_title", VALID_PROMPT));

    verifyNoInteractions(mockUsageManagementService);
    verifyNoInteractions(mockGeminiService);
  }

  @Test
  public void
      getAdvice_whenBothJobTitleAndDescriptionExceedLimit_expectOverCharacterLimitException() {
    ApplicationModel bothExceedApp =
        ApplicationModel.builder()
            .userId(USER_ID)
            .companyId("company_001")
            .jobTitle("a".repeat(ApplicationConstants.MAX_JOB_TITLE_LENGTH + 1))
            .jobDescription("a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH + 1))
            .status(ApplicationStatus.APPLIED)
            .build();
    ReflectionTestUtils.setField(bothExceedApp, "id", "app_both_exceed");

    when(mockApplicationRepository.findById("app_both_exceed"))
        .thenReturn(Optional.of(bothExceedApp));

    assertThrows(
        OverCharacterLimitException.class,
        () -> genAIResumeAdvisorService.getAdvice(USER_ID, "app_both_exceed", VALID_PROMPT));

    verifyNoInteractions(mockUsageManagementService);
    verifyNoInteractions(mockGeminiService);
  }

  @Test
  public void getAdvice_whenApplicationJobDescriptionExactlyAtLimit_expectNoException() {
    ApplicationModel exactDescApp =
        ApplicationModel.builder()
            .userId(USER_ID)
            .companyId("company_001")
            .jobTitle("Software Engineer")
            .jobDescription("a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH))
            .status(ApplicationStatus.APPLIED)
            .build();
    ReflectionTestUtils.setField(exactDescApp, "id", "app_exact_desc");

    when(mockApplicationRepository.findById("app_exact_desc"))
        .thenReturn(Optional.of(exactDescApp));
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    assertDoesNotThrow(
        () -> genAIResumeAdvisorService.getAdvice(USER_ID, "app_exact_desc", VALID_PROMPT));
  }

  @Test
  public void getAdvice_whenApplicationJobTitleExactlyAtLimit_expectNoException() {
    ApplicationModel exactTitleApp =
        ApplicationModel.builder()
            .userId(USER_ID)
            .companyId("company_001")
            .jobTitle("a".repeat(ApplicationConstants.MAX_JOB_TITLE_LENGTH))
            .jobDescription("Normal job description")
            .status(ApplicationStatus.APPLIED)
            .build();
    ReflectionTestUtils.setField(exactTitleApp, "id", "app_exact_title");

    when(mockApplicationRepository.findById("app_exact_title"))
        .thenReturn(Optional.of(exactTitleApp));
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    assertDoesNotThrow(
        () -> genAIResumeAdvisorService.getAdvice(USER_ID, "app_exact_title", VALID_PROMPT));
  }

  // -------------------------------------------------------------------------
  // getAdvice — experience
  // -------------------------------------------------------------------------

  @Test
  public void getAdvice_whenUserHasExperience_expectExperienceIncludedInPrompt() {
    UserExperienceModel experience =
        new UserExperienceModel(
            USER_ID,
            "company_001",
            "Software Engineer",
            "Built microservices",
            START_DATE,
            END_DATE);

    when(mockUserExperienceRepository.findAllByUserId(USER_ID))
        .thenReturn(new ArrayList<>(List.of(experience)));
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    genAIResumeAdvisorService.getAdvice(USER_ID, null, VALID_PROMPT);

    verify(mockGeminiService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    prompt.contains(experience.getRoleTitle())
                        && prompt.contains(experience.getRoleDescription())));
  }

  @Test
  public void getAdvice_whenExperienceLengthExactlyAtLimit_expectIncludedInPrompt() {
    // header = "Student work experiences are listed in the format (Job Title - Job Description):\n"
    // = 81 chars
    // experience entry = roleTitle + " - " + roleDescription + "\n" = "T - " + desc + "\n" =
    // descLength + 5
    // target: 81 + (descLength + 5) == MAX → descLength = MAX - 86
    int descLength = GenAIConstants.MAX_EXPERIENCE_SUMMARY_CHARACTER - 86;

    UserExperienceModel experience =
        new UserExperienceModel(
            USER_ID, "company_001", "T", "a".repeat(descLength), START_DATE, END_DATE);

    when(mockUserExperienceRepository.findAllByUserId(USER_ID))
        .thenReturn(new ArrayList<>(List.of(experience)));
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    genAIResumeAdvisorService.getAdvice(USER_ID, null, VALID_PROMPT);

    // with ">", total == MAX → condition false → experience IS appended
    // with ">=", total == MAX → condition true → experience is NOT appended (kills mutation)
    verify(mockGeminiService)
        .generateResponse(
            argThat(prompt -> prompt.contains("T - ") && !prompt.contains("no experience")));
  }

  @Test
  public void getAdvice_whenMultipleExperiences_expectSortedByMostRecent() {
    UserExperienceModel firstExperience =
        new UserExperienceModel(
            USER_ID,
            "company_001",
            "Junior Engineer",
            "Old role",
            START_DATE.minusYears(1),
            END_DATE.minusYears(1));
    UserExperienceModel secondExperience =
        new UserExperienceModel(
            USER_ID, "company_001", "Senior Engineer", "Recent role", START_DATE, END_DATE);

    when(mockUserExperienceRepository.findAllByUserId(USER_ID))
        .thenReturn(new ArrayList<>(List.of(firstExperience, secondExperience)));
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    genAIResumeAdvisorService.getAdvice(USER_ID, null, VALID_PROMPT);

    verify(mockGeminiService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    prompt.contains(firstExperience.getRoleTitle())
                        && prompt.contains(firstExperience.getRoleDescription())
                        && prompt.contains(secondExperience.getRoleTitle())
                        && prompt.contains(secondExperience.getRoleDescription())
                        && prompt.indexOf(secondExperience.getRoleTitle())
                            < prompt.indexOf(firstExperience.getRoleTitle())));
  }

  @Test
  public void getAdvice_whenMultipleExperiencesWithOneExceedChar_expectSortedByMostRecent() {
    UserExperienceModel firstExperience =
        new UserExperienceModel(
            USER_ID,
            "company_001",
            "First role",
            "First role description",
            START_DATE.minusYears(3),
            END_DATE.minusYears(3));
    UserExperienceModel secondExperience =
        new UserExperienceModel(
            USER_ID,
            "company_001",
            "Second role",
            "a".repeat(GenAIConstants.MAX_EXPERIENCE_SUMMARY_CHARACTER + 1),
            START_DATE.minusYears(2),
            END_DATE.minusYears(2));
    UserExperienceModel thirdExperience =
        new UserExperienceModel(
            USER_ID,
            "company_001",
            "Third role",
            "Third role description",
            START_DATE.minusYears(1),
            END_DATE.minusYears(1));
    UserExperienceModel fourthExperience =
        new UserExperienceModel(
            USER_ID, "company_001", "Fourth role", "Fourth role description", START_DATE, END_DATE);

    when(mockUserExperienceRepository.findAllByUserId(USER_ID))
        .thenReturn(
            new ArrayList<>(
                List.of(firstExperience, secondExperience, thirdExperience, fourthExperience)));
    when(mockGeminiService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    genAIResumeAdvisorService.getAdvice(USER_ID, null, VALID_PROMPT);

    verify(mockGeminiService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    prompt.contains(fourthExperience.getRoleTitle())
                        && prompt.contains(fourthExperience.getRoleDescription())
                        && prompt.contains(thirdExperience.getRoleTitle())
                        && prompt.contains(thirdExperience.getRoleDescription())
                        && !prompt.contains(secondExperience.getRoleTitle())
                        && !prompt.contains(secondExperience.getRoleDescription())
                        && !prompt.contains(firstExperience.getRoleTitle())
                        && !prompt.contains(firstExperience.getRoleDescription())
                        && prompt.indexOf(fourthExperience.getRoleTitle())
                            < prompt.indexOf(thirdExperience.getRoleTitle())));
  }

  // -------------------------------------------------------------------------
  // getAdvice — usage management
  // -------------------------------------------------------------------------

  @Test
  public void getAdvice_whenUsageCheckThrows_expectExceptionPropagated() {
    doThrow(new GenAIQuotaExceededException())
        .when(mockUsageManagementService)
        .checkAndIncrementUsage(anyString());

    assertThrows(
        GenAIQuotaExceededException.class,
        () -> genAIResumeAdvisorService.getAdvice(USER_ID, null, VALID_PROMPT));

    verifyNoInteractions(mockGeminiService);
  }

  @Test
  public void getAdvice_whenGenAIServiceThrows_expectExceptionPropagated() {
    when(mockGeminiService.generateResponse(anyString()))
        .thenThrow(new GenAIServiceException("Gemini failed"));

    assertThrows(
        GenAIServiceException.class,
        () -> genAIResumeAdvisorService.getAdvice(USER_ID, null, VALID_PROMPT));

    verify(mockUsageManagementService, times(1)).checkAndIncrementUsage(USER_ID);
    verify(mockUsageManagementService, times(1)).decrementUsage(USER_ID);
  }
}
