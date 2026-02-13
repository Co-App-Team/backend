package com.backend.coapp.controller;

import com.backend.coapp.dto.request.CreateReviewRequest;
import com.backend.coapp.dto.request.UpdateReviewRequest;
import com.backend.coapp.dto.response.ReviewResponse;
import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.service.ReviewService;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Getter
@RequestMapping("/api/companies/{companyId}/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  @Autowired
  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  /**
   * Create a new review for a company
   *
   * @param companyId company ID from path parameter
   * @param createRequest CreateReviewRequest DTO
   * @param authentication authentication object
   * @return ResponseEntity with created review info
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createReview(
      @PathVariable String companyId,
      @RequestBody CreateReviewRequest createRequest,
      Authentication authentication) {

    createRequest.validateRequest();

    UserModel user = (UserModel) authentication.getPrincipal();
    if (user == null) {
      throw new InvalidRequestException("User authentication is required.");
    }

    String userId = user.getId();
    String authorName = user.getFirstName() + " " + user.getLastName();

    ReviewModel review =
        this.reviewService.createReview(
            companyId,
            userId,
            authorName,
            createRequest.getRating(),
            createRequest.getComment(),
            createRequest.getJobTitle(),
            createRequest.getWorkTermSeason(),
            createRequest.getWorkTermYear());

    ReviewResponse response = ReviewResponse.fromModel(review);
    return ResponseEntity.status(HttpStatus.CREATED).body(response.toMap());
  }

  /**
   * Update an existing review
   *
   * @param companyId company ID from path parameter
   * @param reviewId review ID from path parameter
   * @param updateRequest UpdateReviewRequest DTO
   * @param authentication authentication object
   * @return ResponseEntity with updated review info
   */
  @PutMapping("/{reviewId}")
  public ResponseEntity<Map<String, Object>> updateReview(
      @PathVariable String companyId,
      @PathVariable String reviewId,
      @RequestBody UpdateReviewRequest updateRequest,
      Authentication authentication) {

    updateRequest.validateRequest();

    UserModel user = (UserModel) authentication.getPrincipal();
    if (user == null) {
      throw new InvalidRequestException("User authentication is required.");
    }

    String userId = user.getId();
    this.reviewService.verifyReviewBelongsToCompany(reviewId, companyId);

    ReviewModel review =
        this.reviewService.updateReview(
            reviewId,
            userId,
            updateRequest.getRating(),
            updateRequest.getComment(),
            updateRequest.getJobTitle(),
            updateRequest.getWorkTermSeason(),
            updateRequest.getWorkTermYear());

    ReviewResponse response = ReviewResponse.fromModel(review);
    return ResponseEntity.ok(response.toMap());
  }

  /**
   * Delete a review
   *
   * @param companyId company ID from path parameter
   * @param reviewId review ID from path parameter
   * @param authentication authentication object
   * @return ResponseEntity with success message
   */
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Map<String, Object>> deleteReview(
      @PathVariable String companyId,
      @PathVariable String reviewId,
      Authentication authentication) {

    UserModel user = (UserModel) authentication.getPrincipal();
    if (user == null) {
      throw new InvalidRequestException("User authentication is required.");
    }

    String userId = user.getId();
    this.reviewService.verifyReviewBelongsToCompany(reviewId, companyId);

    this.reviewService.deleteReview(reviewId, userId);

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Review deleted successfully.");
    response.put("reviewId", reviewId);

    return ResponseEntity.ok(response);
  }
}
