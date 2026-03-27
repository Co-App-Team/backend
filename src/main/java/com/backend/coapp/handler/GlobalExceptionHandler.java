package com.backend.coapp.handler;

import com.backend.coapp.exception.ApiException;
import com.backend.coapp.exception.application.*;
import com.backend.coapp.exception.auth.*;
import com.backend.coapp.exception.genai.*;
import com.backend.coapp.model.enumeration.*;
import java.time.LocalDate;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

/** Exception handler for controller. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
    if (ex.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
      log.error("Failure %s".formatted(ex.getMessage()));
    }
    return errorResponse(ex.getStatus(), ex.getErrorCode(), ((Exception) ex).getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex) {
    log.error("Unhandled exception", ex);
    return errorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        SystemErrorCode.INTERNAL_ERROR,
        "An unexpected error occurred.");
  }

  private ResponseEntity<Map<String, Object>> errorResponse(
      HttpStatus status, Object code, String message) {
    return ResponseEntity.status(status).body(Map.of("error", code, "message", message));
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
}
