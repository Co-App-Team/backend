package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.util.ApplicationConstants;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.6 and revised by Eric Hodgson */
public class ApplicationPaginationRequestTest {

  @Test
  public void getMethods_expectInitValues() {
    ApplicationPaginationRequest request = new ApplicationPaginationRequest(2, 50);

    assertEquals(2, request.getPage());
    assertEquals(50, request.getSize());
  }

  @Test
  public void validatePagination_whenAllValid_expectNoChange() {
    ApplicationPaginationRequest request = new ApplicationPaginationRequest(1, 10);
    request.validatePagination();

    assertEquals(1, request.getPage());
    assertEquals(10, request.getSize());
  }

  @Test
  public void validatePagination_whenPageNull_expectDefaultPage() {
    ApplicationPaginationRequest request = new ApplicationPaginationRequest(null, 10);
    request.validatePagination();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_PAGE, request.getPage());
  }

  @Test
  public void validatePagination_whenPageNegative_expectDefaultPage() {
    ApplicationPaginationRequest request = new ApplicationPaginationRequest(-1, 10);
    request.validatePagination();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_PAGE, request.getPage());
  }

  @Test
  public void validatePagination_whenSizeNull_expectDefaultSize() {
    ApplicationPaginationRequest request = new ApplicationPaginationRequest(0, null);
    request.validatePagination();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validatePagination_whenSizeTooSmall_expectDefaultSize() {
    ApplicationPaginationRequest request = new ApplicationPaginationRequest(0, 0);
    request.validatePagination();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_SIZE, request.getSize());
  }

  @Test
  public void validatePagination_whenSizeTooLarge_expectCappedToMax() {
    ApplicationPaginationRequest request = new ApplicationPaginationRequest(0, 999);
    request.validatePagination();

    assertEquals(ApplicationConstants.APPLICATION_MAX_SIZE, request.getSize());
  }

  @Test
  public void validatePagination_whenMultipleIssues_expectAllFixed() {
    ApplicationPaginationRequest request = new ApplicationPaginationRequest(-3, 999);
    request.validatePagination();

    assertEquals(ApplicationConstants.APPLICATION_DEFAULT_PAGE, request.getPage());
    assertEquals(ApplicationConstants.APPLICATION_MAX_SIZE, request.getSize());
  }
}
