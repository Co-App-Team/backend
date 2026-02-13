package com.backend.coapp.controller;

import com.backend.coapp.dto.request.CreateCompanyRequest;
import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.dto.response.PaginationResponse;
import com.backend.coapp.service.CompanyService;
import com.backend.coapp.util.PaginationConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Getter
@RequestMapping("/api/companies")
public class CompanyController {

  private final CompanyService companyService;

  @Autowired
  public CompanyController(CompanyService companyService) {
    this.companyService = companyService;
  }

  /**
   * Get all companies with optional search and pagination
   *
   * @param search Optional search term for company name
   * @param page Page number (default 0)
   * @param size Items per page (default 20, max 100)
   * @param usePagination Whether to use pagination (default false)
   * @return ResponseEntity with companies list and optional pagination data
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllCompanies(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = PaginationConstants.COMPANY_DEFAULT_PAGE_STR) int page,
      @RequestParam(defaultValue = PaginationConstants.COMPANY_DEFAULT_SIZE_STR) int size,
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_USE_PAGINATION_STR)
          boolean usePagination) {

    if (size > PaginationConstants.COMPANY_MAX_SIZE) {
      size = PaginationConstants.COMPANY_MAX_SIZE;
    }
    if (size < PaginationConstants.COMPANY_MIN_SIZE) {
      size = PaginationConstants.COMPANY_DEFAULT_SIZE;
    }
    if (page < PaginationConstants.COMPANY_DEFAULT_PAGE) {
      page = PaginationConstants.COMPANY_DEFAULT_PAGE;
    }

    Map<String, Object> response = new HashMap<>();
    List<Map<String, Object>> companiesMaps = new ArrayList<>();

    if (usePagination) {
      Pageable pageable = PageRequest.of(page, size);
      Page<CompanyResponse> companiesPage = this.companyService.getAllCompanies(search, pageable);

      for (CompanyResponse company : companiesPage.getContent()) {
        companiesMaps.add(company.toMap());
      }

      Map<String, Object> paginationResponseMap =
          PaginationResponse.fromPage(companiesPage).toMap();

      response.put("companies", companiesMaps);
      response.put("pagination", paginationResponseMap);

    } else { // no pagination

      List<CompanyResponse> companies = this.companyService.getAllCompanies(search);

      for (CompanyResponse company : companies) {
        companiesMaps.add(company.toMap());
      }

      response.put("companies", companiesMaps);
    }

    return ResponseEntity.ok(response);
  }

  /**
   * Get company profile with ID
   *
   * @param companyId company ID
   * @return ResponseEntity with company info
   */
  @GetMapping("/{companyId}")
  public ResponseEntity<Map<String, Object>> getCompanyById(@PathVariable String companyId) {
    CompanyResponse company = this.companyService.getCompanyById(companyId);

    Map<String, Object> response = new HashMap<>();
    response.put("company", company.toMap());

    // TODO: Add reviews and reviewsPagination when ReviewService is implemented

    return ResponseEntity.ok(response);
  }

  /**
   * Create a new company
   *
   * @param createRequest CreateCompanyRequest DTO
   * @return ResponseEntity with created company info
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createCompany(
      @RequestBody CreateCompanyRequest createRequest) {

    createRequest.validateRequest();

    CompanyResponse company =
        this.companyService.createCompany(
            createRequest.getCompanyName(),
            createRequest.getLocation(),
            createRequest.getWebsite());

    return ResponseEntity.status(HttpStatus.CREATED).body(company.toMap());
  }
}
