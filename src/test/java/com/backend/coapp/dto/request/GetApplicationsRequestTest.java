package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import java.util.List;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.6 and revised by Eric Hodgson */
class GetApplicationsRequestTest {

  @Test
  void builder_expectInitValues() {
    GetApplicationsRequest request =
        GetApplicationsRequest.builder()
            .search("niche")
            .status("APPLIED")
            .sortBy("dateApplied")
            .sortOrder("asc")
            .page(0)
            .size(20)
            .build();

    assertEquals("niche", request.getSearch());
    assertEquals("APPLIED", request.getStatus());
    assertEquals("dateApplied", request.getSortBy());
    assertEquals("asc", request.getSortOrder());
    assertEquals(0, request.getPage());
    assertEquals(20, request.getSize());
  }

  @Test
  void validateRequest_whenAllNull_expectDefaults() {
    GetApplicationsRequest request = new GetApplicationsRequest();

    request.validateRequest();

    assertNull(request.getParsedStatuses());
    assertEquals(ApplicationConstants.SORT_BY_DATE_APPLIED, request.getSortBy());
    assertEquals(ApplicationConstants.DEFAULT_SORT_ORDER, request.getSortOrder());
    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_PAGE, request.getPage());
    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_SIZE, request.getSize());
  }

  @Test
  void validateRequest_whenAllValid_expectNoChange() {
    GetApplicationsRequest request =
        GetApplicationsRequest.builder()
            .status("APPLIED")
            .sortBy("dateApplied")
            .sortOrder("asc")
            .page(1)
            .size(10)
            .build();

    request.validateRequest();

    assertEquals(List.of(ApplicationStatus.APPLIED), request.getParsedStatuses());
    assertEquals("dateApplied", request.getSortBy());
    assertEquals("asc", request.getSortOrder());
    assertEquals(1, request.getPage());
    assertEquals(10, request.getSize());
  }

  @Test
  void validateRequest_whenStatusNull_expectParsedStatusesNull() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus(null);

    request.validateRequest();

    assertNull(request.getParsedStatuses());
  }

  @Test
  void validateRequest_whenStatusBlank_expectParsedStatusesNull() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("   ");

    request.validateRequest();

    assertNull(request.getParsedStatuses());
  }

  @Test
  void validateRequest_whenStatusSingleValid_expectParsedStatuses() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("INTERVIEWING");

    request.validateRequest();

    assertEquals(List.of(ApplicationStatus.INTERVIEWING), request.getParsedStatuses());
  }

  @Test
  void validateRequest_whenStatusMultipleValid_expectAllParsed() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("APPLIED,REJECTED,OFFER_RECEIVED");

    request.validateRequest();

    assertEquals(
        List.of(
            ApplicationStatus.APPLIED,
            ApplicationStatus.REJECTED,
            ApplicationStatus.OFFER_RECEIVED),
        request.getParsedStatuses());
  }

  @Test
  void validateRequest_whenStatusMultipleWithSpaces_expectTrimmedAndParsed() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("APPLIED , REJECTED");

    request.validateRequest();

    assertEquals(
        List.of(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED),
        request.getParsedStatuses());
  }

  @Test
  void validateRequest_whenStatusInvalid_expectException() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("INVALID_STATUS");

    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenSortByNull_expectDefaultSortBy() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortBy(null);

    request.validateRequest();

    assertEquals(ApplicationConstants.SORT_BY_DATE_APPLIED, request.getSortBy());
  }

  @Test
  void validateRequest_whenSortByBlank_expectDefaultSortBy() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortBy("   ");

    request.validateRequest();

    assertEquals(ApplicationConstants.SORT_BY_DATE_APPLIED, request.getSortBy());
  }

  @Test
  void validateRequest_whenSortByValid_expectUnchanged() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortBy("dateApplied");

    request.validateRequest();

    assertEquals("dateApplied", request.getSortBy());
  }

  @Test
  void validateRequest_whenSortByInvalid_expectException() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortBy("invalidField");

    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenSortOrderNull_expectDefaultSortOrder() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder(null);

    request.validateRequest();

    assertEquals(ApplicationConstants.DEFAULT_SORT_ORDER, request.getSortOrder());
  }

  @Test
  void validateRequest_whenSortOrderBlank_expectDefaultSortOrder() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder("   ");

    request.validateRequest();

    assertEquals(ApplicationConstants.DEFAULT_SORT_ORDER, request.getSortOrder());
  }

  @Test
  void validateRequest_whenSortOrderDesc_expectUnchanged() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder("desc");

    request.validateRequest();

    assertEquals("desc", request.getSortOrder());
  }

  @Test
  void validateRequest_whenSortOrderUpperCase_expectNormalisedToLower() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder("ASC");

    request.validateRequest();

    assertEquals("asc", request.getSortOrder());
  }

  @Test
  void validateRequest_whenSortOrderInvalid_expectException() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder("sideways");

    assertThrows(InvalidRequestException.class, request::validateRequest);
  }
}
