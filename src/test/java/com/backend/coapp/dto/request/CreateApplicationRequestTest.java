package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class CreateApplicationRequestTest {

  private final String validUserId = "user-123";
  private final String validCompanyId = "comp-456";
  private final String validJobTitle = "Software Engineer";
  private final ApplicationStatus validStatus = ApplicationStatus.APPLIED;
  private final LocalDate validDeadline = LocalDate.of(2025, 12, 31);
  private final LocalDate validDateApplied = LocalDate.now();
  private final String validUrl = "https://careers.google.com";

  private final String EXCEPTION_PREFIX = "Invalid inputs of the request. ";

  /** Helper method to create a valid builder. */
  private CreateApplicationRequest.CreateApplicationRequestBuilder getValidRequestBuilder() {
    return CreateApplicationRequest.builder()
        .userId(validUserId)
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

    assertEquals(validUserId, request.getUserId());
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

  // User ID Validation
  @Test
  public void validateRequest_whenUserIdIsNull_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().userId(null).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "User id cannot be null or empty.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenUserIdIsBlank_expectException() {
    CreateApplicationRequest request = getValidRequestBuilder().userId("   ").build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "User id cannot be null or empty.", exception.getMessage());
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
}
