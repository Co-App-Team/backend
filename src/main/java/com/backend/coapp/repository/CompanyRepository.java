package com.backend.coapp.repository;

import com.backend.coapp.model.document.CompanyModel;
import java.util.Optional;
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
}
