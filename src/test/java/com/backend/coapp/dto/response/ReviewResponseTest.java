package com.backend.coapp.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.document.ReviewModel;
import java.util.Map;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
public class ReviewResponseTest {

  @Test
  public void getterMethod_expectInitValues() {
    ReviewResponse response =
        new ReviewResponse(
            "1", "comp1", "user1", "John Doe", 5, "Great place", "Developer", "Summer", 2024);

    assertEquals("1", response.getReviewId());
    assertEquals("comp1", response.getCompanyId());
    assertEquals("user1", response.getUserId());
    assertEquals("John Doe", response.getAuthorName());
    assertEquals(5, response.getRating());
    assertEquals("Great place", response.getComment());
    assertEquals("Developer", response.getJobTitle());
    assertEquals("Summer", response.getWorkTermSeason());
    assertEquals(2024, response.getWorkTermYear());
  }

  @Test
  public void toMap_expectMapWithInitValues() {
    ReviewResponse response =
        new ReviewResponse(
            "1", "comp1", "user1", "John Doe", 5, "Great place", "Developer", "Summer", 2024);

    Map<String, Object> map = response.toMap();

    assertEquals("1", map.get("reviewId"));
    assertEquals("comp1", map.get("companyId"));
    assertEquals("user1", map.get("userId"));
    assertEquals("John Doe", map.get("authorName"));
    assertEquals(5, map.get("rating"));
    assertEquals("Great place", map.get("comment"));
    assertEquals("Developer", map.get("jobTitle"));
    assertEquals("Summer", map.get("workTermSeason"));
    assertEquals(2024, map.get("workTermYear"));
  }

  @Test
  public void fromModel_expectCorrectMapping() {
    ReviewModel review =
        new ReviewModel("comp1", "user1", "John Doe", 5, "Great", "Dev", "Fall", 2023);

    ReviewResponse response = ReviewResponse.fromModel(review);

    assertEquals(review.getId(), response.getReviewId());
    assertEquals(review.getCompanyId(), response.getCompanyId());
    assertEquals(review.getUserId(), response.getUserId());
    assertEquals(review.getAuthorName(), response.getAuthorName());
    assertEquals(review.getRating(), response.getRating());
    assertEquals(review.getComment(), response.getComment());
    assertEquals(review.getJobTitle(), response.getJobTitle());
    assertEquals(review.getWorkTermSeason(), response.getWorkTermSeason());
    assertEquals(review.getWorkTermYear(), response.getWorkTermYear());
  }
}
