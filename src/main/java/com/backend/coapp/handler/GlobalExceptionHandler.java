package com.backend.coapp.handler;

import com.backend.coapp.exception.*;
import com.backend.coapp.model.enumeration.AuthErrorCodeEnum;
import com.backend.coapp.model.enumeration.RequestErrorCodeEnum;
import com.backend.coapp.model.enumeration.SystemErrorCodeEnum;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception handler for controller. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException ex) {
    return ResponseEntity.status(400)
        .body(
            Map.of(
                "error",
                RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD,
                "message",
                ex.getMessage()));
  }

  @ExceptionHandler(AuthEmailNotRegisteredException.class)
  public ResponseEntity<Map<String, Object>> handleEmailNotRegistered(
      AuthEmailNotRegisteredException ex) {
    return ResponseEntity.status(400)
        .body(Map.of("error", AuthErrorCodeEnum.EMAIL_NOT_REGISTERED, "message", ex.getMessage()));
  }

  @ExceptionHandler(EmailServiceException.class)
  public ResponseEntity<Map<String, Object>> handleEmailServiceException(EmailServiceException ex) {
    String errorMessage = "ERROR: Email service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(500)
        .body(
            Map.of(
                "error",
                SystemErrorCodeEnum.INTERNAL_ERROR,
                "message",
                "Unable to send verification email. Please try again later."));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
    String errorMessage = "ERROR: Reset verification code service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(500)
        .body(
            Map.of(
                "error",
                SystemErrorCodeEnum.INTERNAL_ERROR,
                "message",
                "Internal failure. Please try again later."));
  }

  @ExceptionHandler(EmailInvalidAddressException.class)
  public ResponseEntity<Map<String, Object>> handleEmailInvalidAddressException(
      EmailInvalidAddressException ex) {
    return ResponseEntity.status(400)
        .body(Map.of("error", AuthErrorCodeEnum.INVALID_EMAIL, "message", ex.getMessage()));
  }

  @ExceptionHandler(AuthEmailAlreadyUsedException.class)
  public ResponseEntity<Map<String, Object>> handleAuthEmailAlreadyUsedException(
      AuthEmailAlreadyUsedException ex) {
    return ResponseEntity.status(409)
        .body(
            Map.of(
                "error", AuthErrorCodeEnum.EXIST_ACCOUNT_WITH_EMAIL, "message", ex.getMessage()));
  }

  @ExceptionHandler(AuthAccountNotYetActivatedException.class)
  public ResponseEntity<Map<String, Object>> handleAuthAccountNotYetActivatedException(
      AuthAccountNotYetActivatedException ex) {
    return ResponseEntity.status(401)
        .body(Map.of("error", AuthErrorCodeEnum.ACCOUNT_NOT_ACTIVATED, "message", ex.getMessage()));
  }

  @ExceptionHandler(IncorrectCodeException.class)
  public ResponseEntity<Map<String, Object>> handleIncorrectCodeException(
      IncorrectCodeException ex) {
    return ResponseEntity.status(400)
        .body(
            Map.of(
                "error", AuthErrorCodeEnum.INVALID_CONFIRMATION_CODE, "message", ex.getMessage()));
  }

  @ExceptionHandler(AuthAccountAlreadyVerifyException.class)
  public ResponseEntity<Map<String, Object>> handleAuthAccountAlreadyVerifyException(
      AuthAccountAlreadyVerifyException ex) {
    return ResponseEntity.status(405)
        .body(
            Map.of(
                "error", AuthErrorCodeEnum.ACCOUNT_ALREADY_VERIFIED, "message", ex.getMessage()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(
      AuthenticationException ex) {
    String errorMessage = "ERROR: JWT Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(500)
        .body(
            Map.of(
                "error",
                SystemErrorCodeEnum.INTERNAL_ERROR,
                "message",
                "Authentication failed. Please try again later."));
  }

  @ExceptionHandler(AuthBadCredentialException.class)
  public ResponseEntity<Map<String, Object>> handleAuthBadCredentialException(
      AuthBadCredentialException ex) {

    return ResponseEntity.status(401)
        .body(
            Map.of(
                "error", AuthErrorCodeEnum.INVALID_EMAIL_OR_PASSWORD, "message", ex.getMessage()));
  }

  @ExceptionHandler(JwtServiceFailException.class)
  public ResponseEntity<Map<String, Object>> handleJwtServiceFailException(
      JwtServiceFailException ex) {
    String errorMessage = "ERROR: JWT Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(500)
        .body(
            Map.of(
                "error",
                SystemErrorCodeEnum.INTERNAL_ERROR,
                "message",
                "Authentication failed. Please try again."));
  }

  @ExceptionHandler(UserServiceFailException.class)
  public ResponseEntity<Map<String, Object>> handleUserServiceFailException(
      UserServiceFailException ex) {
    String errorMessage = "ERROR: User Service failed: " + ex.getMessage();
    log.error(errorMessage);
    return ResponseEntity.status(500)
        .body(
            Map.of(
                "error",
                SystemErrorCodeEnum.INTERNAL_ERROR,
                "message",
                "User service fail. Please try again."));
  }

  @ExceptionHandler(CompanyAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleCompanyAlreadyExistsException(
      CompanyAlreadyExistsException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", "COMPANY_ALREADY_EXISTS");
    response.put("message", ex.getMessage());
    response.put("existingCompanyId", ex.getExistingCompanyId());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(CompanyNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleCompanyNotFoundException(
      CompanyNotFoundException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "NOT_FOUND");
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(InvalidWebsiteException.class)
  public ResponseEntity<Map<String, String>> handleInvalidWebsiteException(
      InvalidWebsiteException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "INVALID_WEBSITE");
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(CompanyServiceFailException.class)
  public ResponseEntity<Map<String, String>> handleCompanyServiceFailException(
      CompanyServiceFailException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "INTERNAL_SERVER_ERROR");
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
