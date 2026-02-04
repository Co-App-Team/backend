package com.backend.coapp.model.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

/** Enum for valid work term seasons. Allowed: Fall, Winter, or Summer. */
public enum WorkTermSeasonEnum {
  FALL("Fall"),
  WINTER("Winter"),
  SUMMER("Summer");

  private final String displayName;

  WorkTermSeasonEnum(String displayName) {
    this.displayName = displayName;
  }

  @JsonValue
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Converts a string to WorkTermSeason enum (case-sensitive).
   *
   * @param value The string value to convert
   * @return The matching WorkTermSeason enum value
   * @throws IllegalArgumentException if the value is not valid
   */
  public static WorkTermSeasonEnum fromString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Work term season cannot be null");
    }

    for (WorkTermSeasonEnum season : WorkTermSeasonEnum.values()) {
      if (season.displayName.equals(value)) {
        return season;
      }
    }

    throw new IllegalArgumentException(
        "Invalid work term season: " + value + ". Must be one of: Fall, Winter, Summer");
  }

  /**
   * Checks if a string is a valid work term season.
   *
   * @param value The string to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValid(String value) {
    if (value == null) {
      return false;
    }

    for (WorkTermSeasonEnum season : WorkTermSeasonEnum.values()) {
      if (season.displayName.equals(value)) {
        return true;
      }
    }
    return false;
  }
}
