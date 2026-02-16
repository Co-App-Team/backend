package com.backend.coapp.model.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class WorkTermSeasonTest {

  @Test
  public void fromString_whenValidSeason_expectCorrectEnum() {
    assertEquals(WorkTermSeason.FALL, WorkTermSeason.fromString("Fall"));
    assertEquals(WorkTermSeason.WINTER, WorkTermSeason.fromString("Winter"));
    assertEquals(WorkTermSeason.SUMMER, WorkTermSeason.fromString("Summer"));
  }

  @Test
  public void fromString_whenInvalidSeason_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeason.fromString("Spring"));
    assertTrue(exception.getMessage().contains("Invalid work term season"));
  }

  @Test
  public void fromString_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeason.fromString(null));
    assertEquals("Work term season cannot be null", exception.getMessage());
  }

  @Test
  public void fromString_whenInvalidCase_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeason.fromString("fall"));
    assertTrue(exception.getMessage().contains("Invalid work term season"));
  }

  @Test
  public void isValid_whenValidSeasons_expectTrue() {
    assertTrue(WorkTermSeason.isValid("Fall"));
    assertTrue(WorkTermSeason.isValid("Winter"));
    assertTrue(WorkTermSeason.isValid("Summer"));
  }

  @Test
  public void isValid_whenInvalidSeason_expectFalse() {
    assertFalse(WorkTermSeason.isValid("Spring"));
    assertFalse(WorkTermSeason.isValid("fall"));
    assertFalse(WorkTermSeason.isValid(null));
    assertFalse(WorkTermSeason.isValid(""));
  }

  @Test
  public void getDisplayName_expectCorrectName() {
    assertEquals("Fall", WorkTermSeason.FALL.getDisplayName());
    assertEquals("Winter", WorkTermSeason.WINTER.getDisplayName());
    assertEquals("Summer", WorkTermSeason.SUMMER.getDisplayName());
  }
}
