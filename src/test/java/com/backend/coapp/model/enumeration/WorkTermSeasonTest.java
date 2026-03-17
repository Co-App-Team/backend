package com.backend.coapp.model.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WorkTermSeasonTest {

  @Test
  void fromString_whenValidSeason_expectCorrectEnum() {
    assertEquals(WorkTermSeason.FALL, WorkTermSeason.fromString("Fall"));
    assertEquals(WorkTermSeason.WINTER, WorkTermSeason.fromString("Winter"));
    assertEquals(WorkTermSeason.SUMMER, WorkTermSeason.fromString("Summer"));
  }

  @Test
  void fromString_whenInvalidSeason_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeason.fromString("Spring"));
    assertTrue(exception.getMessage().contains("Invalid work term season"));
  }

  @Test
  void fromString_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeason.fromString(null));
    assertEquals("Work term season cannot be null", exception.getMessage());
  }

  @Test
  void fromString_whenInvalidCase_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeason.fromString("fall"));
    assertTrue(exception.getMessage().contains("Invalid work term season"));
  }

  @Test
  void isValid_whenValidSeasons_expectTrue() {
    assertTrue(WorkTermSeason.isValid("Fall"));
    assertTrue(WorkTermSeason.isValid("Winter"));
    assertTrue(WorkTermSeason.isValid("Summer"));
  }

  @Test
  void isValid_whenInvalidSeason_expectFalse() {
    assertFalse(WorkTermSeason.isValid("Spring"));
    assertFalse(WorkTermSeason.isValid("fall"));
    assertFalse(WorkTermSeason.isValid(null));
    assertFalse(WorkTermSeason.isValid(""));
  }

  @Test
  void getDisplayName_expectCorrectName() {
    assertEquals("Fall", WorkTermSeason.FALL.getDisplayName());
    assertEquals("Winter", WorkTermSeason.WINTER.getDisplayName());
    assertEquals("Summer", WorkTermSeason.SUMMER.getDisplayName());
  }
}
