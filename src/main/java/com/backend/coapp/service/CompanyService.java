package com.backend.coapp.service;

import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Company Service
 * This handles all business logic related to Company.
 */
@Slf4j
@Service
@Getter // For testing only
public class CompanyService {

  /** Singleton repositories */
  private final CompanyRepository companyRepository;
  private final ReviewRepository reviewRepository;

  public CompanyService(CompanyRepository companyRepository, ReviewRepository reviewRepository) {
    this.companyRepository = companyRepository;
    this.reviewRepository = reviewRepository;
  }

  /**
   * Create a new company
   *
   * @param companyName The company name
   * @param location The company location
   * @param website The company website URL
   * @return CompanyResponse DTO
   * @throws CompanyAlreadyExistsException if company with this name already exists
   * @throws InvalidWebsiteException if website URL is invalid
   * @throws CompanyServiceFailException if database operation fails
   */
  public CompanyResponse createCompany(String companyName, String location, String website)
    throws CompanyAlreadyExistsException, InvalidWebsiteException, CompanyServiceFailException {

    // Normalize and validate company name
    String normalizedName = companyName.trim();
    String nameLower = normalizedName.toLowerCase();

    // Check if company already exists (case-insensitive)
    if (this.companyRepository.existsByCompanyNameLower(nameLower)) {
      Optional<CompanyModel> existingCompany = this.companyRepository.findByCompanyNameLower(nameLower);
      String existingId = existingCompany.map(CompanyModel::getId).orElse("unknown");
      throw new CompanyAlreadyExistsException(existingId);
    }

    // Validate website URL
    if (!website.matches("^(https?://).*")) {
      throw new InvalidWebsiteException();
    }

    try {
      CompanyModel company = new CompanyModel(normalizedName, location.trim(), website.trim());
      CompanyModel savedCompany = this.companyRepository.save(company);
      log.info("Company created successfully: {}", savedCompany.getId());
      return CompanyResponse.fromModel(savedCompany);
    } catch (Exception e) {
      log.error("Failed to create company: {}", e.getMessage());
      throw new CompanyServiceFailException("Failed to create company. Please try again later.");
    }
  }

  /**
   * Get all companies with optional search and pagination
   *
   * @param search Optional search term for company name (case-insensitive, partial match)
   * @param pageable Pagination parameters
   * @return Page of CompanyResponse DTOs
   * @throws CompanyServiceFailException if database operation fails
   */
  public Page<CompanyResponse> getAllCompanies(String search, Pageable pageable)
    throws CompanyServiceFailException {
    try {
      Page<CompanyModel> companies;

      if (search != null && !search.isBlank()) {
        // Case-insensitive search using regex
        String searchLower = search.toLowerCase();
        companies = this.companyRepository.findByCompanyNameLowerContaining(searchLower, pageable);
      } else {
        companies = this.companyRepository.findAll(pageable);
      }

      return companies.map(CompanyResponse::fromModel);
    } catch (Exception e) {
      log.error("Failed to retrieve companies: {}", e.getMessage());
      throw new CompanyServiceFailException("Failed to retrieve companies. Please try again later.");
    }
  }

  /**
   * Get all companies without pagination
   *
   * @param search Optional search term for company name (case-insensitive, partial match)
   * @return List of CompanyResponse DTOs
   * @throws CompanyServiceFailException if database operation fails
   */
  public List<CompanyResponse> getAllCompaniesNoPagination(String search)
    throws CompanyServiceFailException {
    try {
      List<CompanyModel> companies;

      if (search != null && !search.isBlank()) {
        String searchLower = search.toLowerCase();
        companies = this.companyRepository.findByCompanyNameLowerContaining(searchLower);
      } else {
        companies = this.companyRepository.findAll();
      }

      return companies.stream()
        .map(CompanyResponse::fromModel)
        .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Failed to retrieve companies (without pagination): {}", e.getMessage());
      throw new CompanyServiceFailException("Failed to retrieve companies. Please try again later.");
    }
  }

