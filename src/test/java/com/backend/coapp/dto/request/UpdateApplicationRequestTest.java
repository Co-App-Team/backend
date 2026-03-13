package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UpdateApplicationRequestTest {

  private final String validCompanyId = "comp-456";
  private final String validJobTitle = "Senior Software Engineer";
  private final String EXCEPTION_PREFIX = "Invalid inputs of the request. ";

  private final LocalDate deadline = LocalDate.now().plusMonths(1);
  private final LocalDate applied = LocalDate.now().plusMonths(1);

  /** Helper methods to create a valid builder. */
  private UpdateApplicationRequest.UpdateApplicationRequestBuilder getValidRequestBuilder() {
    return UpdateApplicationRequest.builder().companyId(validCompanyId).jobTitle(validJobTitle);
  }

  private UpdateApplicationRequest.UpdateApplicationRequestBuilder getFullValidRequestBuilder() {

    return UpdateApplicationRequest.builder()
        .companyId(validCompanyId)
        .jobTitle(validJobTitle)
        .companyId(validCompanyId)
        .jobTitle(validJobTitle)
        .status(ApplicationStatus.APPLIED)
        .applicationDeadline(deadline)
        .dateApplied(applied)
        .jobDescription("Updated description")
        .numPositions(3)
        .sourceLink("https://updated-link.com")
        .notes("Updated notes")
        .interviewDate(deadline);
  }

  @Test
  void getMethods_andLombokConstructors_expectInitValues() {

    UpdateApplicationRequest request = getFullValidRequestBuilder().build();

    UpdateApplicationRequest emptyRequest = new UpdateApplicationRequest();

    assertNull(emptyRequest.getCompanyId());

    assertEquals(validCompanyId, request.getCompanyId());
    assertEquals(validJobTitle, request.getJobTitle());
    assertEquals(ApplicationStatus.APPLIED, request.getStatus());
    assertEquals(deadline, request.getApplicationDeadline());
    assertEquals(applied, request.getDateApplied());
    assertEquals("Updated description", request.getJobDescription());
    assertEquals(3, request.getNumPositions());
    assertEquals("https://updated-link.com", request.getSourceLink());
    assertEquals("Updated notes", request.getNotes());
    assertEquals(deadline, request.getInterviewDate());
  }

  @Test
  void validateRequest_whenValidBasicRequest_expectNoException() {
    UpdateApplicationRequest request = getValidRequestBuilder().build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenAllFieldsPopulatedAndValid_expectNoException() {
    UpdateApplicationRequest request = getFullValidRequestBuilder().build();

    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenAllFieldsNull_expectException() {
    UpdateApplicationRequest request = UpdateApplicationRequest.builder().build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "At least one field must be provided to update the application request.",
        exception.getMessage());
  }

  @Test
  void validateRequest_whenExactlyOneFieldIsProvided_expectSuccess() {
    assertDoesNotThrow(
        () -> UpdateApplicationRequest.builder().companyId("C").build().validateRequest());
    assertDoesNotThrow(
        () -> UpdateApplicationRequest.builder().jobTitle("J").build().validateRequest());
    assertDoesNotThrow(
        () ->
            UpdateApplicationRequest.builder()
                .status(ApplicationStatus.APPLIED)
                .build()
                .validateRequest());
    assertDoesNotThrow(
        () ->
            UpdateApplicationRequest.builder()
                .applicationDeadline(LocalDate.now())
                .build()
                .validateRequest());
    assertDoesNotThrow(
        () -> UpdateApplicationRequest.builder().jobDescription("Desc").build().validateRequest());
    assertDoesNotThrow(
        () -> UpdateApplicationRequest.builder().numPositions(1).build().validateRequest());
    assertDoesNotThrow(
        () ->
            UpdateApplicationRequest.builder()
                .sourceLink("http://valid.com")
                .build()
                .validateRequest());
    assertDoesNotThrow(
        () ->
            UpdateApplicationRequest.builder()
                .dateApplied(LocalDate.now())
                .build()
                .validateRequest());
    assertDoesNotThrow(
        () -> UpdateApplicationRequest.builder().notes("Notes").build().validateRequest());
  }

  @Test
  void validateRequest_whenCompanyIdIsNull_shortCircuit_companyIdBlank() {
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder().jobTitle(validJobTitle).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenCompanyIdIsBlank_expectException() {
    UpdateApplicationRequest request = getValidRequestBuilder().companyId("   ").build();
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertEquals(EXCEPTION_PREFIX + "Company Id cannot be blank.", exception.getMessage());
  }

  @Test
  void validateRequest_whenJobTitleIsEmpty_expectException() {
    UpdateApplicationRequest request = getValidRequestBuilder().jobTitle("").build();
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertEquals(EXCEPTION_PREFIX + "Job Title cannot be blank.", exception.getMessage());
  }

  @Test
  void validateRequest_whenJobDescriptionExceedsLimit_expectException() {
    String longDescription = "a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH + 1);
    UpdateApplicationRequest request =
        getValidRequestBuilder().jobDescription(longDescription).build();
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertEquals(
        EXCEPTION_PREFIX
            + "Description cannot exceed %s characters"
                .formatted(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH),
        exception.getMessage());
  }

  @Test
  void validateRequest_whenNotesExceedsLimit_expectException() {
    String longNotes = "a".repeat(ApplicationConstants.MAX_JOB_NOTES_LENGTH + 1);
    UpdateApplicationRequest request = getValidRequestBuilder().notes(longNotes).build();
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertEquals(
        EXCEPTION_PREFIX
            + "Notes cannot exceed %s characters"
                .formatted(ApplicationConstants.MAX_JOB_NOTES_LENGTH),
        exception.getMessage());
  }

  @Test
  void validateRequest_whenJobTitleExceedsLimit_expectException() {
    String longTitle = "a".repeat(ApplicationConstants.MAX_JOB_TITLE_LENGTH + 1);
    UpdateApplicationRequest request = getValidRequestBuilder().jobTitle(longTitle).build();
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertEquals(
        EXCEPTION_PREFIX
            + "Job title cannot exceed %s characters"
                .formatted(ApplicationConstants.MAX_JOB_TITLE_LENGTH),
        exception.getMessage());
  }

  @Test
  void validateRequest_whenSourceLinkIsInvalid_expectException() {
    UpdateApplicationRequest request = getValidRequestBuilder().sourceLink("not-a-url").build();
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertEquals(EXCEPTION_PREFIX + "Website must be a valid URL", exception.getMessage());
  }

  @Test
  void validateRequest_whenSourceLinkIsNull_expectSuccess() {
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder().jobTitle("Manager").sourceLink(null).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenSourceLinkIsEmpty_expectSuccess() {
    UpdateApplicationRequest request = getValidRequestBuilder().sourceLink("").build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenSourceLinkIsWhitespace_expectSuccess() {
    UpdateApplicationRequest request = getValidRequestBuilder().sourceLink("   ").build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenNumPositionsNegative_expectException() {
    UpdateApplicationRequest request = getValidRequestBuilder().numPositions(-1).build();
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertEquals(
        EXCEPTION_PREFIX + "Number of positions cannot be negative.", exception.getMessage());
  }

  @Test
  void validateRequest_whenDateAppliedAfterDeadline_expectException() {
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
  void validateRequest_whenDateAppliedBeforeDeadline_expectNoException() {
    UpdateApplicationRequest request =
        getValidRequestBuilder()
            .dateApplied(LocalDate.of(2024, 1, 1))
            .applicationDeadline(LocalDate.of(2025, 1, 1))
            .build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenDateAppliedNull_expectNoException() {
    UpdateApplicationRequest request =
        getValidRequestBuilder().applicationDeadline(LocalDate.of(2025, 1, 1)).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenDeadlineNull_expectNoException() {
    UpdateApplicationRequest request =
        getValidRequestBuilder().dateApplied(LocalDate.of(2024, 1, 1)).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenJobDescriptionIsNull_expectNoException() {
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder().jobTitle(validJobTitle).jobDescription(null).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenNotesIsNull_expectNoException() {
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder().jobTitle(validJobTitle).notes(null).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenNumPositionsIsNull_expectNoException() {
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder().jobTitle(validJobTitle).numPositions(null).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenNumPositionsIsZero_expectNoException() {
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder().jobTitle(validJobTitle).numPositions(0).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenDateAppliedEqualsDeadline_expectNoException() {
    LocalDate sameDate = LocalDate.of(2025, 6, 15);
    UpdateApplicationRequest request =
        getValidRequestBuilder().dateApplied(sameDate).applicationDeadline(sameDate).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenJobDescriptionExactlyAtLimit_expectNoException() {
    String exactDescription = "a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH);
    UpdateApplicationRequest request =
        getValidRequestBuilder().jobDescription(exactDescription).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenNotesExactlyAtLimit_expectNoException() {
    String exactNotes = "a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH);
    UpdateApplicationRequest request = getValidRequestBuilder().notes(exactNotes).build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenSourceLinkIsValidWithWhitespace_expectNoException() {
    UpdateApplicationRequest request =
        getValidRequestBuilder().sourceLink("  https://valid-url.com  ").build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenCompanyIdIsNotBlank_expectNoException() {
    UpdateApplicationRequest request =
        UpdateApplicationRequest.builder().companyId("valid-id").build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenInterviewDateInThePast_expectException() {
    UpdateApplicationRequest request =
        getValidRequestBuilder().interviewDate(LocalDate.now().minusDays(1)).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "Interview Date cannot be in the past.", exception.getMessage());
  }

  @Test
  void validateRequest_whenInterviewDateIsToday_expectSuccess() {
    UpdateApplicationRequest request =
        getValidRequestBuilder().interviewDate(LocalDate.now()).build();

    assertDoesNotThrow(request::validateRequest);
  }
}
