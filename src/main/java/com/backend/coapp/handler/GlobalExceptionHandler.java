package com.backend.coapp.handler;

import com.backend.coapp.exception.*;
import com.backend.coapp.model.enumeration.*;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception handler for controller. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles invalid requests with missing or empty required fields
   *
   * @param ex Caught InvalidRequestException
   * @return HTTP 400 response with error code {@code REQUEST_HAS_NULL_OR_EMPTY_FIELD}
   */
  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "error",
                RequestErrorCode.REQUEST_HAS_NULL_OR_EMPTY_FIELD,
                "message",
                ex.getMessage()));
  }

  /**
   * Handles authentication attempts with unregistered email
   *
   * @param ex Caught AuthEmailNotRegisteredException
   * @return HTTP 400 response with error code {@code EMAIL_NOT_REGISTERED}
   */
  @ExceptionHandler(AuthEmailNotRegisteredException.class)
  public ResponseEntity<Map<String, Object>> handleEmailNotRegistered(
      AuthEmailNotRegisteredException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", AuthErrorCode.EMAIL_NOT_REGISTERED, "message", ex.getMessage()));
  }

  /**
   * Handles failures in email service operations Logs detailed error and returns generic message to
   * client
   *
   * @param ex Caught EmailServiceException
   * @return HTTP 500 response with error code {@code INTERNAL_ERROR}
   */
  @ExceptionHandler(EmailServiceException.class)
  public ResponseEntity<Map<String, Object>> handleEmailServiceException(EmailServiceException ex) {
    String errorMessage = "ERROR: Email service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "Unable to send verification email. Please try again later."));
  }

  /**
   * Handles uncaught runtime exceptions Acts as fallback for unexpected errors. Logs detailed
   * stacktrace.
   *
   * @param ex Caught RuntimeException
   * @return HTTP 500 response with error code {@code INTERNAL_ERROR}
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
    String errorMessage = "ERROR: Reset verification code service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "Internal failure. Please try again later."));
  }

  /**
   * Handles malformed email addresses
   *
   * @param ex Caught EmailInvalidAddressException
   * @return HTTP 400 response with error code {@code INVALID_EMAIL}
   */
  @ExceptionHandler(EmailInvalidAddressException.class)
  public ResponseEntity<Map<String, Object>> handleEmailInvalidAddressException(
      EmailInvalidAddressException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", AuthErrorCode.INVALID_EMAIL, "message", ex.getMessage()));
  }

  /**
   * Handles duplicate email registration attempts
   *
   * @param ex Caught AuthEmailAlreadyUsedException
   * @return HTTP 409 response with error code {@code EXIST_ACCOUNT_WITH_EMAIL}
   */
  @ExceptionHandler(AuthEmailAlreadyUsedException.class)
  public ResponseEntity<Map<String, Object>> handleAuthEmailAlreadyUsedException(
      AuthEmailAlreadyUsedException ex) {
    return ResponseEntity.status(409)
        .body(Map.of("error", AuthErrorCode.EXIST_ACCOUNT_WITH_EMAIL, "message", ex.getMessage()));
  }

  /**
   * Handles access attempts for non-activated accounts
   *
   * @param ex Caught AuthAccountNotYetActivatedException
   * @return HTTP 401 response with error code {@code ACCOUNT_NOT_ACTIVATED}
   */
  @ExceptionHandler(AuthAccountNotYetActivatedException.class)
  public ResponseEntity<Map<String, Object>> handleAuthAccountNotYetActivatedException(
      AuthAccountNotYetActivatedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", AuthErrorCode.ACCOUNT_NOT_ACTIVATED, "message", ex.getMessage()));
  }

  /**
   * Handles invalid verification/confirmation codes
   *
   * @param ex Caught IncorrectCodeException
   * @return HTTP 400 response with error code {@code INVALID_CONFIRMATION_CODE}
   */
  @ExceptionHandler(IncorrectCodeException.class)
  public ResponseEntity<Map<String, Object>> handleIncorrectCodeException(
      IncorrectCodeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", AuthErrorCode.INVALID_CONFIRMATION_CODE, "message", ex.getMessage()));
  }

  /**
   * Handles redundant account verification attempts
   *
   * @param ex Caught AuthAccountAlreadyVerifyException
   * @return HTTP 405 response with error code {@code ACCOUNT_ALREADY_VERIFIED}
   */
  @ExceptionHandler(AuthAccountAlreadyVerifyException.class)
  public ResponseEntity<Map<String, Object>> handleAuthAccountAlreadyVerifyException(
      AuthAccountAlreadyVerifyException ex) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(Map.of("error", AuthErrorCode.ACCOUNT_ALREADY_VERIFIED, "message", ex.getMessage()));
  }

  /**
   * Handles Spring Security authentication failures Logs error details and returns generic message
   *
   * @param ex Caught AuthenticationException
   * @return HTTP 500 response with error code {@code INTERNAL_ERROR}
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(
      AuthenticationException ex) {
    String errorMessage = "ERROR: JWT Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "Authentication failed. Please try again later."));
  }

  /**
   * Handles invalid email/password combinations
   *
   * @param ex Caught AuthBadCredentialException
   * @return HTTP 401 response with error code {@code INVALID_EMAIL_OR_PASSWORD}
   */
  @ExceptionHandler(AuthBadCredentialException.class)
  public ResponseEntity<Map<String, Object>> handleAuthBadCredentialException(
      AuthBadCredentialException ex) {

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", AuthErrorCode.INVALID_EMAIL_OR_PASSWORD, "message", ex.getMessage()));
  }

  /**
   * Handles JWT processing failures Logs detailed error and returns generic response
   *
   * @param ex Caught JwtServiceFailException
   * @return HTTP 500 response with error code {@code INTERNAL_ERROR}
   */
  @ExceptionHandler(JwtServiceFailException.class)
  public ResponseEntity<Map<String, Object>> handleJwtServiceFailException(
      JwtServiceFailException ex) {
    String errorMessage = "ERROR: JWT Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "Authentication failed. Please try again."));
  }

  /**
   * Handles user service failures Logs error and returns generic failure message
   *
   * @param ex Caught UserServiceFailException
   * @return HTTP 500 response with error code {@code INTERNAL_ERROR}
   */
  @ExceptionHandler(UserServiceFailException.class)
  public ResponseEntity<Map<String, Object>> handleUserServiceFailException(
      UserServiceFailException ex) {
    String errorMessage = "ERROR: User Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "User service fail. Please try again."));
  }

  /**
   * Handles duplicate company creation attempts
   *
   * @param ex Caught CompanyAlreadyExistsException
   * @return HTTP 409 response with error code {@code COMPANY_ALREADY_EXISTS}
   */
  @ExceptionHandler(CompanyAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleCompanyAlreadyExistsException(
      CompanyAlreadyExistsException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", CompanyErrorCode.COMPANY_ALREADY_EXISTS);
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  /**
   * Handles requests for non-existent companies
   *
   * @param ex Caught CompanyNotFoundException
   * @return HTTP 404 response with error code {@code COMPANY_NOT_FOUND}
   */
  @ExceptionHandler(CompanyNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleCompanyNotFoundException(
      CompanyNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", CompanyErrorCode.COMPANY_NOT_FOUND, "message", ex.getMessage()));
  }

  /**
   * Handles invalid website URL formats
   *
   * @param ex Caught InvalidWebsiteException
   * @return HTTP 400 response with error code {@code INVALID_WEBSITE}
   */
  @ExceptionHandler(InvalidWebsiteException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidWebsiteException(
      InvalidWebsiteException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", CompanyErrorCode.INVALID_WEBSITE, "message", ex.getMessage()));
  }

  /**
   * Handles company service failures Logs error and returns generic failure message
   *
   * @param ex Caught CompanyServiceFailException
   * @return HTTP 500 response with error code {@code INTERNAL_ERROR}
   */
  @ExceptionHandler(CompanyServiceFailException.class)
  public ResponseEntity<Map<String, Object>> handleCompanyServiceFailException(
      CompanyServiceFailException ex) {
    String errorMessage = "ERROR: Company Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "An unexpected error occurred while processing your request."));
  }

  /**
   * Handles duplicate review submissions
   *
   * @param ex Caught ReviewAlreadyExistsException
   * @return HTTP 409 response with error code {@code REVIEW_ALREADY_EXISTS}
   */
  @ExceptionHandler(ReviewAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleReviewAlreadyExistsException(
      ReviewAlreadyExistsException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", ReviewErrorCode.REVIEW_ALREADY_EXISTS);
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  /**
   * Handles requests for non-existent reviews
   *
   * @param ex Caught ReviewNotFoundException
   * @return HTTP 404 response with error code {@code REVIEW_NOT_FOUND}
   */
  @ExceptionHandler(ReviewNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleReviewNotFoundException(
      ReviewNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", ReviewErrorCode.REVIEW_NOT_FOUND, "message", ex.getMessage()));
  }

  /**
   * Handles unauthorized review modification attempts
   *
   * @param ex Caught ReviewNotOwnedException
   * @return HTTP 403 response with error code {@code REVIEW_NOT_OWNED}
   */
  @ExceptionHandler(ReviewNotOwnedException.class)
  public ResponseEntity<Map<String, Object>> handleReviewNotOwnedException(
      ReviewNotOwnedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("error", ReviewErrorCode.REVIEW_NOT_OWNED, "message", ex.getMessage()));
  }

  /**
   * Handles review service failures Logs error and returns generic failure message
   *
   * @param ex Caught ReviewServiceFailException
   * @return HTTP 500 response with error code {@code INTERNAL_ERROR}
   */
  @ExceptionHandler(ReviewServiceFailException.class)
  public ResponseEntity<Map<String, Object>> handleReviewServiceFailException(
      ReviewServiceFailException ex) {
    String errorMessage = "ERROR: Review Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "An unexpected error occurred while processing your review."));
  }

  /**
   * Handles requests referencing non-existent users
   *
   * @param ex Caught UserNotExistException
   * @return HTTP 400 response with error code {@code USER_NOT_EXIST}
   */
  @ExceptionHandler(UserNotExistException.class)
  public ResponseEntity<Map<String, Object>> handleUserNotExitException(UserNotExistException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", UserErrorCode.USER_NOT_EXIST, "message", ex.getMessage()));
  }

  /**
   * Handles malformed JSON requests
   *
   * @param ex Caught HttpMessageNotReadableException
   * @return HTTP 400 response with error code {@code REQUEST_HAS_NULL_OR_EMPTY_FIELD}
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
      HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "error",
                RequestErrorCode.REQUEST_HAS_NULL_OR_EMPTY_FIELD,
                "message",
                "Request body is missing or malformed."));
  }

  /**
   * Handles illegal argument exceptions Catches miscellaneous validation failures not covered by
   * specific handlers
   *
   * @param ex Caught IllegalArgumentException
   * @return HTTP 400 response with error code "BAD_REQUEST"
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", "BAD_REQUEST", "message", ex.getMessage()));
  }
}
