package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class CreateApplicationRequestTest {

  private final String validCompanyId = "comp-456";
  private final String validJobTitle = "Software Engineer";
  private final ApplicationStatus validStatus = ApplicationStatus.APPLIED;
  private final LocalDate validDeadline = LocalDate.of(2025, 12, 31);
  private final String validUrl = "https://careers.google.com";

  private final String EXCEPTION_PREFIX = "Invalid inputs of the request. ";

  @Test
  public void getMethods_expectInitValues() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            validCompanyId,
            validJobTitle,
            validStatus,
            validDeadline,
            "Description",
            1,
            validUrl,
            LocalDate.now(),
            "Some notes");

    assertEquals(validCompanyId, request.getCompanyId());
    assertEquals(validJobTitle, request.getJobTitle());
    assertEquals(validStatus, request.getStatus());
    assertEquals(validDeadline, request.getApplicationDeadline());
    assertEquals(validUrl, request.getSourceLink());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            validCompanyId,
            validJobTitle,
            validStatus,
            validDeadline,
            null,
            null,
            validUrl,
            null,
            null);

    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenCompanyIdIsNull_expectException() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            null, validJobTitle, validStatus, validDeadline, null, null, null, null, null);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Company id cannot be null or empty.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenCompanyIdIsBlank_expectException() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            " ", validJobTitle, validStatus, validDeadline, null, null, null, null, null);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Company id cannot be null or empty.", exception.getMessage());
  }

  // --- Job Title Validation ---

  @Test
  public void validateRequest_whenJobTitleIsNull_expectException() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            validCompanyId, null, validStatus, validDeadline, null, null, null, null, null);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Job title cannot be null or empty.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenJobTitleIsBlank_expectException() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            validCompanyId, " ", validStatus, validDeadline, null, null, null, null, null);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Job title cannot be null or empty.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenStatusIsNull_expectException() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            validCompanyId, validJobTitle, null, validDeadline, null, null, null, null, null);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Status cannot be null.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenDeadlineIsNull_expectException() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            validCompanyId, validJobTitle, validStatus, null, null, null, null, null, null);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Application deadline cannot be null.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenSourceLinkIsInvalid_expectException() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            validCompanyId,
            validJobTitle,
            validStatus,
            validDeadline,
            null,
            null,
            "not-a-url",
            null,
            null);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Source link is not a valid URL.", exception.getMessage());
  }

  @Test
  public void validateRequest_whenSourceLinkIsBlank_expectSuccess() {
    CreateApplicationRequest request =
        new CreateApplicationRequest(
            validCompanyId,
            validJobTitle,
            validStatus,
            validDeadline,
            null,
            null,
            "   ",
            null,
            null);

    assertDoesNotThrow(request::validateRequest);
  }
}
