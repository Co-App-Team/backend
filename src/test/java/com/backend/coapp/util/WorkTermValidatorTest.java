package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Year;
import org.junit.jupiter.api.Test;

class WorkTermValidatorTest {

  @Test
  void validateSeason_whenValidSeasons_expectNoException() {
    assertDoesNotThrow(() -> WorkTermValidator.validateSeason("Fall"));
    assertDoesNotThrow(() -> WorkTermValidator.validateSeason("Winter"));
    assertDoesNotThrow(() -> WorkTermValidator.validateSeason("Summer"));
  }

  @Test
  void validateSeason_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermValidator.validateSeason(null));
    assertEquals("Work term season cannot be null or empty", exception.getMessage());
  }

  @Test
  void validateSeason_whenEmpty_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermValidator.validateSeason(""));
    assertEquals("Work term season cannot be null or empty", exception.getMessage());
  }

  @Test
  void validateSeason_whenWhitespace_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermValidator.validateSeason("   "));
    assertEquals("Work term season cannot be null or empty", exception.getMessage());
  }

  @Test
  void validateSeason_whenInvalidSeason_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> WorkTermValidator.validateSeason("Spring"));
    assertEquals("Work term season must be one of: Fall, Winter, Summer", exception.getMessage());
  }

  @Test
  void validateSeason_whenInvalidCase_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> WorkTermValidator.validateSeason("fall"));
    assertEquals("Work term season must be one of: Fall, Winter, Summer", exception.getMessage());
  }

  @Test
  void validateYear_whenValidYear_expectNoException() {
    int currentYear = Year.now().getValue();
    assertDoesNotThrow(() -> WorkTermValidator.validateYear(1950));
    assertDoesNotThrow(() -> WorkTermValidator.validateYear(2000));
    assertDoesNotThrow(() -> WorkTermValidator.validateYear(currentYear));
  }

  @Test
  void validateYear_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermValidator.validateYear(null));
    assertEquals("Work term year cannot be null", exception.getMessage());
  }

  @Test
  void validateYear_whenTooLow_expectThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> WorkTermValidator.validateYear(WorkTermValidator.getMinYear() - 1));
    assertEquals(
        "Work term year must be between 1950 and " + Year.now().getValue(), exception.getMessage());
  }

  @Test
  void validateYear_whenTooHigh_expectThrowsException() {
    int futureYear = Year.now().getValue() + 1;
    int currentYear = Year.now().getValue();
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> WorkTermValidator.validateYear(futureYear));
    assertEquals("Work term year must be between 1950 and " + currentYear, exception.getMessage());
  }

  @Test
  void getMinYear_expectReturns1950() {
    assertEquals(1950, WorkTermValidator.getMinYear());
  }

  @Test
  void getMaxYear_expectReturnsCurrentYear() {
    int currentYear = Year.now().getValue();
    assertEquals(currentYear, WorkTermValidator.getMaxYear());
  }
}
