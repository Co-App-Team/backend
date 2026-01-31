package com.backend.coapp.handler;

import com.backend.coapp.exception.*;
import com.backend.coapp.model.enumeration.AuthErrorCodeEnum;
import com.backend.coapp.model.enumeration.RequestErrorCodeEnum;
import com.backend.coapp.model.enumeration.SystemErrorCodeEnum;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
                "Unable to reset verification code. Please try again later."));
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
}
