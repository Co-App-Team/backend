package com.backend.coapp.exception;

import java.util.List;
import lombok.Getter;

/** thrown when a query parameter contains an invalid value. */
@Getter
public class InvalidQueryParameterException extends RuntimeException {

  private final String parameter;
  private final String invalidValue;
  private final List<String> validValues;

  public InvalidQueryParameterException(
      String parameter, String invalidValue, List<String> validValues) {
    super("Invalid query parameter");
    this.parameter = parameter;
    this.invalidValue = invalidValue;
    this.validValues = validValues;
  }
}
