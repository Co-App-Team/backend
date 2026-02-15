package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.util.PaginationConstants;
import org.junit.jupiter.api.Test;

public class GetCompanyByIdRequestTest {

  @Test
  public void validateRequest_withValidData_expectNoException() {
    GetCompanyByIdRequest request =
        new GetCompanyByIdRequest("company1", 0, PaginationConstants.REVIEW_DEFAULT_SIZE);

    assertDoesNotThrow(request::validateRequest);
    assertEquals("company1", request.getCompanyId());
    assertEquals(0, request.getPage());
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withNullCompanyId_expectException() {
    GetCompanyByIdRequest request = new GetCompanyByIdRequest(null, 0, 10);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertTrue(exception.getMessage().contains("Company ID cannot be null or empty"));
  }

  @Test
  public void validateRequest_withBlankCompanyId_expectException() {
    GetCompanyByIdRequest request = new GetCompanyByIdRequest("   ", 0, 10);

    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::validateRequest);
    assertTrue(exception.getMessage().contains("Company ID cannot be null or empty"));
  }

  @Test
  public void validateRequest_withNullPage_expectDefaultPage() {
    GetCompanyByIdRequest request = new GetCompanyByIdRequest("company1", null, 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_PAGE, request.getPage());
  }

  @Test
  public void validateRequest_withNegativePage_expectDefaultPage() {
    GetCompanyByIdRequest request = new GetCompanyByIdRequest("company1", -1, 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_PAGE, request.getPage());
  }

  @Test
  public void validateRequest_withNullSize_expectDefaultSize() {
    GetCompanyByIdRequest request = new GetCompanyByIdRequest("company1", 0, null);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withZeroSize_expectDefaultSize() {
    GetCompanyByIdRequest request = new GetCompanyByIdRequest("company1", 0, 0);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withSizeAboveMax_expectCappedSize() {
    GetCompanyByIdRequest request =
        new GetCompanyByIdRequest("company1", 0, PaginationConstants.REVIEW_MAX_SIZE + 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_MAX_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withValidMaxSize_expectNoChange() {
    GetCompanyByIdRequest request =
        new GetCompanyByIdRequest("company1", 0, PaginationConstants.REVIEW_MAX_SIZE);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_MAX_SIZE, request.getSize());
  }
}
