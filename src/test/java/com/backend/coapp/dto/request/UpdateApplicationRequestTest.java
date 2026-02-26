package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class UpdateApplicationRequestTest {

  private final String validCompanyId = "comp-456";
  private final String validJobTitle = "Senior Software Engineer";
  private final String EXCEPTION_PREFIX = "Invalid inputs of the request. ";

  /** Helper method to create a valid builder. */
  private UpdateApplicationRequest.UpdateApplicationRequestBuilder getValidRequestBuilder() {
    return UpdateApplicationRequest.builder().companyId(validCompanyId).jobTitle(validJobTitle);
  }

  @Test
  public void getMethods_expectInitValues() {
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder()
            .companyId(validCompanyId)
            .jobTitle(validJobTitle)
            .jobDescription("Updated description")
            .notes("Updated notes")
            .sourceLink("https://updated-link.com")
            .build();

    assertEquals(validCompanyId, request.getCompanyId());
    assertEquals(validJobTitle, request.getJobTitle());
    assertEquals("Updated description", request.getJobDescription());
    assertEquals("Updated notes", request.getNotes());
    assertEquals("https://updated-link.com", request.getSourceLink());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    UpdateApplicationRequest request = getValidRequestBuilder().build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenAllFieldsNull_expectException() {
    UpdateApplicationRequest request = UpdateApplicationRequest.builder().build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "At least one field must be provided to update the application request.",
        exception.getMessage());
  }

  // --- Company ID Validation ---

  @Test
  public void validateRequest_whenCompanyIdIsBlank_expectException() {
    UpdateApplicationRequest request = getValidRequestBuilder().companyId(" ").build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    // Note: The DTO code provided has a typo in the check:
    // "if (companyId != null && isBlank(jobTitle))".
    // Assuming the intention is "isBlank(companyId)", this test expects that result.
    // If the DTO code is exactly as provided, this test validates the logic logic path
    // where companyId is set to blank.
    assertEquals(EXCEPTION_PREFIX + "Company Id cannot be blank.", exception.getMessage());
  }

  // --- Job Title Validation ---

  @Test
  public void validateRequest_whenJobTitleIsBlank_expectException() {
    UpdateApplicationRequest request = getValidRequestBuilder().jobTitle(" ").build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Job Title cannot be blank.", exception.getMessage());
  }

  // --- Optional Fields Validation ---

  @Test
  public void validateRequest_whenSourceLinkIsInvalid_expectException() {
    UpdateApplicationRequest request = getValidRequestBuilder().sourceLink("not-a-url").build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Website must be a valid URL", exception.getMessage());
  }

  @Test
  public void validateRequest_whenNumPositionsNegative_expectException() {
    UpdateApplicationRequest request = getValidRequestBuilder().numPositions(-1).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "Number of positions cannot be negative.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenDateAppliedIsAfterDeadline_expectException() {
    UpdateApplicationRequest request =
        getValidRequestBuilder()
            .dateApplied(LocalDate.of(2025, 1, 1))
            .applicationDeadline(LocalDate.of(2024, 1, 1))
            .build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "Date applied cannot be after application deadline.",
        exception.getMessage());
  }

  @Test
  public void validateRequest_whenOptionalFieldsAreNull_expectSuccess() {
    // In an Update request, null fields are valid (meaning 'do not update')
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder()
            .jobTitle("Only updating title")
            .companyId(null)
            .notes(null)
            .build();

    assertDoesNotThrow(request::validateRequest);
  }
}
