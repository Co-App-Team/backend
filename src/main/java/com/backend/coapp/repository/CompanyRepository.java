package com.backend.coapp.repository;

import com.backend.coapp.model.document.CompanyModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Repository to interact with companies in the database */
@Repository
public interface CompanyRepository extends MongoRepository<CompanyModel, String> {

  /**
   * Finds a company by its lowercase name
   *
   * @param companyNameLower The lowercase company name
   * @return a company if found
   */
  Optional<CompanyModel> findByCompanyNameLower(String companyNameLower);

  /**
   * Checks if a company exists with the lowercase name
   *
   * @param companyNameLower The lowercase company name
   * @return true if exists, false if it doesn't
   */
  boolean existsByCompanyNameLower(String companyNameLower);

  /**
   * Find companies by partial name match (case-insensitive) with pagination
   *
   * @param searchTerm The search term (already lowercased)
   * @param pageable Pagination parameters
   * @return Page of companies matching the search term
   */
  Page<CompanyModel> findByCompanyNameLowerContaining(String searchTerm, Pageable pageable);

  /**
   * Find companies by partial name match (case-insensitive) without pagination
   *
   * @param searchTerm The search term (already lowercased)
   * @return List of companies matching the search term
   */
  List<CompanyModel> findByCompanyNameLowerContaining(String searchTerm);
}
