package com.backend.coapp.model.document;

import com.backend.coapp.util.ReviewConstants;
import com.backend.coapp.util.ReviewConstants.*;
import com.backend.coapp.util.WorkTermValidator;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/** Review Model */
@Getter
@SuppressWarnings(
    "LombokSetterMayBeUsed") // ignore this warning when we want to use our own setters
@NoArgsConstructor
@Document(collection = "reviews")
@CompoundIndexes({
  // the :1 makes it ascending and the unique=true ensures all combinations must be unique
  @CompoundIndex(name = "user_company_unique", def = "{'userId': 1, 'companyId': 1}", unique = true)
})
public class ReviewModel {

  @Id private String id;

  @NotBlank(message = "Company ID cannot be empty")
  private String companyId;

  @NotBlank(message = "User ID cannot be empty")
  private String userId;

  @NotBlank(message = "Author name cannot be empty")
  private String authorName;

  @NotNull(message = "Rating cannot be null")
  @Min(value = 1, message = ("Rating must be at least " + ReviewConstants.MIN_RATING))
  @Max(value = 5, message = ("Rating must be at least " + ReviewConstants.MAX_RATING))
  private Integer rating;

  @Size(
      max = ReviewConstants.MAX_COMMENT_LENGTH,
      message = ("Comment cannot exceed " + ReviewConstants.MAX_COMMENT_LENGTH + " characters"))
  private String comment;

  @NotBlank(message = "Job title cannot be empty")
  private String jobTitle;

  @NotBlank(message = "Work term season cannot be empty")
  private String workTermSeason;

  @NotNull(message = "Work term year cannot be null")
  private Integer workTermYear;

  /**
   * Constructor for creating a new review
   *
   * @param companyId The ID of the company being reviewed
   * @param userId The ID of the user writing the review
   * @param authorName The display name of the user writing the review
   * @param rating The rating (1-5)
   * @param comment The review comment (optional, max 2000 chars)
   * @param jobTitle The job title during the work term
   * @param workTermSeason The season (Fall, Winter, or Summer)
   * @param workTermYear The year (1950 to current year)
   */
  public ReviewModel(
      String companyId,
      String userId,
      String authorName,
      Integer rating,
      String comment,
      String jobTitle,
      String workTermSeason,
      Integer workTermYear) {

    if (companyId == null || companyId.trim().isEmpty()) {
      throw new IllegalArgumentException("Company ID cannot be null or empty");
    }
    if (userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }
    if (authorName == null || authorName.trim().isEmpty()) {
      throw new IllegalArgumentException("Author name cannot be null or empty");
    }
    if (rating == null
        || rating < ReviewConstants.MIN_RATING
        || rating > ReviewConstants.MAX_RATING) {
      throw new IllegalArgumentException(
          "Rating must be between "
              + ReviewConstants.MIN_RATING
              + " and "
              + ReviewConstants.MAX_RATING);
    }
    if (jobTitle == null || jobTitle.trim().isEmpty()) {
      throw new IllegalArgumentException("Job title cannot be null or empty");
    }

    // will throw if anything is invalid
    WorkTermValidator.validateSeason(workTermSeason);
    WorkTermValidator.validateYear(workTermYear);

    this.companyId = companyId.trim();
    this.userId = userId.trim();
    this.authorName = authorName.trim();
    this.rating = rating;
    this.comment = comment != null ? comment.trim() : null;
    this.jobTitle = jobTitle.trim();
    this.workTermSeason = workTermSeason;
    this.workTermYear = workTermYear;
  }

  /** Setter methods */
  public void setId(String id) {
    this.id = id;
  }

  public void setCompanyId(String companyId) {
    if (companyId == null || companyId.trim().isEmpty()) {
      throw new IllegalArgumentException("Company ID cannot be null or empty");
    }
    this.companyId = companyId.trim();
  }

  public void setUserId(String userId) {
    if (userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }
    this.userId = userId.trim();
  }

  public void setAuthorName(String authorName) {
    if (authorName == null || authorName.trim().isEmpty()) {
      throw new IllegalArgumentException("Author name cannot be null or empty");
    }
    this.authorName = authorName.trim();
  }

  public void setRating(Integer rating) {
    if (rating == null
        || rating < ReviewConstants.MIN_RATING
        || rating > ReviewConstants.MAX_RATING) {
      throw new IllegalArgumentException(
          "Rating must be between "
              + ReviewConstants.MIN_RATING
              + " and "
              + ReviewConstants.MAX_RATING);
    }
    this.rating = rating;
  }

  public void setComment(String comment) {
    this.comment = comment != null ? comment.trim() : null;
  }

  public void setJobTitle(String jobTitle) {
    if (jobTitle == null || jobTitle.trim().isEmpty()) {
      throw new IllegalArgumentException("Job title cannot be null or empty");
    }
    this.jobTitle = jobTitle.trim();
  }

  public void setWorkTermSeason(String workTermSeason) {
    com.backend.coapp.util.WorkTermValidator.validateSeason(workTermSeason);
    this.workTermSeason = workTermSeason;
  }

  public void setWorkTermYear(Integer workTermYear) {
    com.backend.coapp.util.WorkTermValidator.validateYear(workTermYear);
    this.workTermYear = workTermYear;
  }
}
