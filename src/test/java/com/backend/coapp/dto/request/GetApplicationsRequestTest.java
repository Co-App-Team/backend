package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidQueryParameterException;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import com.backend.coapp.util.ApplicationValidSearchParameters;
import java.util.List;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.6 and revised by Eric Hodgson */
public class GetApplicationsRequestTest {

  @Test
  public void getMethods_expectInitValues() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSearch("niche");
    request.setStatus("APPLIED");
    request.setSortBy("dateApplied");
    request.setSortOrder("asc");

    assertEquals("niche", request.getSearch());
    assertEquals("APPLIED", request.getStatus());
    assertEquals("dateApplied", request.getSortBy());
    assertEquals("asc", request.getSortOrder());
  }

  @Test
  public void validateAndParse_whenAllNull_expectDefaults() {
    GetApplicationsRequest request = new GetApplicationsRequest();

    request.validateAndParse();

    assertNull(request.getParsedStatuses());
    assertEquals(ApplicationConstants.SORT_BY_DATE_APPLIED, request.getSortBy());
    assertEquals(ApplicationConstants.DEFAULT_SORT_ORDER, request.getSortOrder());
    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_PAGE, request.getPage());
    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateAndParse_whenAllValid_expectNoChange() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("APPLIED");
    request.setSortBy("dateApplied");
    request.setSortOrder("asc");
    request.setPage(1);
    request.setSize(10);

    request.validateAndParse();

    assertEquals(List.of(ApplicationStatus.APPLIED), request.getParsedStatuses());
    assertEquals("dateApplied", request.getSortBy());
    assertEquals("asc", request.getSortOrder());
    assertEquals(1, request.getPage());
    assertEquals(10, request.getSize());
  }

  @Test
  public void validateAndParse_whenStatusNull_expectParsedStatusesNull() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus(null);

    request.validateAndParse();

    assertNull(request.getParsedStatuses());
  }

  @Test
  public void validateAndParse_whenStatusBlank_expectParsedStatusesNull() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("   ");

    request.validateAndParse();

    assertNull(request.getParsedStatuses());
  }

  @Test
  public void validateAndParse_whenStatusSingleValid_expectParsedStatuses() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("INTERVIEWING");

    request.validateAndParse();

    assertEquals(List.of(ApplicationStatus.INTERVIEWING), request.getParsedStatuses());
  }

  @Test
  public void validateAndParse_whenStatusMultipleValid_expectAllParsed() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("APPLIED,REJECTED,OFFER_RECEIVED");

    request.validateAndParse();

    assertEquals(
        List.of(
            ApplicationStatus.APPLIED,
            ApplicationStatus.REJECTED,
            ApplicationStatus.OFFER_RECEIVED),
        request.getParsedStatuses());
  }

  @Test
  public void validateAndParse_whenStatusMultipleWithSpaces_expectTrimmedAndParsed() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("APPLIED , REJECTED");

    request.validateAndParse();

    assertEquals(
        List.of(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED),
        request.getParsedStatuses());
  }

  @Test
  public void validateAndParse_whenStatusInvalid_expectException() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setStatus("INVALID_STATUS");

    InvalidQueryParameterException ex =
        assertThrows(InvalidQueryParameterException.class, request::validateAndParse);

    assertEquals("status", ex.getParameter());
    assertEquals("INVALID_STATUS", ex.getInvalidValue());
    assertEquals(ApplicationValidSearchParameters.VALID_STATUS_VALUES, ex.getValidValues());
  }

  @Test
  public void validateAndParse_whenSortByNull_expectDefaultSortBy() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortBy(null);

    request.validateAndParse();

    assertEquals(ApplicationConstants.SORT_BY_DATE_APPLIED, request.getSortBy());
  }

  @Test
  public void validateAndParse_whenSortByBlank_expectDefaultSortBy() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortBy("   ");

    request.validateAndParse();

    assertEquals(ApplicationConstants.SORT_BY_DATE_APPLIED, request.getSortBy());
  }

  @Test
  public void validateAndParse_whenSortByValid_expectUnchanged() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortBy("dateApplied");

    request.validateAndParse();

    assertEquals("dateApplied", request.getSortBy());
  }

  @Test
  public void validateAndParse_whenSortByInvalid_expectException() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortBy("invalidField");

    InvalidQueryParameterException ex =
        assertThrows(InvalidQueryParameterException.class, request::validateAndParse);

    assertEquals("sortBy", ex.getParameter());
    assertEquals("invalidField", ex.getInvalidValue());
    assertEquals(ApplicationValidSearchParameters.VALID_SORT_BY_VALUES, ex.getValidValues());
  }

  @Test
  public void validateAndParse_whenSortOrderNull_expectDefaultSortOrder() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder(null);

    request.validateAndParse();

    assertEquals(ApplicationConstants.DEFAULT_SORT_ORDER, request.getSortOrder());
  }

  @Test
  public void validateAndParse_whenSortOrderBlank_expectDefaultSortOrder() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder("   ");

    request.validateAndParse();

    assertEquals(ApplicationConstants.DEFAULT_SORT_ORDER, request.getSortOrder());
  }

  @Test
  public void validateAndParse_whenSortOrderDesc_expectUnchanged() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder("desc");

    request.validateAndParse();

    assertEquals("desc", request.getSortOrder());
  }

  @Test
  public void validateAndParse_whenSortOrderUpperCase_expectNormalisedToLower() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder("ASC");

    request.validateAndParse();

    assertEquals("asc", request.getSortOrder());
  }

  @Test
  public void validateAndParse_whenSortOrderInvalid_expectException() {
    GetApplicationsRequest request = new GetApplicationsRequest();
    request.setSortOrder("sideways");

    InvalidQueryParameterException ex =
        assertThrows(InvalidQueryParameterException.class, request::validateAndParse);

    assertEquals("sortOrder", ex.getParameter());
    assertEquals("sideways", ex.getInvalidValue());
    assertEquals(ApplicationValidSearchParameters.VALID_SORT_ORDER_VALUES, ex.getValidValues());
  }
}
