package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.util.PaginationConstants;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
public class ReviewPaginationRequestTest {

  @Test
  public void validateRequest_withValidData_expectNoException() {
    ReviewPaginationRequest request = new ReviewPaginationRequest(0, PaginationConstants.REVIEW_DEFAULT_SIZE);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(0, request.getPage());
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withNullPage_expectDefaultPage() {
    ReviewPaginationRequest request = new ReviewPaginationRequest(null, 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_PAGE, request.getPage());
  }

  @Test
  public void validateRequest_withNegativePage_expectDefaultPage() {
    ReviewPaginationRequest request = new ReviewPaginationRequest(-1, 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_PAGE, request.getPage());
  }

  @Test
  public void validateRequest_withNullSize_expectDefaultSize() {
    ReviewPaginationRequest request = new ReviewPaginationRequest(0, null);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withZeroSize_expectDefaultSize() {
    ReviewPaginationRequest request = new ReviewPaginationRequest(0, 0);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withSizeAboveMax_expectCappedSize() {
    ReviewPaginationRequest request = new ReviewPaginationRequest(0, PaginationConstants.REVIEW_MAX_SIZE + 10);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_MAX_SIZE, request.getSize());
  }

  @Test
  public void validateRequest_withValidMaxSize_expectNoChange() {
    ReviewPaginationRequest request = new ReviewPaginationRequest(0, PaginationConstants.REVIEW_MAX_SIZE);

    assertDoesNotThrow(request::validateRequest);
    assertEquals(PaginationConstants.REVIEW_MAX_SIZE, request.getSize());
  }
}
