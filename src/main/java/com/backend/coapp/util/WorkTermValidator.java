package com.backend.coapp.util;

import com.backend.coapp.model.enumeration.WorkTermSeasonEnum;
import java.time.Year;

/** Utility class for validating work term seasons and years */
public class WorkTermValidator {

  private static final int MIN_YEAR = 1950;

  private WorkTermValidator() {
    throw new IllegalStateException("Cannot call constructor on a utility class");
  }

  /**
   * Validates work term season Throws illegal argument exception if season invalid.
   *
   * @param season The season to validate
   * @throws IllegalArgumentException if season is invalid
   */
  public static void validateSeason(String season) {
    if (season == null || season.trim().isEmpty()) {
      throw new IllegalArgumentException("Work term season cannot be null or empty");
    }

    if (!WorkTermSeasonEnum.isValid(season)) {
      throw new IllegalArgumentException("Work term season must be one of: Fall, Winter, Summer");
    }
  }

  /**
   * Validates work term year. Throws illegal argument exception if year is invalid.
   *
   * @param year The year to validate
   * @throws IllegalArgumentException if year is invalid
   */
  public static void validateYear(Integer year) {
    if (year == null) {
      throw new IllegalArgumentException("Work term year cannot be null");
    }

    int currentYear = Year.now().getValue();

    if (year < MIN_YEAR || year > currentYear) {
      throw new IllegalArgumentException(
          String.format("Work term year must be between %d and %d", MIN_YEAR, currentYear));
    }
  }

  /**
   * Gets the minimum allowed year for a work term
   *
   * @return The minimum year (1950)
   */
  public static int getMinYear() {
    return MIN_YEAR;
  }

  /**
   * Gets the maximum allowed year (whatever the current year is).
   *
   * @return The current year
   */
  public static int getMaxYear() {
    return Year.now().getValue();
  }
}
