package com.backend.coapp.model.document;

import com.backend.coapp.util.ReviewConstants;
import com.backend.coapp.util.WorkTermValidator;
import jakarta.validation.constraints.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

/** Review Model */
@Data
@NoArgsConstructor
@Document(collection = "reviews")
@CompoundIndexes({
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
  @Min(
      value = ReviewConstants.MIN_RATING,
      message = "Rating must be at least " + ReviewConstants.MIN_RATING)
  @Max(
      value = ReviewConstants.MAX_RATING,
      message = "Rating must be at most " + ReviewConstants.MAX_RATING)
  private Integer rating;

  @Size(
      max = ReviewConstants.MAX_COMMENT_LENGTH,
      message = "Comment cannot exceed " + ReviewConstants.MAX_COMMENT_LENGTH + " characters")
  private String comment;

  @NotBlank(message = "Job title cannot be empty")
  private String jobTitle;

  @NotBlank(message = "Work term season cannot be empty")
  private String workTermSeason;

  @NotNull(message = "Work term year cannot be null")
  private Integer workTermYear;

  @CreatedDate private Instant dateCreated;

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

    // validation not covered by standard annotations
    WorkTermValidator.validateSeason(workTermSeason);
    WorkTermValidator.validateYear(workTermYear);

    // normalize fields when assigned
    this.companyId = companyId != null ? companyId.trim() : null;
    this.userId = userId != null ? userId.trim() : null;
    this.authorName = authorName != null ? authorName.trim() : null;
    this.rating = rating;
    this.comment = comment != null ? comment.trim() : null;
    this.jobTitle = jobTitle != null ? jobTitle.trim() : null;
    this.workTermSeason = workTermSeason;
    this.workTermYear = workTermYear;
  }

  public void setWorkTermSeason(String workTermSeason) {
    WorkTermValidator.validateSeason(workTermSeason);
    this.workTermSeason = workTermSeason;
  }

  public void setWorkTermYear(Integer workTermYear) {
    WorkTermValidator.validateYear(workTermYear);
    this.workTermYear = workTermYear;
  }
}
