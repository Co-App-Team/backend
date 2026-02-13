package com.backend.coapp.dto.response;

import com.backend.coapp.model.document.ReviewModel;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** DTO response for returning review info */
@Getter
@AllArgsConstructor
public class ReviewResponse implements IResponse {

  private String reviewId;
  private String companyId;
  private String userId;
  private String authorName;
  private Integer rating;
  private String comment;
  private String jobTitle;
  private String workTermSeason;
  private Integer workTermYear;

  /**
   * Create a map, where keys are object attributes name and values are attributes' values
   *
   * @return Map
   */
  @Override
  public Map<String, Object> toMap() {
    return Map.of(
        "reviewId", this.reviewId,
        "companyId", this.companyId,
        "userId", this.userId,
        "authorName", this.authorName,
        "rating", this.rating,
        "comment", this.comment,
        "jobTitle", this.jobTitle,
        "workTermSeason", this.workTermSeason,
        "workTermYear", this.workTermYear);
  }

  /**
   * Map ReviewModel to ReviewResponse DTO
   *
   * @param review review model
   * @return ReviewResponse DTO
   */
  public static ReviewResponse fromModel(ReviewModel review) {
    return new ReviewResponse(
        review.getId(),
        review.getCompanyId(),
        review.getUserId(),
        review.getAuthorName(),
        review.getRating(),
        review.getComment(),
        review.getJobTitle(),
        review.getWorkTermSeason(),
        review.getWorkTermYear());
  }
}
