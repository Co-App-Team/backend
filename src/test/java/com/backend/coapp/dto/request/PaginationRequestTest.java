package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.util.PaginationConstants;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
public class PaginationRequestTest {

  @Test
  public void validateRequest_withValidData_expectNoException() {
    PaginationRequest request = new PaginationRequest(0, PaginationConstants.REVIEW_DEFAULT_SIZE);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(0, request.getPage());
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withNullPage_expectDefaultPage() {
    PaginationRequest request = new PaginationRequest(null, 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_PAGE, request.getPage());
  }

  @Test
  public void validateRequest_withNegativePage_expectDefaultPage() {
    PaginationRequest request = new PaginationRequest(-1, 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_PAGE, request.getPage());
  }

  @Test
  public void validateRequest_withNullSize_expectDefaultSize() {
    PaginationRequest request = new PaginationRequest(0, null);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withZeroSize_expectDefaultSize() {
    PaginationRequest request = new PaginationRequest(0, 0);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withSizeAboveMax_expectCappedSize() {
    PaginationRequest request = new PaginationRequest(0, PaginationConstants.REVIEW_MAX_SIZE + 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_MAX_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withValidMaxSize_expectNoChange() {
    PaginationRequest request = new PaginationRequest(0, PaginationConstants.REVIEW_MAX_SIZE);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_MAX_SIZE, request.getSize());
  }
}
