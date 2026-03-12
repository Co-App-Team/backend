package com.backend.coapp.service;

import com.backend.coapp.exception.*;
import com.backend.coapp.exception.application.ApplicationNotFoundException;
import com.backend.coapp.exception.genai.ConcurrencyException;
import com.backend.coapp.exception.genai.GenAIQuotaExceededException;
import com.backend.coapp.exception.genai.GenAIUsageManagementServiceException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.document.UserExperienceModel;
import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.UserExperienceRepository;
import com.backend.coapp.service.genAI.GenAIService;
import com.backend.coapp.util.ApplicationConstants;
import com.backend.coapp.util.GenAIConstants;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter // For testing only
public class GenAIResumeAdvisorService {
  private final GenAIService genAIService;
  private final GenAIUsageManagementService genAIUsageManagementService;
  private final ApplicationRepository applicationRepository;
  private final UserExperienceRepository userExperienceRepository;

  @Autowired
  public GenAIResumeAdvisorService(
      GenAIService genAIService,
      GenAIUsageManagementService genAIUsageManagementService,
      ApplicationRepository applicationRepository,
      UserExperienceRepository userExperienceRepository) {
    this.genAIService = genAIService;
    this.genAIUsageManagementService = genAIUsageManagementService;
    this.applicationRepository = applicationRepository;
    this.userExperienceRepository = userExperienceRepository;
  }

  /**
   * Get feedback from GenAI for application
   *
   * @param userId ID of a user
   * @param applicationId ID of the application that user want to apply for (Optional)
   * @param prompt prompt from user
   * @return response from GenAI
   * @throws OverCharacterLimitException when request has too many characters
   * @throws ApplicationNotOwnedException when application doesn't belong to the user
   * @throws ApplicationNotFoundException when application can't be found
   * @throws GenAIUsageManagementServiceException when there is something wrong in GenAI usage
   *     management
   * @throws UserNotFoundException when user doesn't exist
   * @throws GenAIQuotaExceededException when user exceed GenAI usage limit
   * @throws ConcurrencyException when the same user make a request twice
   */
  public String getAdvice(String userId, String applicationId, String prompt)
      throws OverCharacterLimitException,
          ApplicationNotOwnedException,
          ApplicationNotFoundException,
          GenAIUsageManagementServiceException,
          UserNotFoundException,
          GenAIQuotaExceededException,
          ConcurrencyException {
    String applicationJobDescription = null;
    String applicationJobTitle = null;
    if (prompt.length() > GenAIConstants.MAX_PROMPT_CHARACTERS) {
      throw new OverCharacterLimitException(
          "Your prompt is over character limit of "
              + GenAIConstants.MAX_PROMPT_CHARACTERS
              + ". Please try again");
    }

    if (applicationId != null) {
      ApplicationModel applicationModel =
          this.applicationRepository
              .findById(applicationId)
              .orElseThrow(ApplicationNotFoundException::new);
      if (!applicationModel.getUserId().equals(userId)) {
        throw new ApplicationNotOwnedException();
      }
      applicationJobDescription = applicationModel.getJobDescription();
      if (applicationJobDescription == null) {
        applicationJobDescription = "No job description";
      }
      applicationJobTitle = applicationModel.getJobTitle();
      if (applicationJobDescription.length() > ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH
          || applicationJobTitle.length() > ApplicationConstants.MAX_JOB_TITLE_LENGTH) {
        throw new OverCharacterLimitException(
            "The selected job application has job description too long (over %s characters) or job title too long (over %s characters). Please shorten the description and try again"
                .formatted(
                    ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH,
                    ApplicationConstants.MAX_JOB_TITLE_LENGTH));
      }
    }
    List<UserExperienceModel> experiences = userExperienceRepository.findAllByUserId(userId);
    String experienceSummary = this.prepareExperienceDescription((experiences));
    String context =
        this.getContext(applicationJobTitle, applicationJobDescription, experienceSummary);
    String instruction = this.getInstruction();

    String finalPrompt = this.prepareFinalPrompt(instruction, context, prompt);

    this.genAIUsageManagementService.checkAndIncrementUsage(userId);
    String response;
    try {
      response = this.genAIService.generateResponse(finalPrompt);
    } catch (Exception e) {
      this.genAIUsageManagementService.decrementUsage(userId);
      throw e;
    }
    return response;
  }

  /**
   * Get instruction for GenAI
   *
   * @return set of instructions for GenAI
   */
  private String getInstruction() {
    return """
        You are a professional career advisor and resume expert.

        Your task is to review a student's resume or cover letter and help improve its clarity, impact, and professionalism.

        Please:
        1. Provide clear feedback and suggest improvements on content and wording.
        2. Rewrite improved versions while preserving the original meaning.
        3. Maintain a professional and concise tone suitable for co-op job applications.
        4. Do NOT exaggerate, or invent any skills, experiences, or achievements that are not explicitly provided by the user.

        Output format:
        - Section 1: Key Feedback (bullet points)
        - Section 2: Improved Version (rewritten content)
        """;
  }

  /**
   * Generate context about user for GenAI
   *
   * @param applicationJobTitle job title that the user want to apply
   * @param applicationJobDescription job description of the job
   * @param experienceDescription user's experiences
   * @return context about user for GenAI
   */
  private String getContext(
      String applicationJobTitle, String applicationJobDescription, String experienceDescription) {
    if (applicationJobTitle != null) {

      return """
              You are supporting a co-op student who is applying for %s.

              Here is the job description of the job: %s

              %s
              """
          .formatted(applicationJobTitle, applicationJobDescription, experienceDescription);
    } else {
      return "%s".formatted(experienceDescription);
    }
  }

  /**
   * Prepare description about user's experiences
   *
   * @param allExperience all experiences of the user
   * @return summary about user's experiences
   */
  private String prepareExperienceDescription(List<UserExperienceModel> allExperience) {
    StringBuilder experienceDescription;
    if (allExperience.isEmpty()) {
      experienceDescription = new StringBuilder("This student has no experience.");
    } else {
      experienceDescription =
          new StringBuilder(
              """
        Student work experiences are listed in the format (Job Title - Job Description):
        """);

      allExperience.sort(Comparator.comparing(UserExperienceModel::getStartDate).reversed());

      for (UserExperienceModel exp : allExperience) {

        String experience = exp.getRoleTitle() + " - " + exp.getRoleDescription() + "\n";

        if (experienceDescription.length() + experience.length()
            > GenAIConstants.MAX_EXPERIENCE_SUMMARY_CHARACTER) {
          break;
        }

        experienceDescription.append(experience);
      }
    }
    return experienceDescription.toString();
  }

  /**
   * Prepare final prompt for GenAI
   *
   * @param instruction set of instructions
   * @param context additional information about user
   * @param userPrompt prompt from the user
   * @return
   */
  private String prepareFinalPrompt(String instruction, String context, String userPrompt) {
    return """
           %s

           %s

           Use information provided above to address the request below:
           %s
           """
        .formatted(instruction, context, userPrompt);
  }
}
