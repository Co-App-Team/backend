package com.backend.coapp.service;

import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import com.backend.coapp.util.UrlValidator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Company Service */
@Slf4j
@Service
@Getter
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final ReviewRepository reviewRepository;

  public CompanyService(CompanyRepository companyRepository, ReviewRepository reviewRepository) {
    this.companyRepository = companyRepository;
    this.reviewRepository = reviewRepository;
  }

  /**
   * Create a new company
   *
   * @param companyName company name
   * @param location company location
   * @param website company website URL
   * @return CompanyResponse DTO
   * @throws CompanyAlreadyExistsException if company with this name already exists
   * @throws InvalidWebsiteException if website URL is invalid
   * @throws CompanyServiceFailException if database operation fails
   */
  public CompanyResponse createCompany(String companyName, String location, String website)
      throws CompanyAlreadyExistsException, InvalidWebsiteException, CompanyServiceFailException {

    String trimmedName = companyName.trim();
    String nameLower = trimmedName.toLowerCase();

    // Check if company already exists
    CompanyModel existingCompany;
    existingCompany = this.companyRepository.findByCompanyNameLower(nameLower).orElse(null);
    if (existingCompany != null) {
      throw new CompanyAlreadyExistsException(existingCompany.getId());
    }

    if (!UrlValidator.isValidUrl(website.trim())) {
      throw new InvalidWebsiteException();
    }

    try {
      CompanyModel company = new CompanyModel(trimmedName, location.trim(), website.trim());
      CompanyModel savedCompany = this.companyRepository.save(company);
      return CompanyResponse.fromModel(savedCompany);

    } catch (Exception e) {
      log.error("failed to create company: {}", e.getMessage());
      throw new CompanyServiceFailException("failed to create company. Please try again.");
    }
  }

  /**
   * Get all companies with optional search and with pagination
   *
   * @param searchString search term for company name: optional, case-insensitive, partial match
   * @param pageable pagination parameters
   * @return page of CompanyResponse DTOs
   * @throws CompanyServiceFailException if database operation fails
   */
  public Page<CompanyResponse> getAllCompanies(String searchString, Pageable pageable)
      throws CompanyServiceFailException {
    try {
      Page<CompanyModel> companies;

      if (searchString != null && !searchString.isBlank()) {
        companies =
            this.companyRepository.findByCompanyNameLowerContaining(
                searchString.toLowerCase(), pageable);
      } else {
        companies = this.companyRepository.findAll(pageable);
      }

      return companies.map(CompanyResponse::fromModel);

    } catch (Exception e) {
      log.error("failed to retrieve companies: {}", e.getMessage());
      throw new CompanyServiceFailException(
          "failed to retrieve companies. Please try again later.");
    }
  }

  /**
   * Get all companies without pagination
   *
   * @param search Optional search term for company name (case-insensitive, partial match)
   * @return List of CompanyResponse DTOs
   * @throws CompanyServiceFailException if database operation fails
   */
  public List<CompanyResponse> getAllCompanies(String search) throws CompanyServiceFailException {
    // unpaged allows us to just do the above method without any pagination
    return getAllCompanies(search, Pageable.unpaged()).getContent();
  }

  /**
   * Get company using ID
   *
   * @param companyId company ID
   * @return CompanyResponse DTO
   * @throws CompanyNotFoundException if company not found
   * @throws CompanyServiceFailException if database operation fails
   */
  public CompanyResponse getCompanyById(String companyId)
      throws CompanyNotFoundException, CompanyServiceFailException {

    try {
      Optional<CompanyModel> company = this.companyRepository.findById(companyId);

      if (company.isEmpty()) {
        throw new CompanyNotFoundException();
      }

      return CompanyResponse.fromModel(company.get());

    } catch (CompanyNotFoundException e) {
      throw e; // do this so we can also catch other types of errors separately

    } catch (Exception e) {
      log.error("Failed to retrieve company: {}", e.getMessage());
      throw new CompanyServiceFailException("Failed to retrieve company. Please try again later.");
    }
  }

  /**
   * Update company average rating based on all reviews for this company.
   *
   * <p>This method should be called whenever a review is created, updated, or deleted.
   *
   * <p>All reviews are fetched and used to recalculate average
   *
   * @param companyId company ID
   * @throws CompanyNotFoundException if company not found
   * @throws CompanyServiceFailException if database operation fails
   */
  public void updateAvgRating(String companyId)
      throws CompanyNotFoundException, CompanyServiceFailException {
    try {
      Optional<CompanyModel> companyCheck = this.companyRepository.findById(companyId);

      if (companyCheck.isEmpty()) {
        throw new CompanyNotFoundException();
      }
      CompanyModel company = companyCheck.get();

      List<ReviewModel> reviews = this.reviewRepository.findByCompanyId(companyId);

      double avgRating = 0.0;
      if (!reviews.isEmpty()) {
        int totalRating = 0;
        for (ReviewModel review : reviews) {
          totalRating += review.getRating();
        }
        avgRating = (double) totalRating / reviews.size();
      }

      company.setAvgRating(avgRating);
      this.companyRepository.save(company);

      log.info("Company average rating updated: {} to {}", companyId, avgRating);

    } catch (CompanyNotFoundException e) {
      throw e; // do this so we can also catch other types of errors separately

    } catch (Exception e) {
      log.error("Failed to update company average rating: {}", e.getMessage());
      throw new CompanyServiceFailException(
          "Failed to update company rating. Please try again later.");
    }
  }
}
