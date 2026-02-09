package com.backend.coapp.model.document;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.util.ReviewConstants;
import com.backend.coapp.util.WorkTermValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for ReviewModel */
public class ReviewModelTest {

  private static ValidatorFactory validatorFactory;
  private static Validator validator;
  private ReviewModel validReview;

  @BeforeAll
  public static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  public static void tearDownValidator() {
    if (validatorFactory != null) {
      validatorFactory.close();
    }
  }

  @BeforeEach
  public void setUp() {
    this.validReview =
        new ReviewModel(
            "company123",
            "user456",
            "John Doe",
            ReviewConstants.MAX_RATING - 1,
            "Great company",
            "Software Developer",
            "Fall",
            WorkTermValidator.getMaxYear() - 2);
  }

  /* test constructor */

  @Test
  public void constructor_whenAllValid_expectSuccess() {
    ReviewModel review =
        new ReviewModel(
            "company1",
            "user1",
            "Jane Doe",
            ReviewConstants.MAX_RATING - 1,
            "Excellent",
            "Engineer",
            "Winter",
            WorkTermValidator.getMaxYear() - 1);

    assertNotNull(review);
    assertEquals("company1", review.getCompanyId());
    assertEquals("user1", review.getUserId());
    assertEquals("Jane Doe", review.getAuthorName());
    assertEquals(ReviewConstants.MAX_RATING - 1, review.getRating());
    assertEquals("Winter", review.getWorkTermSeason());
    assertEquals(WorkTermValidator.getMaxYear() - 1, review.getWorkTermYear());
  }

  @Test
  public void constructor_whenTrimsWhitespace_expectTrimmed() {
    ReviewModel review =
        new ReviewModel(
            "  company1  ",
            "  user1  ",
            "  Jane  ",
            ReviewConstants.MAX_RATING,
            "  Good work  ",
            "  Dev  ",
            "Fall",
            WorkTermValidator.getMaxYear() - 1);

    assertEquals("company1", review.getCompanyId());
    assertEquals("user1", review.getUserId());
    assertEquals("Jane", review.getAuthorName());
    assertEquals("Good work", review.getComment());
    assertEquals("Dev", review.getJobTitle());
  }

  @Test
  public void constructor_whenNullComment_expectNull() {
    ReviewModel review =
        new ReviewModel(
            "company1",
            "user1",
            "Jane",
            ReviewConstants.MAX_RATING,
            null,
            "Dev",
            "Fall",
            WorkTermValidator.getMaxYear() - 1);

    assertNull(review.getComment());
  }

  @Test
  public void constructor_whenInvalidSeason_expectThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ReviewModel(
                "company1",
                "user1",
                "Jane",
                ReviewConstants.MAX_RATING,
                "Good",
                "Dev",
                "Spring",
                WorkTermValidator.getMaxYear() - 1));
  }

  @Test
  public void constructor_whenInvalidYear_expectThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ReviewModel(
                "company1",
                "user1",
                "Jane",
                ReviewConstants.MAX_RATING,
                "Good",
                "Dev",
                "Fall",
                WorkTermValidator.getMinYear() - 1));
  }

  /* test jakarta validation annotations with a validator */

  @Test
  public void validate_whenAllFieldsValid_expectNoViolations() {
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void validate_whenCompanyIdNull_expectViolation() {
    validReview.setCompanyId(null);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Company ID cannot be empty")));
  }

  @Test
  public void validate_whenCompanyIdBlank_expectViolation() {
    validReview.setCompanyId("   ");
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenUserIdNull_expectViolation() {
    validReview.setUserId(null);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("User ID cannot be empty")));
  }

  @Test
  public void validate_whenUserIdBlank_expectViolation() {
    validReview.setUserId("");
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenAuthorNameNull_expectViolation() {
    validReview.setAuthorName(null);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenAuthorNameBlank_expectViolation() {
    validReview.setAuthorName("");
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenRatingNull_expectViolation() {
    validReview.setRating(null);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Rating cannot be null")));
  }

  @Test
  public void validate_whenRatingTooLow_expectViolation() {
    validReview.setRating(ReviewConstants.MIN_RATING - 1);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream()
            .anyMatch(violation -> violation.getMessage().contains("Rating must be at least")));
  }

  @Test
  public void validate_whenRatingTooHigh_expectViolation() {
    validReview.setRating(6);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenJobTitleNull_expectViolation() {
    validReview.setJobTitle(null);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenJobTitleBlank_expectViolation() {
    validReview.setJobTitle("");
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenCommentTooLong_expectViolation() {
    String longComment = "a".repeat(ReviewConstants.MAX_COMMENT_LENGTH + 1);
    validReview.setComment(longComment);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenCommentNull_expectNoViolation() {
    validReview.setComment(null);
    Set<ConstraintViolation<ReviewModel>> violations = validator.validate(validReview);

    assertTrue(violations.isEmpty());
  }

  /* custom setters */

  @Test
  public void setWorkTermSeason_whenValid_expectSuccess() {
    validReview.setWorkTermSeason("Summer");
    assertEquals("Summer", validReview.getWorkTermSeason());
  }

  @Test
  public void setWorkTermSeason_whenInvalid_expectThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> validReview.setWorkTermSeason("Spring"));
  }

  @Test
  public void setWorkTermYear_whenValid_expectSuccess() {
    validReview.setWorkTermYear(WorkTermValidator.getMaxYear());
    assertEquals(WorkTermValidator.getMaxYear(), validReview.getWorkTermYear());
  }

  @Test
  public void setWorkTermYear_whenTooLow_expectThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validReview.setWorkTermYear(WorkTermValidator.getMinYear() - 1));
  }

  @Test
  public void setWorkTermYear_whenTooHigh_expectThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validReview.setWorkTermYear(WorkTermValidator.getMaxYear() + 1));
  }

  /* lombok setters / getters */

  @Test
  public void lombokSetters_expectWork() {
    ReviewModel review = new ReviewModel();
    review.setCompanyId("comp1");
    review.setUserId("user1");
    review.setAuthorName("Jane");
    review.setRating(ReviewConstants.MAX_RATING - 2);
    review.setComment("Good");
    review.setJobTitle("Developer");

    assertEquals("comp1", review.getCompanyId());
    assertEquals("user1", review.getUserId());
    assertEquals("Jane", review.getAuthorName());
    assertEquals(ReviewConstants.MAX_RATING - 2, review.getRating());
    assertEquals("Good", review.getComment());
    assertEquals("Developer", review.getJobTitle());
  }

  @Test
  public void lombokGetters_expectWork() {
    assertNotNull(validReview.getCompanyId());
    assertNotNull(validReview.getUserId());
    assertNotNull(validReview.getAuthorName());
    assertNotNull(validReview.getRating());
  }
}
