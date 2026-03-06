package com.backend.coapp.service;

import com.backend.coapp.exception.ApplicationNotFoundException;
import com.backend.coapp.exception.ApplicationNotOwnedException;
import com.backend.coapp.exception.OverCharacterLimitException;
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

  public String getAdvice(String userId, String applicationId, String prompt) {
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
    List<UserExperienceModel> allExperience = userExperienceRepository.findAllByUserId(userId);
    String experienceSummary = this.prepareExperienceDescription((allExperience));
    String context =
        this.getContext(applicationJobTitle, applicationJobDescription, experienceSummary);
    String instruction = this.getInstruction();

    String finalPrompt = this.prepareFinalPrompt(instruction, context, prompt);

    this.genAIUsageManagementService.checkAndIncrementUsage(userId);
    return this.genAIService.generateResponse(finalPrompt);
  }

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

  private String getContext(
      String applicationJobTitle, String applicationJobDescription, String experienceDescription) {
    if (applicationJobTitle != null && applicationJobDescription != null) {

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

  private String prepareFinalPrompt(String instruction, String context, String userPrompt) {
    return """
           %s

           %s

           Use information provided above to address the request bellow:
           %s
           """
        .formatted(instruction, context, userPrompt);
  }
}
