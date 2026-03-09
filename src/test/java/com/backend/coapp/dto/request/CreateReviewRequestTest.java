package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
public class CreateReviewRequestTest {

  @Test
  public void getMethods_expectInitValues() {
    CreateReviewRequest request =
        new CreateReviewRequest(5, "Great company", "Developer", "Summer", 2024);

    assertEquals(5, request.getRating());
    assertEquals("Great company", request.getComment());
    assertEquals("Developer", request.getJobTitle());
    assertEquals("Summer", request.getWorkTermSeason());
    assertEquals(2024, request.getWorkTermYear());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    CreateReviewRequest request =
        new CreateReviewRequest(5, "Great company", "Developer", "Summer", 2024);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenValidRequestWithoutComment_expectNoException() {
    CreateReviewRequest request = new CreateReviewRequest(5, null, "Developer", "Summer", 2024);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenRatingNull_expectException() {
    CreateReviewRequest request =
        new CreateReviewRequest(null, "Great", "Developer", "Summer", 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenRatingTooLow_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(0, "Great", "Developer", "Summer", 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenRatingTooHigh_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(6, "Great", "Developer", "Summer", 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenCommentTooLong_expectException() {
    String longComment = "a".repeat(2001);
    CreateReviewRequest request =
        new CreateReviewRequest(5, longComment, "Developer", "Summer", 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenJobTitleNull_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(5, "Great", null, "Summer", 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenJobTitleBlank_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(5, "Great", "   ", "Summer", 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenSeasonNull_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(5, "Great", "Developer", null, 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenSeasonInvalid_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(5, "Great", "Developer", "Spring", 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenYearNull_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(5, "Great", "Developer", "Summer", null);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenYearTooLow_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(5, "Great", "Developer", "Summer", 1949);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenYearTooHigh_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(5, "Great", "Developer", "Summer", 2030);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  public void validateRequest_whenSeasonBlank_expectException() {
    CreateReviewRequest request = new CreateReviewRequest(5, "Great", "Developer", "   ", 2024);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }
}
