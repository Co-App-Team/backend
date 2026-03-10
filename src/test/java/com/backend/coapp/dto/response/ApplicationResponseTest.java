package com.backend.coapp.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ApplicationResponseTest {

  private final String applicationId = "app123";
  private final String companyId = "comp456";
  private final String jobTitle = "Software Engineer";
  private final ApplicationStatus status = ApplicationStatus.APPLIED;
  private final LocalDate deadline = LocalDate.of(2026, 1, 1);
  private final String description = "Detailed job description";
  private final Integer numPositions = 3;
  private final String source = "https://example.com";
  private final LocalDate appliedDate = LocalDate.now();
  private final String notes = "Some important notes";
  private final LocalDate interviewDate = LocalDate.now().plusDays(1);

  /** Helper methods. */
  private ApplicationResponse.ApplicationResponseBuilder getValidResponseBuilder() {
    return ApplicationResponse.builder()
            .applicationId(applicationId)
            .companyId(companyId)
            .jobTitle(jobTitle)
            .status(status)
            .applicationDeadline(deadline)
            .jobDescription(description)
            .numPositions(numPositions)
            .sourceLink(source)
            .dateApplied(appliedDate)
            .notes(notes)
            .interviewDate(interviewDate);
  }

  private ApplicationModel.ApplicationModelBuilder getValidModelBuilder() {
    return ApplicationModel.builder()
            .id(applicationId)
            .companyId(companyId)
            .jobTitle(jobTitle)
            .status(status)
            .applicationDeadline(deadline)
            .jobDescription(description)
            .numPositions(numPositions)
            .sourceLink(source)
            .dateApplied(appliedDate)
            .notes(notes)
            .interviewDate(interviewDate);
  }

  @Test
  public void constructorAndGetters_expectCorrectValues() {
    ApplicationResponse response = getValidResponseBuilder().build();

    assertEquals(applicationId, response.getApplicationId());
    assertEquals(companyId, response.getCompanyId());
    assertEquals(jobTitle, response.getJobTitle());
    assertEquals(status, response.getStatus());
    assertEquals(deadline, response.getApplicationDeadline());
    assertEquals(description, response.getJobDescription());
    assertEquals(numPositions, response.getNumPositions());
    assertEquals(source, response.getSourceLink());
    assertEquals(appliedDate, response.getDateApplied());
    assertEquals(notes, response.getNotes());
    assertEquals(interviewDate, response.getInterviewDate());
  }

  @Test
  public void fromModel_whenValidModel_expectCorrectMapping() {
    ApplicationModel model = getValidModelBuilder().build();

    ApplicationResponse response = ApplicationResponse.fromModel(model);

    assertNotNull(response);
    assertEquals(applicationId, response.getApplicationId());
    assertEquals(companyId, response.getCompanyId());
    assertEquals(jobTitle, response.getJobTitle());
    assertEquals(notes, response.getNotes());
    assertEquals(status, response.getStatus());
    assertEquals(interviewDate, response.getInterviewDate());
  }

  @Test
  public void toMap_expectKeysAndValuesMatch() {
    ApplicationResponse response = getValidResponseBuilder().build();

    Map<String, Object> map = response.toMap();

    assertNotNull(map);
    assertEquals(11, map.size());
    assertEquals(applicationId, map.get("applicationId"));
    assertEquals(companyId, map.get("companyId"));
    assertEquals(jobTitle, map.get("jobTitle"));
    assertEquals(status, map.get("status"));
    assertEquals(deadline, map.get("applicationDeadline"));
    assertEquals(description, map.get("jobDescription"));
    assertEquals(numPositions, map.get("numPositions"));
    assertEquals(source, map.get("sourceLink"));
    assertEquals(appliedDate, map.get("dateApplied"));
    assertEquals(notes, map.get("notes"));
    assertEquals(interviewDate, map.get("interviewDate"));
  }

  @Test
  public void toMap_withNullValues_expectKeysExistWithNulls() {
    ApplicationResponse response =
            getValidResponseBuilder()
                    .jobDescription(null)
                    .numPositions(null)
                    .sourceLink(null)
                    .dateApplied(null)
                    .notes(null)
                    .interviewDate(null)
                    .build();

    Map<String, Object> map = response.toMap();

    assertTrue(map.containsKey("jobDescription"));
    assertNull(map.get("jobDescription"));
    assertTrue(map.containsKey("notes"));
    assertNull(map.get("notes"));
    assertTrue(map.containsKey("interviewDate"));
    assertNull(map.get("interviewDate"));
    assertEquals(applicationId, map.get("applicationId"));
  }
}
