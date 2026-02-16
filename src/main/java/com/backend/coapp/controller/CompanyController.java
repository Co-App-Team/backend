package com.backend.coapp.controller;

import com.backend.coapp.dto.request.CreateCompanyRequest;
import com.backend.coapp.dto.request.GetAllCompaniesRequest;
import com.backend.coapp.dto.request.PaginationRequest;
import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.dto.response.PaginationResponse;
import com.backend.coapp.dto.response.ReviewResponse;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.service.CompanyService;
import com.backend.coapp.service.ReviewService;
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
  private final ReviewService reviewService;

  @Autowired
  public CompanyController(CompanyService companyService, ReviewService reviewService) {
    this.companyService = companyService;
    this.reviewService = reviewService;
  }

  /**
   * Get all companies with optional search and pagination
   *
   * @param request GetAllCompaniesRequest DTO
   * @return ResponseEntity with companies list and optional pagination data
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllCompanies(
      @ModelAttribute GetAllCompaniesRequest request) {

    request.validateRequest();

    Map<String, Object> response = new HashMap<>();
    List<Map<String, Object>> companiesMaps = new ArrayList<>();

    if (request.getUsePagination()) {
      Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
      Page<CompanyResponse> companiesPage =
          this.companyService.getAllCompanies(request.getSearch(), pageable);

      for (CompanyResponse company : companiesPage.getContent()) {
        companiesMaps.add(company.toMap());
      }

      Map<String, Object> paginationResponseMap =
          PaginationResponse.fromPage(companiesPage).toMap();
      response.put("companies", companiesMaps);
      response.put("pagination", paginationResponseMap);
    } else { // no pagination
      List<CompanyResponse> companies = this.companyService.getAllCompanies(request.getSearch());

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
   * @param request GetCompanyByIdRequest DTO
   * @return ResponseEntity with company info
   */
  @GetMapping("/{companyId}")
  public ResponseEntity<Map<String, Object>> getCompanyById(
      @PathVariable String companyId, @ModelAttribute PaginationRequest request) {

    request.validateRequest();

    CompanyResponse company = this.companyService.getCompanyById(companyId);

    Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
    Page<ReviewModel> reviewsPage = this.reviewService.getReviewsByCompanyId(companyId, pageable);

    List<Map<String, Object>> reviewsMaps = new ArrayList<>();
    for (ReviewModel review : reviewsPage.getContent()) {
      reviewsMaps.add(ReviewResponse.fromModel(review).toMap());
    }

    Map<String, Object> response = new HashMap<>();
    response.put("company", company.toMap());
    response.put("reviews", reviewsMaps);
    response.put("reviewsPagination", PaginationResponse.fromPage(reviewsPage).toMap());

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
