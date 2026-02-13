package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.util.PaginationConstants;
import org.junit.jupiter.api.Test;

public class GetAllCompaniesRequestTest {

  @Test
  public void getMethods_expectInitValues() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest("niche", 0, 20, true);
    assertEquals("niche", params.getSearch());
    assertEquals(0, params.getPage());
    assertEquals(20, params.getSize());
    assertTrue(params.getUsePagination());
  }

  @Test
  public void validateRequest_whenAllValid_expectNoChange() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest("niche", 0, 20, true);
    params.validateRequest();

    assertEquals("niche", params.getSearch());
    assertEquals(0, params.getPage());
    assertEquals(20, params.getSize());
    assertTrue(params.getUsePagination());
  }

  @Test
  public void validateRequest_whenNullValues_expectDefaults() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest(null, null, null, null);
    params.validateRequest();

    assertNull(params.getSearch());
    assertEquals(PaginationConstants.COMPANY_DEFAULT_PAGE, params.getPage());
    assertEquals(PaginationConstants.COMPANY_DEFAULT_SIZE, params.getSize());
    assertFalse(params.getUsePagination());
  }

  @Test
  public void validateRequest_whenSizeTooLarge_expectCappedToMax() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest(null, 0, 150, true);
    params.validateRequest();

    assertEquals(PaginationConstants.COMPANY_MAX_SIZE, params.getSize());
  }

  @Test
  public void validateRequest_whenSizeTooSmall_expectDefaultSize() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest(null, 0, 0, true);
    params.validateRequest();

    assertEquals(PaginationConstants.COMPANY_DEFAULT_SIZE, params.getSize());
  }

  @Test
  public void validateRequest_whenSizeNegative_expectDefaultSize() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest(null, 0, -5, true);
    params.validateRequest();

    assertEquals(PaginationConstants.COMPANY_DEFAULT_SIZE, params.getSize());
  }

  @Test
  public void validateRequest_whenPageNegative_expectDefaultPage() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest(null, -1, 20, true);
    params.validateRequest();

    assertEquals(PaginationConstants.COMPANY_DEFAULT_PAGE, params.getPage());
  }

  @Test
  public void validateRequest_whenSearchPresent_expectUnchanged() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest("amazon", 0, 20, false);
    params.validateRequest();

    assertEquals("amazon", params.getSearch());
  }

  @Test
  public void validateRequest_whenMultipleIssues_expectAllFixed() {
    GetAllCompaniesRequest params = new GetAllCompaniesRequest("test", -5, 200, null);
    params.validateRequest();

    assertEquals("test", params.getSearch());
    assertEquals(PaginationConstants.COMPANY_DEFAULT_PAGE, params.getPage());
    assertEquals(PaginationConstants.COMPANY_MAX_SIZE, params.getSize());
    assertFalse(params.getUsePagination());
  }
}
