package com.backend.coapp.controller;

import com.backend.coapp.dto.request.CreateCompanyRequest;
import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.dto.response.PaginationResponse;
import com.backend.coapp.service.CompanyService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Getter // For testing
@RequestMapping("/api/companies")
public class CompanyController {

  /** Singleton service */
  private final CompanyService companyService;

  @Autowired
  public CompanyController(CompanyService companyService) {
    this.companyService = companyService;
  }

  /**
   * Get all companies with optional search and pagination
   *
   * @param search Optional search term for company name
   * @param page Page number (0-indexed, default: 0)
   * @param size Items per page (default: 20, max: 100)
   * @param usePagination Whether to use pagination (default: false)
   * @return ResponseEntity with companies list and optional pagination metadata
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllCompanies(
    @RequestParam(required = false) String search,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "false") boolean usePagination) {

    //! TODO: remove this or add constants
    // Validate and cap page size
    if (size > 100) {
      size = 100;
    }
    if (size < 1) {
      size = 20;
    }
    if (page < 0) {
      page = 0;
    }

    Map<String, Object> response = new HashMap<>();

    if (usePagination) {
      Pageable pageable = PageRequest.of(page, size);
      Page<CompanyResponse> companiesPage = this.companyService.getAllCompanies(search, pageable);

      // Convert CompanyResponse objects to Maps
      List<Map<String, Object>> companiesMaps = companiesPage.getContent().stream()
        .map(CompanyResponse::toMap)
        .collect(Collectors.toList());

      PaginationResponse paginationResponse = PaginationResponse.fromPage(companiesPage);

      response.put("companies", companiesMaps);
      response.put("pagination", paginationResponse.toMap());
    } else {
      List<CompanyResponse> companies = this.companyService.getAllCompaniesNoPagination(search);

      // Convert CompanyResponse objects to Maps
      List<Map<String, Object>> companiesMaps = companies.stream()
        .map(CompanyResponse::toMap)
        .collect(Collectors.toList());

      response.put("companies", companiesMaps);
    }

    return ResponseEntity.ok(response);
  }

  /**
   * Get company profile by ID
   * Note: This endpoint will be extended to include reviews in a future implementation
   *
   * @param companyId The company ID
   * @return ResponseEntity with company information
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
   * @return ResponseEntity with created company information
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createCompany(@RequestBody CreateCompanyRequest createRequest) {
    createRequest.validateRequest();

    CompanyResponse company = this.companyService.createCompany(
      createRequest.getCompanyName(),
      createRequest.getLocation(),
      createRequest.getWebsite()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(company.toMap());
  }
}
