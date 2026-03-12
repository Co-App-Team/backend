package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class CreateApplicationRequestTest {

  private final String validUserId = "user-123";
  private final String validCompanyId = "comp-456";
  private final String validJobTitle = "Software Engineer";
  private final ApplicationStatus validStatus = ApplicationStatus.APPLIED;

  private final LocalDate validDateApplied = LocalDate.now();
  private final LocalDate validDeadline = LocalDate.now().plusMonths(1);

  private final String validUrl = "https://careers.google.com";

  private final String EXCEPTION_PREFIX = "Invalid inputs of the request. ";

  /** Helper method to create a valid builder. */
  private CreateApplicationRequest.CreateApplicationRequestBuilder getValidRequestBuilder() {
    return CreateApplicationRequest.builder()
        .companyId(validCompanyId)
        .jobTitle(validJobTitle)
        .status(validStatus)
        .applicationDeadline(validDeadline)
        .dateApplied(validDateApplied);
  }

  @Test
  public void getMethods_expectInitValues() {
    CreateApplicationRequest request =
        getValidRequestBuilder()
            .jobDescription("Description")
            .numPositions(1)
            .sourceLink(validUrl)
            .notes("Some notes")
            .build();

    assertEquals(validCompanyId, request.getCompanyId());
    assertEquals(validJobTitle, request.getJobTitle());
    assertEquals(validStatus, request.getStatus());
    assertEquals(validDeadline, request.getApplicationDeadline());
    assertEquals(validUrl, request.getSourceLink());
    assertEquals("Description", request.getJobDescription());
    assertEquals(1, request.getNumPositions());
    assertEquals("Some notes", request.getNotes());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    CreateApplicationRequest request = getValidRequestBuilder().sourceLink(validUrl).build();

    assertDoesNotThrow(request::validateRequest);
  }

  // Company ID Validation
  @Test
  public void validateRequest_whenCompanyIdIsNull_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().companyId(null).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Company id cannot be null or empty.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenCompanyIdIsBlank_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().companyId(" ").build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Company id cannot be null or empty.", exception.getMessage());
  }

  // Job Title Validation
  @Test
  public void validateRequest_whenJobTitleIsNull_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().jobTitle(null).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Job title cannot be null or empty.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenJobTitleIsBlank_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().jobTitle(" ").build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Job title cannot be null or empty.", exception.getMessage());
  }

  // Status Validation
  @Test
  public void validateRequest_whenStatusIsNull_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().status(null).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Status cannot be null.", exception.getMessage());
  }

  // Deadline Validation
  @Test
  public void validateRequest_whenDeadlineIsNull_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().applicationDeadline(null).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Application deadline cannot be null.", exception.getMessage());
  }

  // Source Link Validation
  @Test
  public void validateRequest_whenSourceLinkIsInvalid_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().sourceLink("not-a-url").build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Source link is not a valid URL.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenSourceLinkIsBlank_expectSuccess() {
    CreateApplicationRequest request = getValidRequestBuilder().sourceLink("   ").build();

    assertDoesNotThrow(request::validateRequest);
  }

  // Job Description Length Validation
  @Test
  public void validateRequest_whenJobDescriptionExceedsMaxLength_expectException() {
    String longDescription = "a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH + 1);
    CreateApplicationRequest request =
        getValidRequestBuilder().jobDescription(longDescription).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX
            + "Job description cannot exceed %s characters."
                .formatted(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH),
        exception.getMessage());
  }

  @Test
  public void validateRequest_whenJobDescriptionExactlyMaxLength_expectSuccess() {
    String maxDescription = "a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH);
    CreateApplicationRequest request =
        getValidRequestBuilder().jobDescription(maxDescription).build();

    assertDoesNotThrow(request::validateRequest);
  }

  // Notes Length Validation
  @Test
  public void validateRequest_whenNotesExceedsMaxLength_expectException() {
    String longNotes = "a".repeat(ApplicationConstants.MAX_JOB_NOTES_LENGTH + 1);
    CreateApplicationRequest request = getValidRequestBuilder().notes(longNotes).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX
            + "Notes cannot exceed %s characters."
                .formatted(ApplicationConstants.MAX_JOB_NOTES_LENGTH),
        exception.getMessage());
  }

  @Test
  public void validateRequest_whenNotesExactlyMaxLength_expectSuccess() {
    String maxNotes = "a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH);
    CreateApplicationRequest request = getValidRequestBuilder().notes(maxNotes).build();

    assertDoesNotThrow(request::validateRequest);
  }

  // Job tile length validation
  @Test
  public void validateRequest_whenJobTitleExceedsLimit_expectException() {
    String longTitle = "a".repeat(ApplicationConstants.MAX_JOB_TITLE_LENGTH + 1);
    CreateApplicationRequest request = getValidRequestBuilder().jobTitle(longTitle).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX
            + "Job title cannot exceed %s characters."
                .formatted(ApplicationConstants.MAX_JOB_TITLE_LENGTH),
        exception.getMessage());
  }

  // Num Positions Validation
  @Test
  public void validateRequest_whenNumPositionsNegative_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().numPositions(-1).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "Number of positions cannot be negative.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenNumPositionsZero_expectSuccess() {
    CreateApplicationRequest request = getValidRequestBuilder().numPositions(0).build();
    assertDoesNotThrow(request::validateRequest);
  }

  // Date Applied vs Deadline Validation
  @Test
  public void validateRequest_whenDateAppliedAfterDeadline_expectException() {
    LocalDate deadline = LocalDate.now().minusDays(1);
    LocalDate applied = LocalDate.now();

    CreateApplicationRequest request =
        getValidRequestBuilder().applicationDeadline(deadline).dateApplied(applied).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "Date applied cannot be after application deadline.",
        exception.getMessage());
  }

  @Test
  public void validateRequest_whenDateAppliedBeforeDeadline_expectSuccess() {
    LocalDate applied = LocalDate.now().minusDays(3);
    LocalDate deadline = LocalDate.now();

    CreateApplicationRequest request =
        getValidRequestBuilder().applicationDeadline(deadline).dateApplied(applied).build();

    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenDateAppliedIsNull_expectSuccess() {
    CreateApplicationRequest request = getValidRequestBuilder().dateApplied(null).build();

    assertDoesNotThrow(request::validateRequest);
  }
}
