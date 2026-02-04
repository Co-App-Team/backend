package com.backend.coapp.model.document;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.util.ReviewConstants;
import com.backend.coapp.util.WorkTermValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ReviewModel tests */
public class ReviewModelTest {

  private ReviewModel testReviewModel;

  @BeforeEach
  public void setUp() {
    this.testReviewModel =
        new ReviewModel(
            "123",
            "456",
            "Eric Hodgson",
            5,
            "Great experience!",
            "Software Developer",
            "Summer",
            WorkTermValidator.getMaxYear());
  }

  @Test
  public void getterMethods_expectInitValues() {
    assertEquals("123", this.testReviewModel.getCompanyId());
    assertEquals("456", this.testReviewModel.getUserId());
    assertEquals("Eric Hodgson", this.testReviewModel.getAuthorName());
    assertEquals(5, this.testReviewModel.getRating());
    assertEquals("Great experience!", this.testReviewModel.getComment());
    assertEquals("Software Developer", this.testReviewModel.getJobTitle());
    assertEquals("Summer", this.testReviewModel.getWorkTermSeason());
    assertEquals(WorkTermValidator.getMaxYear(), this.testReviewModel.getWorkTermYear());
  }

  @Test
  public void constructor_expectTrimsWhitespace() {
    ReviewModel review =
        new ReviewModel(
            "  123  ",
            "  456  ",
            "  Eric Hodgson  ",
            ReviewConstants.MAX_RATING,
            "  Great!  ",
            "  Developer  ",
            "Summer",
            WorkTermValidator.getMaxYear());

    assertEquals("123", review.getCompanyId());
    assertEquals("456", review.getUserId());
    assertEquals("Eric Hodgson", review.getAuthorName());
    assertEquals("Great!", review.getComment());
    assertEquals("Developer", review.getJobTitle());
  }

  @Test
  public void constructor_whenNullComment_expectNull() {
    ReviewModel review =
        new ReviewModel(
            "123",
            "456",
            "Eric Hodgson",
            ReviewConstants.MAX_RATING,
            null,
            "Developer",
            "Summer",
            WorkTermValidator.getMaxYear());
    assertNull(review.getComment());
  }

