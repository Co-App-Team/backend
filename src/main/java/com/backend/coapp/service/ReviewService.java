package com.backend.coapp.service;

import com.backend.coapp.exception.CompanyNotFoundException;
import com.backend.coapp.exception.ReviewAlreadyExistsException;
import com.backend.coapp.exception.ReviewNotFoundException;
import com.backend.coapp.exception.ReviewNotOwnedException;
import com.backend.coapp.exception.ReviewServiceFailException;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Getter
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final CompanyRepository companyRepository;
  private final CompanyService companyService;

  @Autowired
  public ReviewService(
      ReviewRepository reviewRepository,
      CompanyRepository companyRepository,
      CompanyService companyService) {
    this.reviewRepository = reviewRepository;
    this.companyRepository = companyRepository;
    this.companyService = companyService;
  }

  /**
   * Create a new review for a company
   *
   * @param companyId ID of the company
   * @param userId ID of the user
   * @param authorName display name of the user
   * @param rating rating (1-5)
   * @param comment review comment (optional)
   * @param jobTitle job title during the work term
   * @param workTermSeason season (Fall, Winter, or Summer)
   * @param workTermYear year (1950 to current year)
   * @return created ReviewModel
   * @throws CompanyNotFoundException if company does not exist
   * @throws ReviewAlreadyExistsException if user already reviewed this company
   * @throws ReviewServiceFailException if an unexpected error occurs
   */
  @Transactional
  public ReviewModel createReview(
      String companyId,
      String userId,
      String authorName,
      Integer rating,
      String comment,
      String jobTitle,
      String workTermSeason,
      Integer workTermYear) {

    try {
      if (!this.companyRepository.existsById(companyId)) {
        throw new CompanyNotFoundException();
      }

      if (this.reviewRepository.existsByUserIdAndCompanyId(userId, companyId)) {
        throw new ReviewAlreadyExistsException();
      }

      ReviewModel review =
          new ReviewModel(
              companyId,
              userId,
              authorName,
              rating,
              comment,
              jobTitle,
              workTermSeason,
              workTermYear);

      ReviewModel savedReview = this.reviewRepository.save(review);
      this.companyService.updateAvgRating(companyId);

      return savedReview;

    } catch (CompanyNotFoundException | ReviewAlreadyExistsException ex) {
      throw ex;

    } catch (DuplicateKeyException ex) {

      // there could be a race condition here where duplicate reviews are created
      if (this.reviewRepository.existsByUserIdAndCompanyId(userId, companyId)) {
        throw new ReviewAlreadyExistsException();
      }
      throw new ReviewServiceFailException("Failed to create review: " + ex.getMessage());

    } catch (Exception ex) {

      log.error("Error creating review for company: {}", companyId, ex);
      throw new ReviewServiceFailException("Failed to create review: " + ex.getMessage());
    }
  }

  /**
   * Update a review
   *
   * @param reviewId ID of the review
   * @param userId ID of the user
   * @param rating new rating (optional)
   * @param comment new comment (optional)
   * @param jobTitle new job title (optional)
   * @param workTermSeason new work term season (optional)
   * @param workTermYear new work term year (optional)
   * @return updated ReviewModel
   * @throws ReviewNotFoundException if review does not exist
   * @throws ReviewNotOwnedException if user doesn't own the review
   * @throws ReviewServiceFailException if an unexpected error occurs
   */
  @Transactional
  public ReviewModel updateReview(
      String reviewId,
      String userId,
      Integer rating,
      String comment,
      String jobTitle,
      String workTermSeason,
      Integer workTermYear) {

    try {
      ReviewModel review =
          this.reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

      // must be owned by user
      if (!review.getUserId().equals(userId)) {
        throw new ReviewNotOwnedException("update");
      }

      // want to track if its changed so we can update the avg rating for the company
      boolean ratingChanged = false;
      if (rating != null && !rating.equals(review.getRating())) {
        review.setRating(rating);
        ratingChanged = true;
      }

      if (comment != null) {
        review.setComment(comment.trim());
      }
      if (jobTitle != null) {
        review.setJobTitle(jobTitle.trim());
      }
      if (workTermSeason != null) {
        review.setWorkTermSeason(workTermSeason);
      }
      if (workTermYear != null) {
        review.setWorkTermYear(workTermYear);
      }

      ReviewModel updatedReview = this.reviewRepository.save(review);

      // update company average rating if rating changed
      if (ratingChanged) {
        this.companyService.updateAvgRating(review.getCompanyId());
      }

      return updatedReview;

    } catch (ReviewNotFoundException | ReviewNotOwnedException ex) {
      throw ex;

    } catch (Exception ex) {
      log.error("Error updating review: {}", reviewId, ex);
      throw new ReviewServiceFailException("Failed to update review: " + ex.getMessage());
    }
  }

  /**
   * Delete a review
   *
   * @param reviewId ID of the review
   * @param userId ID of the user
   * @throws ReviewNotFoundException if review does not exist
   * @throws ReviewNotOwnedException if user doesn't own the review
   * @throws ReviewServiceFailException if an unexpected error occurs
   */
  @Transactional
  public void deleteReview(String reviewId, String userId) {

    try {
      ReviewModel review =
          this.reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

      // must be owned by user
      if (!review.getUserId().equals(userId)) {
        throw new ReviewNotOwnedException("delete");
      }

      String companyId = review.getCompanyId();

      this.reviewRepository.deleteById(reviewId);

      this.companyService.updateAvgRating(companyId);

    } catch (ReviewNotFoundException | ReviewNotOwnedException ex) {
      throw ex;

    } catch (Exception ex) {
      log.error("Error deleting review: {}", reviewId, ex);
      throw new ReviewServiceFailException("Failed to delete review: " + ex.getMessage());
    }
  }
}
