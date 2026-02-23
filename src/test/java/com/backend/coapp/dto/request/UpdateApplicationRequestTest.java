package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UpdateApplicationRequestTest {

  private UpdateApplicationRequest request;
  private final String validCompanyId = "comp-456";
  private final String validJobTitle = "Senior Software Engineer";
  private final String EXCEPTION_PREFIX = "Invalid inputs of the request. ";

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
    InvalidRequestException ex =
        assertThrows(InvalidRequestException.class, () -> request.validateRequest());
    assertEquals(EXCEPTION_PREFIX + "Company ID cannot be blank.", ex.getMessage());
  }

  @Test
  public void validateRequest_whenCompanyIdIsEmpty_expectException() {
    request.setCompanyId("");
    InvalidRequestException ex =
        assertThrows(InvalidRequestException.class, () -> request.validateRequest());
    assertEquals(EXCEPTION_PREFIX + "Company ID cannot be blank.", ex.getMessage());
  }

  // --- Job Title Validation ---

  @Test
  public void validateRequest_whenJobTitleIsNull_expectException() {
    request.setJobTitle(null);
    InvalidRequestException ex =
        assertThrows(InvalidRequestException.class, () -> request.validateRequest());
    assertEquals(EXCEPTION_PREFIX + "Job Title cannot be blank.", ex.getMessage());
  }

  @Test
  public void validateRequest_whenJobTitleIsBlank_expectException() {
    request.setJobTitle(" ");
    InvalidRequestException ex =
        assertThrows(InvalidRequestException.class, () -> request.validateRequest());
    assertEquals(EXCEPTION_PREFIX + "Job Title cannot be blank.", ex.getMessage());
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