  /**
   * Get company by ID
   *
   * @param companyId The company ID
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
      throw e;
    } catch (Exception e) {
      log.error("Failed to retrieve company: {}", e.getMessage());
      throw new CompanyServiceFailException("Failed to retrieve company. Please try again later.");
    }
  }

  /**
   * Update company information
   *
   * @param companyId The company ID
   * @param companyName Optional new company name
   * @param location Optional new location
   * @param website Optional new website
   * @return CompanyResponse DTO
   * @throws CompanyNotFoundException if company not found
   * @throws CompanyAlreadyExistsException if new name conflicts with existing company
   * @throws InvalidWebsiteException if website URL is invalid
   * @throws CompanyServiceFailException if database operation fails
   */
  public CompanyResponse updateCompany(String companyId, String companyName, String location, String website)
    throws CompanyNotFoundException, CompanyAlreadyExistsException, InvalidWebsiteException, CompanyServiceFailException {
    try {
      Optional<CompanyModel> companyOpt = this.companyRepository.findById(companyId);

      if (companyOpt.isEmpty()) {
        throw new CompanyNotFoundException();
      }

      CompanyModel company = companyOpt.get();

      // Update company name if provided
      if (companyName != null && !companyName.isBlank()) {
        String normalizedName = companyName.trim();
        String nameLower = normalizedName.toLowerCase();

        // Check if new name conflicts with another company
        if (!company.getCompanyNameLower().equals(nameLower) &&
          this.companyRepository.existsByCompanyNameLower(nameLower)) {
          Optional<CompanyModel> existingCompany = this.companyRepository.findByCompanyNameLower(nameLower);
          String existingId = existingCompany.map(CompanyModel::getId).orElse("unknown");
          throw new CompanyAlreadyExistsException(existingId);
        }

        company.setCompanyName(normalizedName);
      }

      // Update location if provided
      if (location != null && !location.isBlank()) {
        company.setLocation(location.trim());
      }

      // Update website if provided
      if (website != null && !website.isBlank()) {
        if (!website.matches("^(https?://).*")) {
          throw new InvalidWebsiteException();
        }
        company.setWebsite(website.trim());
      }

      CompanyModel updatedCompany = this.companyRepository.save(company);
      log.info("Company updated successfully: {}", companyId);
      return CompanyResponse.fromModel(updatedCompany);
    } catch (CompanyNotFoundException | CompanyAlreadyExistsException | InvalidWebsiteException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to update company: {}", e.getMessage());
      throw new CompanyServiceFailException("Failed to update company. Please try again later.");
    }
  }

  /**
   * Delete company by ID
   *
   * @param companyId The company ID
   * @throws CompanyNotFoundException if company not found
   * @throws CompanyServiceFailException if database operation fails
   */
  public void deleteCompany(String companyId)
    throws CompanyNotFoundException, CompanyServiceFailException {
    try {
      if (!this.companyRepository.existsById(companyId)) {
        throw new CompanyNotFoundException();
      }

      this.companyRepository.deleteById(companyId);
      log.info("Company deleted successfully: {}", companyId);
    } catch (CompanyNotFoundException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to delete company: {}", e.getMessage());
      throw new CompanyServiceFailException("Failed to delete company. Please try again later.");
    }
  }

  /**
   * Update company average rating based on all reviews for this company.
   * This method should be called whenever a review is created, updated, or deleted.
   * It fetches all reviews for the company and recalculates the average rating.
   *
   * @param companyId The company ID
   * @throws CompanyNotFoundException if company not found
   * @throws CompanyServiceFailException if database operation fails
   */
  public void updateAvgRating(String companyId)
    throws CompanyNotFoundException, CompanyServiceFailException {
    try {
      Optional<CompanyModel> companyOpt = this.companyRepository.findById(companyId);

      if (companyOpt.isEmpty()) {
        throw new CompanyNotFoundException();
      }

      CompanyModel company = companyOpt.get();

      // Fetch all reviews for this company
      List<ReviewModel> reviews = this.reviewRepository.findByCompanyId(companyId);

      // Calculate average rating from all reviews
      double avgRating = 0.0;
      if (!reviews.isEmpty()) {
        avgRating = reviews.stream()
          .mapToInt(ReviewModel::getRating)
          .average()
          .orElse(0.0);
      }

      // Update company's average rating
      company.setAvgRating(avgRating);
      this.companyRepository.save(company);

      log.info("Company average rating updated: {} -> {} (based on {} reviews)",
        companyId, avgRating, reviews.size());
    } catch (CompanyNotFoundException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to update company average rating: {}", e.getMessage());
      throw new CompanyServiceFailException("Failed to update company rating. Please try again later.");
    }
  }
}
