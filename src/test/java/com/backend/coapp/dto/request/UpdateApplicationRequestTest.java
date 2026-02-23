package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UpdateApplicationRequestTest {

  private UpdateApplicationRequest request;
  private final String validCompanyId = "comp-456";
  private final String validJobTitle = "Senior Software Engineer";

  @BeforeEach
  public void setUp() {
    this.request = new UpdateApplicationRequest();
    this.request.setCompanyId(validCompanyId);
    this.request.setJobTitle(validJobTitle);
  }

  @Test
  public void getMethods_expectInitValues() {
    request.setJobDescription("Updated description");
    request.setNotes("Updated notes");
    request.setSourceLink("https://updated-link.com");

    assertEquals(validCompanyId, request.getCompanyId());
    assertEquals(validJobTitle, request.getJobTitle());
    assertEquals("Updated description", request.getJobDescription());
    assertEquals("Updated notes", request.getNotes());
    assertEquals("https://updated-link.com", request.getSourceLink());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    assertDoesNotThrow(() -> request.validateRequest());
  }

  // --- Company ID Validation ---

  @Test
  public void validateRequest_whenCompanyIdIsNull_expectException() {
    request.setCompanyId(null);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> request.validateRequest());
    assertEquals("Company ID cannot be blank.", ex.getMessage());
  }

  @Test
  public void validateRequest_whenCompanyIdIsEmpty_expectException() {
    request.setCompanyId("");
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> request.validateRequest());
    assertEquals("Company ID cannot be blank.", ex.getMessage());
  }

  // --- Job Title Validation ---

  @Test
  public void validateRequest_whenJobTitleIsNull_expectException() {
    request.setJobTitle(null);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> request.validateRequest());
    assertEquals("Job Title cannot be blank.", ex.getMessage());
  }

  @Test
  public void validateRequest_whenJobTitleIsBlank_expectException() {
    request.setJobTitle(" ");
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> request.validateRequest());
    assertEquals("Job Title cannot be blank.", ex.getMessage());
  }

  // --- Optional Fields ---

  @Test
  public void validateRequest_whenOptionalFieldsAreNull_expectSuccess() {
    request.setJobDescription(null);
    request.setNotes(null);
    request.setSourceLink(null);

    assertDoesNotThrow(() -> request.validateRequest());
  }
}