  @Test
  public void constructor_whenNullCompanyId_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    null,
                    "456",
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals("Company ID cannot be null or empty", exception.getMessage());
  }

  @Test
  public void constructor_whenEmptyCompanyId_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "   ",
                    "456",
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals("Company ID cannot be null or empty", exception.getMessage());
  }

  @Test
  public void constructor_whenNullUserId_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    null,
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals("User ID cannot be null or empty", exception.getMessage());
  }

  @Test
  public void constructor_whenNullAuthorName_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    null,
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals("Author name cannot be null or empty", exception.getMessage());
  }

  @Test
  public void constructor_whenNullRating_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "Eric",
                    null,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals(
        "Rating must be between "
            + ReviewConstants.MIN_RATING
            + " and "
            + ReviewConstants.MAX_RATING,
        exception.getMessage());
  }

  @Test
  public void constructor_whenRatingTooLow_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "Eric",
                    ReviewConstants.MIN_RATING - 1,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals(
        "Rating must be between "
            + ReviewConstants.MIN_RATING
            + " and "
            + ReviewConstants.MAX_RATING,
        exception.getMessage());
  }

  @Test
  public void constructor_whenRatingTooHigh_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "Eric",
                    ReviewConstants.MAX_RATING + 1,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals(
        "Rating must be between "
            + ReviewConstants.MIN_RATING
            + " and "
            + ReviewConstants.MAX_RATING,
        exception.getMessage());
  }

  @Test
  public void constructor_whenNullJobTitle_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    null,
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals("Job title cannot be null or empty", exception.getMessage());
  }

  @Test
  public void constructor_whenInvalidSeason_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Spring",
                    WorkTermValidator.getMaxYear()));
    assertTrue(exception.getMessage().contains("Work term season must be one of"));
  }

  @Test
  public void constructor_whenYearTooLow_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMinYear() - 1));
    assertTrue(exception.getMessage().contains("Work term year must be between"));
  }

  @Test
  public void constructor_whenYearTooHigh_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear() + 1));
    assertTrue(exception.getMessage().contains("Work term year must be between"));
  }

  @Test
  public void constructor_whenValidSeasons_expectSuccess() {
    ReviewModel fall =
        new ReviewModel(
            "123",
            "456",
            "Eric",
            ReviewConstants.MAX_RATING,
            "Great!",
            "Developer",
            "Fall",
            WorkTermValidator.getMaxYear());
    assertEquals("Fall", fall.getWorkTermSeason());

    ReviewModel winter =
        new ReviewModel(
            "123",
            "456",
            "Eric",
            ReviewConstants.MAX_RATING,
            "Great!",
            "Developer",
            "Winter",
            WorkTermValidator.getMaxYear());
    assertEquals("Winter", winter.getWorkTermSeason());

    ReviewModel summer =
        new ReviewModel(
            "123",
            "456",
            "Eric",
            ReviewConstants.MAX_RATING,
            "Great!",
            "Developer",
            "Summer",
            WorkTermValidator.getMaxYear());
    assertEquals("Summer", summer.getWorkTermSeason());
  }

  @Test
  public void setCompanyId_expectOnlyCompanyIdChange() {
    this.testReviewModel.setCompanyId("newCompanyId");
    assertEquals("newCompanyId", this.testReviewModel.getCompanyId());
    assertEquals("456", this.testReviewModel.getUserId());
    assertEquals("Eric Hodgson", this.testReviewModel.getAuthorName());
  }

  @Test
  public void setCompanyId_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> this.testReviewModel.setCompanyId(null));
    assertEquals("Company ID cannot be null or empty", exception.getMessage());
  }

  @Test
  public void setUserId_expectOnlyUserIdChange() {
    this.testReviewModel.setUserId("newUserId");
    assertEquals("123", this.testReviewModel.getCompanyId());
    assertEquals("newUserId", this.testReviewModel.getUserId());
  }

  @Test
  public void setUserId_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> this.testReviewModel.setUserId(null));
    assertEquals("User ID cannot be null or empty", exception.getMessage());
  }

  @Test
  public void setAuthorName_expectOnlyAuthorNameChange() {
    this.testReviewModel.setAuthorName("Not Eric");
    assertEquals("Not Eric", this.testReviewModel.getAuthorName());
    assertEquals("456", this.testReviewModel.getUserId());
  }

  @Test
  public void setAuthorName_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> this.testReviewModel.setAuthorName(null));
    assertEquals("Author name cannot be null or empty", exception.getMessage());
  }

  @Test
  public void setRating_expectOnlyRatingChange() {
    this.testReviewModel.setRating(ReviewConstants.MAX_RATING - 1);
    assertEquals(ReviewConstants.MAX_RATING - 1, this.testReviewModel.getRating());
    assertEquals("Great experience!", this.testReviewModel.getComment());
  }

  @Test
  public void setRating_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> this.testReviewModel.setRating(null));
    assertTrue(exception.getMessage().contains("Rating must be between"));
  }

  @Test
  public void setRating_whenTooHigh_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> this.testReviewModel.setRating(ReviewConstants.MAX_RATING + 1));
    assertTrue(exception.getMessage().contains("Rating must be between"));
  }

  @Test
  public void setComment_expectOnlyCommentChange() {
    this.testReviewModel.setComment("Updated comment");
    assertEquals("Updated comment", this.testReviewModel.getComment());
    assertEquals(ReviewConstants.MAX_RATING, this.testReviewModel.getRating());
  }

  @Test
  public void setComment_whenNull_expectNull() {
    this.testReviewModel.setComment(null);
    assertNull(this.testReviewModel.getComment());
  }

  @Test
  public void setJobTitle_expectOnlyJobTitleChange() {
    this.testReviewModel.setJobTitle("Senior Developer");
    assertEquals("Senior Developer", this.testReviewModel.getJobTitle());
    assertEquals("Summer", this.testReviewModel.getWorkTermSeason());
  }

  @Test
  public void setJobTitle_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> this.testReviewModel.setJobTitle(null));
    assertEquals("Job title cannot be null or empty", exception.getMessage());
  }

  @Test
  public void setWorkTermSeason_expectOnlySeasonChange() {
    this.testReviewModel.setWorkTermSeason("Winter");
    assertEquals("Winter", this.testReviewModel.getWorkTermSeason());
    assertEquals(WorkTermValidator.getMaxYear(), this.testReviewModel.getWorkTermYear());
  }

  @Test
  public void setWorkTermSeason_whenInvalid_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> this.testReviewModel.setWorkTermSeason("Spring"));
    assertTrue(exception.getMessage().contains("Work term season must be one of"));
  }

  @Test
  public void setWorkTermYear_expectOnlyYearChange() {
    this.testReviewModel.setWorkTermYear(WorkTermValidator.getMaxYear() - 1);
    assertEquals(WorkTermValidator.getMaxYear() - 1, this.testReviewModel.getWorkTermYear());
    assertEquals("Summer", this.testReviewModel.getWorkTermSeason());
  }

  @Test
  public void setWorkTermYear_whenTooLow_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> this.testReviewModel.setWorkTermYear(WorkTermValidator.getMinYear() - 1));
    assertTrue(exception.getMessage().contains("Work term year must be between"));
  }

  @Test
  public void setId_expectIdChange() {
    this.testReviewModel.setId("111");
    assertEquals("111", this.testReviewModel.getId());
  }

  @Test
  public void getId_whenNoIdSet_expectNull() {
    assertNull(this.testReviewModel.getId());
  }

  @Test
  public void constructor_withMinRating_expectSuccess() {
    ReviewModel review =
        new ReviewModel(
            "123",
            "456",
            "Eric",
            ReviewConstants.MIN_RATING,
            "Okay",
            "Developer",
            "Summer",
            WorkTermValidator.getMaxYear());
    assertEquals(ReviewConstants.MIN_RATING, review.getRating());
  }

  @Test
  public void constructor_withMaxRating_expectSuccess() {
    ReviewModel review =
        new ReviewModel(
            "123",
            "456",
            "Eric",
            ReviewConstants.MAX_RATING,
            "Excellent!",
            "Developer",
            "Summer",
            WorkTermValidator.getMaxYear());
    assertEquals(ReviewConstants.MAX_RATING, review.getRating());
  }

  @Test
  public void setComment_whenEmptyString_expectTrimsToEmpty() {
    this.testReviewModel.setComment("   ");
    assertEquals("", this.testReviewModel.getComment());
  }

  @Test
  public void setId_whenNull_expectNull() {
    this.testReviewModel.setId(null);
    assertNull(this.testReviewModel.getId());
  }

  @Test
  public void constructor_whenEmptyStringsWithSpaces_expectThrowsException() {
    Exception exception1 =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "   ",
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals("User ID cannot be null or empty", exception1.getMessage());

    Exception exception2 =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "   ",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals("Author name cannot be null or empty", exception2.getMessage());

    // Test empty jobTitle
    Exception exception3 =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new ReviewModel(
                    "123",
                    "456",
                    "Eric",
                    ReviewConstants.MAX_RATING,
                    "Great!",
                    "   ",
                    "Summer",
                    WorkTermValidator.getMaxYear()));
    assertEquals("Job title cannot be null or empty", exception3.getMessage());
  }

  @Test
  public void setUserId_whenEmptyString_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> this.testReviewModel.setUserId("   "));
    assertEquals("User ID cannot be null or empty", exception.getMessage());
  }

  @Test
  public void setAuthorName_whenEmptyString_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> this.testReviewModel.setAuthorName("   "));
    assertEquals("Author name cannot be null or empty", exception.getMessage());
  }

  @Test
  public void setJobTitle_whenEmptyString_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> this.testReviewModel.setJobTitle("   "));
    assertEquals("Job title cannot be null or empty", exception.getMessage());
  }

  @Test
  public void setCompanyId_whenEmptyString_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> this.testReviewModel.setCompanyId("   "));
    assertEquals("Company ID cannot be null or empty", exception.getMessage());
  }

  @Test
  public void setRating_whenBelowMin_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> this.testReviewModel.setRating(ReviewConstants.MIN_RATING - 1));
    assertTrue(exception.getMessage().contains("Rating must be between"));
  }
}
