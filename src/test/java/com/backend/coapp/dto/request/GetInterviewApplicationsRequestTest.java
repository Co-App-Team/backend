package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class GetInterviewApplicationsRequestTest {

  private final LocalDate VALID_DATE = LocalDate.now();
  private final String EXCEPTION_PREFIX = "Invalid inputs of the request. ";

  @Test
  public void getMethods_expectInitValues() {
    GetInterviewApplicationsRequest request =
        GetInterviewApplicationsRequest.builder()
            .startDate(VALID_DATE)
            .endDate(VALID_DATE.plusDays(1))
            .build();

    assertEquals(VALID_DATE, request.getStartDate());
    assertEquals(VALID_DATE.plusDays(1), request.getEndDate());
  }

  @Test
  public void validateRequest_whenNoDatesProvided_expectNoException() {
    GetInterviewApplicationsRequest request = GetInterviewApplicationsRequest.builder().build();

    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenValidDateRangeProvided_expectNoException() {
    GetInterviewApplicationsRequest request =
        GetInterviewApplicationsRequest.builder()
            .startDate(VALID_DATE)
            .endDate(VALID_DATE.plusDays(5))
            .build();

    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenDatesAreEqual_expectNoException() {
    GetInterviewApplicationsRequest request =
        GetInterviewApplicationsRequest.builder().startDate(VALID_DATE).endDate(VALID_DATE).build();

    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenStartDateProvidedButEndIsNull_expectException() {
    GetInterviewApplicationsRequest request =
        GetInterviewApplicationsRequest.builder().startDate(VALID_DATE).endDate(null).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "If start date is provided, end date must be provided as well.",
        exception.getMessage());
  }

  @Test
  public void validateRequest_whenEndDateProvidedButStartIsNull_expectException() {
    GetInterviewApplicationsRequest request =
        GetInterviewApplicationsRequest.builder().startDate(null).endDate(VALID_DATE).build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(
        EXCEPTION_PREFIX + "If end date is provided, start date must be provided as well.",
        exception.getMessage());
  }

  @Test
  public void validateRequest_whenStartDateAfterEndDate_expectException() {
    GetInterviewApplicationsRequest request =
        GetInterviewApplicationsRequest.builder()
            .startDate(VALID_DATE.plusDays(1))
            .endDate(VALID_DATE)
            .build();

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);

    assertEquals(EXCEPTION_PREFIX + "Start date must be before end date.", exception.getMessage());
  }
}
