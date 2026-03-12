package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserExperienceModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserExperienceRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.genAI.GeminiGenAIService;
import com.backend.coapp.util.ApplicationConstants;
import com.backend.coapp.util.GenAIConstants;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Parts of the unit test are written with help of Claude (Sonnet 4.6) */
@SpringBootTest
@Testcontainers
public class GenAIResumeAdvisorServiceTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private UserExperienceRepository userExperienceRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CompanyRepository companyRepository;

  private GenAIResumeAdvisorService genAIResumeAdvisorService;
  private GenAIUsageManagementService genAIUsageManagementService;
  private GeminiGenAIService geminiGenAIService;

  private UserModel fooUser;
  private CompanyModel fooCompany;
  private ApplicationModel fooApplication;

  private final LocalDate START_DATE = LocalDate.now().minusYears(1);
  private final LocalDate END_DATE = LocalDate.now();
  private final String VALID_PROMPT = "Help me improve my resume";
  private final String VALID_RESPONSE = "Here are some improvements...";

  @BeforeEach
  public void setUp() {
    applicationRepository.deleteAll();
    userExperienceRepository.deleteAll();
    userRepository.deleteAll();
    companyRepository.deleteAll();

    fooUser =
        userRepository.save(
            new UserModel(
                null,
                "foo@mail.com",
                "encodedPassword",
                "Foo",
                "Bar",
                true,
                UserModel.DEFAULT_VERIFICATION_CODE));

    fooCompany =
        companyRepository.save(new CompanyModel("Foo Company", "Winnipeg", "https://foo.com"));

    fooApplication =
        applicationRepository.save(
            ApplicationModel.builder()
                .userId(fooUser.getId())
                .companyId(fooCompany.getId())
                .jobTitle("Software Engineer")
                .jobDescription("Full stack role")
                .status(ApplicationStatus.APPLIED)
                .build());

    this.genAIUsageManagementService = Mockito.mock(GenAIUsageManagementService.class);
    this.geminiGenAIService = Mockito.mock(GeminiGenAIService.class);
    this.genAIResumeAdvisorService =
        new GenAIResumeAdvisorService(
            this.geminiGenAIService,
            this.genAIUsageManagementService,
            this.applicationRepository,
            this.userExperienceRepository);
  }

  @Test
  public void constructor_expectInitCorrectInstance() {
    assertEquals(geminiGenAIService, genAIResumeAdvisorService.getGenAIService());
    assertEquals(
        genAIUsageManagementService, genAIResumeAdvisorService.getGenAIUsageManagementService());
    assertEquals(applicationRepository, genAIResumeAdvisorService.getApplicationRepository());
    assertEquals(userExperienceRepository, genAIResumeAdvisorService.getUserExperienceRepository());
  }

  @Test
  public void getAdvice_whenValidPromptNoApplicationNoExperience_expectReturnResponse() {
    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    String result = genAIResumeAdvisorService.getAdvice(fooUser.getId(), null, VALID_PROMPT);

    assertEquals(VALID_RESPONSE, result);
    verify(genAIUsageManagementService, times(1)).checkAndIncrementUsage(fooUser.getId());
    verify(geminiGenAIService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    !prompt.contains(fooApplication.getJobTitle())
                        && prompt.contains("no experience")));
  }

  @Test
  public void getAdvice_whenValidPromptWithApplicationNoExperience_expectReturnResponse() {
    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    String result =
        genAIResumeAdvisorService.getAdvice(fooUser.getId(), fooApplication.getId(), VALID_PROMPT);

    assertEquals(VALID_RESPONSE, result);
    verify(geminiGenAIService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    prompt.contains(fooApplication.getJobTitle())
                        && prompt.contains(fooApplication.getJobDescription())
                        && prompt.contains("no experience")));
    verify(genAIUsageManagementService, times(1)).checkAndIncrementUsage(fooUser.getId());
  }

  @Test
  public void getAdvice_whenValidPromptWithApplicationNoDescription_expectReturnResponse() {
    ApplicationModel fooApplicationNoDescription =
        applicationRepository.save(
            ApplicationModel.builder()
                .userId(fooUser.getId())
                .companyId(fooCompany.getId())
                .jobTitle("Software Engineer II")
                .status(ApplicationStatus.APPLIED)
                .build());

    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    String result =
        genAIResumeAdvisorService.getAdvice(
            fooUser.getId(), fooApplicationNoDescription.getId(), VALID_PROMPT);

    assertEquals(VALID_RESPONSE, result);
    verify(geminiGenAIService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    prompt.contains(fooApplicationNoDescription.getJobTitle())
                        && prompt.contains("No job description")
                        && prompt.contains("no experience")));
    verify(genAIUsageManagementService, times(1)).checkAndIncrementUsage(fooUser.getId());
  }

  @Test
  public void getAdvice_whenPromptExceedsMaxCharacters_expectOverCharacterLimitException() {
    String oversizedPrompt = "a".repeat(GenAIConstants.MAX_PROMPT_CHARACTERS + 1);

    assertThrows(
        OverCharacterLimitException.class,
        () -> genAIResumeAdvisorService.getAdvice(fooUser.getId(), null, oversizedPrompt));

    verifyNoInteractions(genAIUsageManagementService);
    verifyNoInteractions(geminiGenAIService);
  }

  @Test
  public void getAdvice_whenPromptExactlyAtLimit_expectNoException() {
    String exactPrompt = "a".repeat(GenAIConstants.MAX_PROMPT_CHARACTERS);
    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    assertDoesNotThrow(
        () -> genAIResumeAdvisorService.getAdvice(fooUser.getId(), null, exactPrompt));
  }

  @Test
  public void getAdvice_whenApplicationNotFound_expectApplicationNotFoundException() {
    assertThrows(
        ApplicationNotFoundException.class,
        () ->
            genAIResumeAdvisorService.getAdvice(fooUser.getId(), "nonExistentAppId", VALID_PROMPT));

    verifyNoInteractions(genAIUsageManagementService);
    verifyNoInteractions(geminiGenAIService);
  }

  @Test
  public void getAdvice_whenApplicationNotOwnedByUser_expectApplicationNotOwnedException() {
    assertThrows(
        ApplicationNotOwnedException.class,
        () ->
            genAIResumeAdvisorService.getAdvice(
                "anotherUserId", fooApplication.getId(), VALID_PROMPT));

    verifyNoInteractions(genAIUsageManagementService);
    verifyNoInteractions(geminiGenAIService);
  }

  @Test
  public void
      getAdvice_whenApplicationJobDescriptionExceedsLimit_expectOverCharacterLimitException() {
    ApplicationModel longDescApp =
        applicationRepository.save(
            ApplicationModel.builder()
                .userId(fooUser.getId())
                .companyId(fooCompany.getId())
                .jobTitle("Software Engineer")
                .jobDescription("a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH + 1))
                .status(ApplicationStatus.APPLIED)
                .build());

    OverCharacterLimitException ex =
        assertThrows(
            OverCharacterLimitException.class,
            () ->
                genAIResumeAdvisorService.getAdvice(
                    fooUser.getId(), longDescApp.getId(), VALID_PROMPT));

    verifyNoInteractions(genAIUsageManagementService);
    verifyNoInteractions(geminiGenAIService);

    assertTrue(
        ex.getMessage().contains(String.valueOf(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH)));
    assertTrue(ex.getMessage().contains(String.valueOf(ApplicationConstants.MAX_JOB_TITLE_LENGTH)));
  }

  @Test
  public void getAdvice_whenApplicationJobTitleExceedsLimit_expectOverCharacterLimitException() {
    ApplicationModel longTitleApp =
        applicationRepository.save(
            ApplicationModel.builder()
                .userId(fooUser.getId())
                .companyId(fooCompany.getId())
                .jobTitle("a".repeat(ApplicationConstants.MAX_JOB_TITLE_LENGTH + 1))
                .jobDescription("Normal job description")
                .status(ApplicationStatus.APPLIED)
                .build());

    assertThrows(
        OverCharacterLimitException.class,
        () ->
            genAIResumeAdvisorService.getAdvice(
                fooUser.getId(), longTitleApp.getId(), VALID_PROMPT));

    verifyNoInteractions(genAIUsageManagementService);
    verifyNoInteractions(geminiGenAIService);
  }

  @Test
  public void getAdvice_whenApplicationJobDescriptionExactlyAtLimit_expectNoException() {
    ApplicationModel exactDescApp =
        applicationRepository.save(
            ApplicationModel.builder()
                .userId(fooUser.getId())
                .companyId(fooCompany.getId())
                .jobTitle("Software Engineer")
                .jobDescription("a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH))
                .status(ApplicationStatus.APPLIED)
                .build());
    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    assertDoesNotThrow(
        () ->
            genAIResumeAdvisorService.getAdvice(
                fooUser.getId(), exactDescApp.getId(), VALID_PROMPT));
  }

  @Test
  public void getAdvice_whenApplicationJobTitleExactlyAtLimit_expectNoException() {
    ApplicationModel exactTitleApp =
        applicationRepository.save(
            ApplicationModel.builder()
                .userId(fooUser.getId())
                .companyId(fooCompany.getId())
                .jobTitle("a".repeat(ApplicationConstants.MAX_JOB_TITLE_LENGTH))
                .jobDescription("Normal job description")
                .status(ApplicationStatus.APPLIED)
                .build());
    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    assertDoesNotThrow(
        () ->
            genAIResumeAdvisorService.getAdvice(
                fooUser.getId(), exactTitleApp.getId(), VALID_PROMPT));
  }

  @Test
  public void
      getAdvice_whenBothJobTitleAndDescriptionExceedLimit_expectOverCharacterLimitException() {
    ApplicationModel bothExceedApp =
        applicationRepository.save(
            ApplicationModel.builder()
                .userId(fooUser.getId())
                .companyId(fooCompany.getId())
                .jobTitle("a".repeat(ApplicationConstants.MAX_JOB_TITLE_LENGTH + 1))
                .jobDescription("a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH + 1))
                .status(ApplicationStatus.APPLIED)
                .build());

    assertThrows(
        OverCharacterLimitException.class,
        () ->
            genAIResumeAdvisorService.getAdvice(
                fooUser.getId(), bothExceedApp.getId(), VALID_PROMPT));

    verifyNoInteractions(genAIUsageManagementService);
    verifyNoInteractions(geminiGenAIService);
  }

  @Test
  public void getAdvice_whenUserHasExperience_expectExperienceIncludedInPrompt() {
    UserExperienceModel experience =
        new UserExperienceModel(
            fooUser.getId(),
            fooCompany.getId(),
            "Software Engineer",
            "Built microservices",
            START_DATE,
            END_DATE);

    userExperienceRepository.save(experience);
    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    genAIResumeAdvisorService.getAdvice(fooUser.getId(), null, VALID_PROMPT);

    verify(geminiGenAIService, times(1))
        .generateResponse(
            argThat(
                prompt ->
                    prompt.contains(experience.getRoleTitle())
                        && prompt.contains(experience.getRoleDescription())));
  }

  @Test
  public void getAdvice_whenUsageCheckThrows_expectExceptionPropagated() {
    doThrow(new GenAIQuotaExceededException())
        .when(genAIUsageManagementService)
        .checkAndIncrementUsage(anyString());

    assertThrows(
        GenAIQuotaExceededException.class,
        () -> genAIResumeAdvisorService.getAdvice(fooUser.getId(), null, VALID_PROMPT));

    verifyNoInteractions(geminiGenAIService);
  }

  @Test
  public void getAdvice_whenGenAIServiceThrows_expectExceptionPropagated() {
    when(geminiGenAIService.generateResponse(anyString()))
        .thenThrow(new GenAIServiceException("Gemini failed"));

    assertThrows(
        GenAIServiceException.class,
        () -> genAIResumeAdvisorService.getAdvice(fooUser.getId(), null, VALID_PROMPT));
    verify(genAIUsageManagementService, times(1)).checkAndIncrementUsage(fooUser.getId());
    verify(genAIUsageManagementService, times(1)).decrementUsage(fooUser.getId());
  }

  @Test
  public void getAdvice_whenMultipleExperiences_expectSortedByMostRecent() {
    UserExperienceModel firstExperience =
        new UserExperienceModel(
            fooUser.getId(),
            fooCompany.getId(),
            "Junior Engineer",
            "Old role",
            START_DATE.minusYears(1),
            END_DATE.minusYears(1));

    UserExperienceModel secondExperience =
        new UserExperienceModel(
            fooUser.getId(),
            fooCompany.getId(),
            "Senior Engineer",
            "Recent role",
            START_DATE,
            END_DATE);

    userExperienceRepository.save(firstExperience);
    userExperienceRepository.save(secondExperience);
    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    genAIResumeAdvisorService.getAdvice(fooUser.getId(), null, VALID_PROMPT);

    verify(geminiGenAIService, times(1))
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
  public void getAdvice_whenMultipleExperiencesWithOnExceedChar_expectSortedByMostRecent() {
    UserExperienceModel firstExperience =
        new UserExperienceModel(
            fooUser.getId(),
            fooCompany.getId(),
            "First role",
            "First role description",
            START_DATE.minusYears(3),
            END_DATE.minusYears(3));

    UserExperienceModel secondExperience =
        new UserExperienceModel(
            fooUser.getId(),
            fooCompany.getId(),
            "Second role",
            "a".repeat(GenAIConstants.MAX_EXPERIENCE_SUMMARY_CHARACTER + 1),
            START_DATE.minusYears(2),
            END_DATE.minusYears(2));

    UserExperienceModel thirdExperience =
        new UserExperienceModel(
            fooUser.getId(),
            fooCompany.getId(),
            "Third role",
            "Third role description",
            START_DATE.minusYears(1),
            END_DATE.minusYears(1));

    UserExperienceModel fourthExperience =
        new UserExperienceModel(
            fooUser.getId(),
            fooCompany.getId(),
            "Fourth role",
            "Fourth role description",
            START_DATE,
            END_DATE);

    userExperienceRepository.save(firstExperience);
    userExperienceRepository.save(secondExperience);
    userExperienceRepository.save(thirdExperience);
    userExperienceRepository.save(fourthExperience);
    when(geminiGenAIService.generateResponse(anyString())).thenReturn(VALID_RESPONSE);

    genAIResumeAdvisorService.getAdvice(fooUser.getId(), null, VALID_PROMPT);

    verify(geminiGenAIService, times(1))
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
}
