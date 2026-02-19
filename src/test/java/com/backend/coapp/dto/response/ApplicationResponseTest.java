package com.backend.coapp.dto.response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ApplicationResponseTest {

  private final String userId = "user123";
  private final String companyId = "comp456";
  private final String jobTitle = "Software Engineer";
  private final ApplicationStatus status = ApplicationStatus.APPLIED;
  private final LocalDate deadline = LocalDate.of(2026, 1, 1);
  private final String description = "Detailed job description";
  private final Integer numPositions = 3;
  private final String source = "https://example.com";
  private final LocalDate appliedDate = LocalDate.now();
  private final String notes = "Some important notes";

  @Test
  public void constructorAndGetters_expectCorrectValues() {
    ApplicationResponse response =
        new ApplicationResponse(
            userId,
            companyId,
            jobTitle,
            status,
            deadline,
            description,
            numPositions,
            source,
            appliedDate,
            notes);

    assertEquals(userId, response.getUserId());
    assertEquals(companyId, response.getCompanyId());
    assertEquals(jobTitle, response.getJobTitle());
    assertEquals(status, response.getStatus());
    assertEquals(deadline, response.getApplicationDeadline());
    assertEquals(description, response.getJobDescription());
    assertEquals(numPositions, response.getNumPositions());
    assertEquals(source, response.getSourceLink());
    assertEquals(appliedDate, response.getDateApplied());
    assertEquals(notes, response.getNotes());
  }

  @Test
  public void fromModel_whenValidModel_expectCorrectMapping() {
    // Mock the model to isolate mapping logic
    ApplicationModel model = mock(ApplicationModel.class);
    when(model.getUserId()).thenReturn(userId);
    when(model.getCompanyId()).thenReturn(companyId);
    when(model.getJobTitle()).thenReturn(jobTitle);
    when(model.getStatus()).thenReturn(status);
    when(model.getApplicationDeadline()).thenReturn(deadline);
    when(model.getJobDescription()).thenReturn(description);
    when(model.getNumPositions()).thenReturn(numPositions);
    when(model.getSourceLink()).thenReturn(source);
    when(model.getDateApplied()).thenReturn(appliedDate);
    when(model.getNotes()).thenReturn(notes);

    ApplicationResponse response = ApplicationResponse.fromModel(model);

    assertNotNull(response);
    assertEquals(userId, response.getUserId());
    assertEquals(notes, response.getNotes());
    verify(model, times(1)).getUserId();
  }

  @Test
  public void toMap_expectKeysAndValuesMatch() {
    ApplicationResponse response =
        new ApplicationResponse(
            userId,
            companyId,
            jobTitle,
            status,
            deadline,
            description,
            numPositions,
            source,
            appliedDate,
            notes);

    Map<String, Object> map = response.toMap();

    assertNotNull(map);
    assertEquals(userId, map.get("userId"));
    assertEquals(companyId, map.get("companyId"));
    assertEquals(jobTitle, map.get("jobTitle"));
    assertEquals(status, map.get("status"));
    assertEquals(deadline, map.get("applicationDeadline"));
    assertEquals(description, map.get("jobDescription"));
    assertEquals(numPositions, map.get("numPositions"));
    assertEquals(source, map.get("sourceLink"));
    assertEquals(appliedDate, map.get("dateApplied"));

    // This test might fail based on your current implementation:
    // map.put("notes", this.jobDescription); <--- Bug in your code!
    assertEquals(notes, map.get("notes"), "The notes key in the map should match the notes field");
  }

  @Test
  public void toMap_withNullValues_expectKeysExistWithNulls() {
    ApplicationResponse response =
        new ApplicationResponse(
            userId, companyId, jobTitle, status, deadline, null, null, null, null, null);

    Map<String, Object> map = response.toMap();

    assertTrue(map.containsKey("jobDescription"));
    assertNull(map.get("jobDescription"));
    assertEquals(userId, map.get("userId"));
  }
}
