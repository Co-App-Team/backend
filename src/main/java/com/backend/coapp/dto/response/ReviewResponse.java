package com.backend.coapp.dto.response;

import com.backend.coapp.model.document.ReviewModel;
import java.util.HashMap;
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
    // needs to be done with hash map to have a null comment since that is an optional field
    // previous toMap strategy didn't allow for null comments
    Map<String, Object> map = new HashMap<>();
    map.put("reviewId", this.reviewId);
    map.put("companyId", this.companyId);
    map.put("userId", this.userId);
    map.put("authorName", this.authorName);
    map.put("rating", this.rating);
    map.put("comment", this.comment); // Now null is allowed
    map.put("jobTitle", this.jobTitle);
    map.put("workTermSeason", this.workTermSeason);
    map.put("workTermYear", this.workTermYear);
    return map;
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
