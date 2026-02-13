package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.*;
import com.backend.coapp.handler.GlobalExceptionHandler;
import com.backend.coapp.model.enumeration.ReviewErrorCodeEnum;
import com.backend.coapp.model.enumeration.SystemErrorCodeEnum;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Temporary test file to cover GlobalExceptionHandler review exception handlers.
 * TODO: Replace with proper ReviewController integration tests when controller is implemented.
 */
public class ReviewControllerTest {

  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  public void setUp() {
    this.exceptionHandler = new GlobalExceptionHandler();
  }

  @Test
  public void handleReviewAlreadyExistsException_expectConflictResponse() {
    ReviewAlreadyExistsException ex = new ReviewAlreadyExistsException();

    ResponseEntity<Map<String, Object>> response =
      this.exceptionHandler.handleReviewAlreadyExistsException(ex);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(ReviewErrorCodeEnum.REVIEW_ALREADY_EXISTS, response.getBody().get("error"));
    assertTrue(response.getBody().get("message").toString().contains("already submitted a review"));
  }

  @Test
  public void handleReviewNotFoundException_expectNotFoundResponse() {
    ReviewNotFoundException ex = new ReviewNotFoundException();

    ResponseEntity<Map<String, Object>> response =
      this.exceptionHandler.handleReviewNotFoundException(ex);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(ReviewErrorCodeEnum.REVIEW_NOT_FOUND, response.getBody().get("error"));
    assertTrue(response.getBody().get("message").toString().contains("does not exist"));
  }

  @Test
  public void handleReviewNotOwnedException_expectForbiddenResponse() {
    ReviewNotOwnedException ex = new ReviewNotOwnedException("update");

    ResponseEntity<Map<String, Object>> response =
      this.exceptionHandler.handleReviewNotOwnedException(ex);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(ReviewErrorCodeEnum.REVIEW_NOT_OWNED, response.getBody().get("error"));
    assertTrue(response.getBody().get("message").toString().contains("your own reviews"));
  }

  @Test
  public void handleReviewServiceFailException_expectInternalServerErrorResponse() {
    ReviewServiceFailException ex = new ReviewServiceFailException("Database connection failed");

    ResponseEntity<Map<String, Object>> response =
      this.exceptionHandler.handleReviewServiceFailException(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(SystemErrorCodeEnum.INTERNAL_ERROR, response.getBody().get("error"));
    assertTrue(
      response.getBody().get("message").toString().contains("unexpected error occurred"));
  }
}
