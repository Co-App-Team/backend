package com.backend.coapp.repository;

import com.backend.coapp.model.document.ReviewModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for Reviews */
@Repository
public interface ReviewRepository extends MongoRepository<ReviewModel, String> {

  /**
   * Finds all reviews for a specific company with pagination
   *
   * @param companyId The company ID
   * @param pageable Pagination information
   * @return Page of reviews for the company
   */
  Page<ReviewModel> findByCompanyId(String companyId, Pageable pageable);

  /**
   * Finds all reviews for a specific company.
   *
   * @param companyId The company ID
   * @return List of reviews for the company
   */
  List<ReviewModel> findByCompanyId(String companyId);

  /**
   * Finds a review by user and company (for checking if a user has already reviewed a company).
   *
   * @param userId The user ID
   * @param companyId The company ID
   * @return Optional containing the review if found
   */
  Optional<ReviewModel> findByUserIdAndCompanyId(String userId, String companyId);

  /**
   * Checks if a user has already reviewed a company
   *
   * @param userId The user ID
   * @param companyId The company ID
   * @return true if review exists, false otherwise
   */
  boolean existsByUserIdAndCompanyId(String userId, String companyId);

  /**
   * Finds all reviews by a specific user
   *
   * @param userId The user ID
   * @return List of reviews by the user
   */
  List<ReviewModel> findByUserId(String userId);

  /**
   * Counts the number of reviews for a company.
   *
   * @param companyId The company ID
   * @return Number of reviews
   */
  long countByCompanyId(String companyId);
}
