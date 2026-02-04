package com.backend.coapp.model.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class WorkTermSeasonEnumTest {

  @Test
  public void fromString_whenValidSeason_expectCorrectEnum() {
    assertEquals(WorkTermSeasonEnum.FALL, WorkTermSeasonEnum.fromString("Fall"));
    assertEquals(WorkTermSeasonEnum.WINTER, WorkTermSeasonEnum.fromString("Winter"));
    assertEquals(WorkTermSeasonEnum.SUMMER, WorkTermSeasonEnum.fromString("Summer"));
  }

  @Test
  public void fromString_whenInvalidSeason_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeasonEnum.fromString("Spring"));
    assertTrue(exception.getMessage().contains("Invalid work term season"));
  }

  @Test
  public void fromString_whenNull_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeasonEnum.fromString(null));
    assertEquals("Work term season cannot be null", exception.getMessage());
  }

  @Test
  public void fromString_whenInvalidCase_expectThrowsException() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> WorkTermSeasonEnum.fromString("fall"));
    assertTrue(exception.getMessage().contains("Invalid work term season"));
  }

  @Test
  public void isValid_whenValidSeasons_expectTrue() {
    assertTrue(WorkTermSeasonEnum.isValid("Fall"));
    assertTrue(WorkTermSeasonEnum.isValid("Winter"));
    assertTrue(WorkTermSeasonEnum.isValid("Summer"));
  }

  @Test
  public void isValid_whenInvalidSeason_expectFalse() {
    assertFalse(WorkTermSeasonEnum.isValid("Spring"));
    assertFalse(WorkTermSeasonEnum.isValid("fall"));
    assertFalse(WorkTermSeasonEnum.isValid(null));
    assertFalse(WorkTermSeasonEnum.isValid(""));
  }

  @Test
  public void getDisplayName_expectCorrectName() {
    assertEquals("Fall", WorkTermSeasonEnum.FALL.getDisplayName());
    assertEquals("Winter", WorkTermSeasonEnum.WINTER.getDisplayName());
    assertEquals("Summer", WorkTermSeasonEnum.SUMMER.getDisplayName());
  }
}
