package com.backend.coapp.handler;

import com.backend.coapp.exception.application.*;
import com.backend.coapp.exception.auth.*;
import com.backend.coapp.exception.company.CompanyAlreadyExistsException;
import com.backend.coapp.exception.company.CompanyNotFoundException;
import com.backend.coapp.exception.company.CompanyServiceFailException;
import com.backend.coapp.exception.company.InvalidWebsiteException;
import com.backend.coapp.exception.genai.*;
import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.exception.review.ReviewAlreadyExistsException;
import com.backend.coapp.exception.review.ReviewNotFoundException;
import com.backend.coapp.exception.review.ReviewServiceFailException;
import com.backend.coapp.model.enumeration.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Exception handler for controller.
 *
 * <p>Internal exceptions (5** HTTP status) will be logged for debugging.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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

  @ExceptionHandler(AuthEmailNotRegisteredException.class)
  public ResponseEntity<Map<String, Object>> handleEmailNotRegistered(
      AuthEmailNotRegisteredException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", AuthErrorCode.EMAIL_NOT_REGISTERED, "message", ex.getMessage()));
  }

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

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
    String errorMessage = "ERROR: Runtime exception: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "Internal failure. Please try again later."));
  }

  @ExceptionHandler(EmailInvalidAddressException.class)
  public ResponseEntity<Map<String, Object>> handleEmailInvalidAddressException(
      EmailInvalidAddressException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", AuthErrorCode.INVALID_EMAIL, "message", ex.getMessage()));
  }

  @ExceptionHandler(AuthEmailAlreadyUsedException.class)
  public ResponseEntity<Map<String, Object>> handleAuthEmailAlreadyUsedException(
      AuthEmailAlreadyUsedException ex) {
    return ResponseEntity.status(409)
        .body(Map.of("error", AuthErrorCode.EXIST_ACCOUNT_WITH_EMAIL, "message", ex.getMessage()));
  }

  @ExceptionHandler(AuthAccountNotYetActivatedException.class)
  public ResponseEntity<Map<String, Object>> handleAuthAccountNotYetActivatedException(
      AuthAccountNotYetActivatedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", AuthErrorCode.ACCOUNT_NOT_ACTIVATED, "message", ex.getMessage()));
  }

  @ExceptionHandler(IncorrectCodeException.class)
  public ResponseEntity<Map<String, Object>> handleIncorrectCodeException(
      IncorrectCodeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", AuthErrorCode.INVALID_CONFIRMATION_CODE, "message", ex.getMessage()));
  }

  @ExceptionHandler(AuthAccountAlreadyVerifyException.class)
  public ResponseEntity<Map<String, Object>> handleAuthAccountAlreadyVerifyException(
      AuthAccountAlreadyVerifyException ex) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(Map.of("error", AuthErrorCode.ACCOUNT_ALREADY_VERIFIED, "message", ex.getMessage()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(
      AuthenticationException ex) {
    String errorMessage = "ERROR: Authentication Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "Authentication failed. Please try again later."));
  }

  @ExceptionHandler(AuthBadCredentialException.class)
  public ResponseEntity<Map<String, Object>> handleAuthBadCredentialException(
      AuthBadCredentialException ex) {

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", AuthErrorCode.INVALID_EMAIL_OR_PASSWORD, "message", ex.getMessage()));
  }

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

  @ExceptionHandler(CompanyAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleCompanyAlreadyExistsException(
      CompanyAlreadyExistsException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", CompanyErrorCode.COMPANY_ALREADY_EXISTS);
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(CompanyNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleCompanyNotFoundException(
      CompanyNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", CompanyErrorCode.COMPANY_NOT_FOUND, "message", ex.getMessage()));
  }

  @ExceptionHandler(InvalidWebsiteException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidWebsiteException(
      InvalidWebsiteException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", CompanyErrorCode.INVALID_WEBSITE, "message", ex.getMessage()));
  }

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

  @ExceptionHandler(DuplicateApplicationException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateApplicationException(
      DuplicateApplicationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            Map.of(
                "error", ApplicationErrorCode.DUPLICATE_APPLICATION, "message", ex.getMessage()));
  }

  @ExceptionHandler(ApplicationNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleApplicationNotFoundException(
      ApplicationNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            Map.of(
                "error", ApplicationErrorCode.APPLICATION_NOT_FOUND, "message", ex.getMessage()));
  }

  @ExceptionHandler(UnauthorizedApplicationAccessException.class)
  public ResponseEntity<Map<String, Object>> handleUnauthorizedApplicationAccessException(
      UnauthorizedApplicationAccessException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            Map.of(
                "error",
                ApplicationErrorCode.UNAUTHORIZED_APPLICATION_ACCESS,
                "message",
                ex.getMessage()));
  }

  @ExceptionHandler(ApplicationServiceFailException.class)
  public ResponseEntity<Map<String, Object>> handleApplicationServiceFailException(
      ApplicationServiceFailException ex) {
    String errorMessage = "ERROR: Application Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "An unexpected error occurred while processing your application."));
  }

  @ExceptionHandler(ReviewAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleReviewAlreadyExistsException(
      ReviewAlreadyExistsException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", ReviewErrorCode.REVIEW_ALREADY_EXISTS);
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(ReviewNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleReviewNotFoundException(
      ReviewNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", ReviewErrorCode.REVIEW_NOT_FOUND, "message", ex.getMessage()));
  }

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

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleUserNotExitException(UserNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", UserErrorCode.USER_NOT_EXIST, "message", ex.getMessage()));
  }

  @ExceptionHandler(UserInvalidPasswordChangeException.class)
  public ResponseEntity<Map<String, Object>> handleUserUpdateSamePasswordException(
      UserInvalidPasswordChangeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "error",
                UserErrorCode.NEW_PASSWORD_SAME_WITH_OLD_PASSWORD,
                "message",
                ex.getMessage()));
  }

  @ExceptionHandler(NoChangesDetectedException.class)
  public ResponseEntity<Map<String, Object>> handleNoChangesDetectedException(
      NoChangesDetectedException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "error",
                ApplicationErrorCode.NO_CHANGE_DETECTED_TO_UPDATE,
                "message",
                ex.getMessage()));
  }

  @ExceptionHandler(OverCharacterLimitException.class)
  public ResponseEntity<Map<String, Object>> handleOverCharacterLimitException(
      OverCharacterLimitException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", GenAIErrorCode.OVER_LIMIT_CHARACTER, "message", ex.getMessage()));
  }

  @ExceptionHandler(ApplicationNotOwnedException.class)
  public ResponseEntity<Map<String, Object>> handleApplicationNotOwnedException(
      ApplicationNotOwnedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            Map.of("error", ApplicationErrorCode.APPLICATION_NOT_OWN, "message", ex.getMessage()));
  }

  @ExceptionHandler(GenAIQuotaExceededException.class)
  public ResponseEntity<Map<String, Object>> handleGenAIQuotaExceededException(
      GenAIQuotaExceededException ex) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .body(
            Map.of("error", GenAIErrorCode.OVER_LIMIT_CHATBOT_REQUEST, "message", ex.getMessage()));
  }

  @ExceptionHandler(ConcurrencyException.class)
  public ResponseEntity<Map<String, Object>> handleConcurrencyException(ConcurrencyException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            Map.of("error", GenAIErrorCode.OTHER_REQUEST_IN_PROGRESS, "message", ex.getMessage()));
  }

  @ExceptionHandler(GenAIUsageManagementServiceException.class)
  public ResponseEntity<Map<String, Object>> handleGenAIUsageManagementServiceException(
      GenAIUsageManagementServiceException ex) {
    String errorMessage = "ERROR: GenAI Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "An unexpected error occurred while processing your request. Please try again later."));
  }

  @ExceptionHandler(ExperienceNotOwnedException.class)
  public ResponseEntity<Map<String, Object>> handleExperienceNotOwnedException(
      ExperienceNotOwnedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            Map.of(
                "error", UserExperienceErrorCode.EXPERIENCE_NOT_OWN, "message", ex.getMessage()));
  }

  @ExceptionHandler(ExperienceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleExperienceNotFoundException(
      ExperienceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            Map.of(
                "error", UserExperienceErrorCode.EXPERIENCE_NOT_FOUND, "message", ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e) {

    String message = "Invalid request body.";

    Throwable cause = e.getCause();
    if (cause instanceof InvalidFormatException invalidFormatException) {

      String fieldName = invalidFormatException.getPath().get(0).getPropertyName();
      Class<?> targetType = invalidFormatException.getTargetType();

      if (targetType == LocalDate.class) {
        message =
            "Invalid date format for field '%s'. Expected format: yyyy-MM-dd.".formatted(fieldName);
      } else {
        message = "Invalid value for field '%s'.".formatted(fieldName);
      }
    }

    return ResponseEntity.badRequest()
        .body(Map.of("error", RequestErrorCode.INVALID_FORMAT_FIELD.name(), "message", message));
  }

  @ExceptionHandler(GenAIOutOfServiceException.class)
  public ResponseEntity<Map<String, Object>> handleGenAIOutOfServiceException(
      GenAIOutOfServiceException ex) {

    String errorMessage = "ERROR: Resume workshop Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(
            Map.of(
                "error",
                GenAIErrorCode.SERVICE_UNAVAILABLE,
                "message",
                "Our AI service is currently unavailable. Please try again later."));
  }

  @ExceptionHandler(GenAIServiceException.class)
  public ResponseEntity<Map<String, Object>> handleGenAIServiceException(GenAIServiceException ex) {

    String errorMessage = "ERROR: GenAI Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "error",
                SystemErrorCode.INTERNAL_ERROR,
                "message",
                "GenAI Service failed. Please try again later."));
  }
}
