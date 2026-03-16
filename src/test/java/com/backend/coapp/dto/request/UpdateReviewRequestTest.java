package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.global.InvalidRequestException;
import org.junit.jupiter.api.Test;

class UpdateReviewRequestTest {

  @Test
  void getMethods_expectInitValues() {
    UpdateReviewRequest request =
        new UpdateReviewRequest(4, "Updated comment", "Engineer", "Fall", 2023);

    assertEquals(4, request.getRating());
    assertEquals("Updated comment", request.getComment());
    assertEquals("Engineer", request.getJobTitle());
    assertEquals("Fall", request.getWorkTermSeason());
    assertEquals(2023, request.getWorkTermYear());
  }

  @Test
  void validateRequest_whenAllFieldsProvided_expectNoException() {
    UpdateReviewRequest request = new UpdateReviewRequest(4, "Updated", "Engineer", "Fall", 2023);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenOnlyRatingProvided_expectNoException() {
    UpdateReviewRequest request = new UpdateReviewRequest(4, null, null, null, null);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenOnlyCommentProvided_expectNoException() {
    UpdateReviewRequest request = new UpdateReviewRequest(null, "Updated", null, null, null);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenNoFieldsProvided_expectException() {
    UpdateReviewRequest request = new UpdateReviewRequest(null, null, null, null, null);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRatingTooLow_expectException() {
    UpdateReviewRequest request = new UpdateReviewRequest(0, null, null, null, null);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRatingTooHigh_expectException() {
    UpdateReviewRequest request = new UpdateReviewRequest(6, null, null, null, null);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenCommentTooLong_expectException() {
    String longComment = "a".repeat(2001);
    UpdateReviewRequest request = new UpdateReviewRequest(null, longComment, null, null, null);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenSeasonInvalid_expectException() {
    UpdateReviewRequest request = new UpdateReviewRequest(null, null, null, "Spring", null);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenYearTooLow_expectException() {
    UpdateReviewRequest request = new UpdateReviewRequest(null, null, null, null, 1949);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenYearTooHigh_expectException() {
    UpdateReviewRequest request = new UpdateReviewRequest(null, null, null, null, 2030);
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenOnlyJobTitleProvided_expectNoException() {
    UpdateReviewRequest request = new UpdateReviewRequest(null, null, "Engineer", null, null);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenOnlySeasonProvided_expectNoException() {
    UpdateReviewRequest request = new UpdateReviewRequest(null, null, null, "Fall", null);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenOnlyYearProvided_expectNoException() {
    UpdateReviewRequest request = new UpdateReviewRequest(null, null, null, null, 2024);
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenSeasonBlank_expectNoException() {
    // Blank season should be treated as "no update" for this field
    UpdateReviewRequest request = new UpdateReviewRequest(4, null, null, "   ", null);
    assertDoesNotThrow(request::validateRequest);
  }
}
