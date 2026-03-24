package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.util.ApplicationConstants;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.6 and revised by Eric Hodgson */
class ApplicationPaginationRequestTest {

  @Test
  void getMethods_expectInitValues() {
    ApplicationPaginationRequest request =
        ApplicationPaginationRequest.builder().page(2).size(50).build();

    assertEquals(2, request.getPage());
    assertEquals(50, request.getSize());
  }

  @Test
  void validateRequest_whenAllValid_expectNoChange() {
    ApplicationPaginationRequest request =
        ApplicationPaginationRequest.builder().page(1).size(10).build();
    request.validateRequest();

    assertEquals(1, request.getPage());
    assertEquals(10, request.getSize());
  }

  @Test
  void validateRequest_whenPageNull_expectDefaultPage() {
    ApplicationPaginationRequest request =
        ApplicationPaginationRequest.builder().page(null).size(10).build();
    request.validateRequest();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_PAGE, request.getPage());
  }

  @Test
  void validateRequest_whenPageNegative_expectDefaultPage() {
    ApplicationPaginationRequest request =
        ApplicationPaginationRequest.builder().page(-1).size(10).build();
    request.validateRequest();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_PAGE, request.getPage());
  }

  @Test
  void validateRequest_whenSizeNull_expectDefaultSize() {
    ApplicationPaginationRequest request =
        ApplicationPaginationRequest.builder().page(0).size(null).build();
    request.validateRequest();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_SIZE, request.getSize());
  }

  @Test
  void validateRequest_whenSizeTooSmall_expectDefaultSize() {
    ApplicationPaginationRequest request =
        ApplicationPaginationRequest.builder().page(0).size(0).build();
    request.validateRequest();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_SIZE, request.getSize());
  }

  @Test
  void validateRequest_whenSizeTooLarge_expectCappedToMax() {
    ApplicationPaginationRequest request =
        ApplicationPaginationRequest.builder().page(0).size(999).build();
    request.validateRequest();

    assertEquals(ApplicationConstants.APPLICATION_MAX_SIZE, request.getSize());
  }

  @Test
  void validateRequest_whenMultipleIssues_expectAllFixed() {
    ApplicationPaginationRequest request =
        ApplicationPaginationRequest.builder().page(-3).size(999).build();
    request.validateRequest();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_PAGE, request.getPage());
    assertEquals(ApplicationConstants.APPLICATION_MAX_SIZE, request.getSize());
  }
}
