package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.enumeration.ApplicationStatus;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.6 and revised by Eric Hodgson */
class ApplicationValidSearchParametersTest {

  @Test
  void validSortByValues_expectContainsDateApplied() {
    List<String> values = ApplicationValidSearchParameters.VALID_SORT_BY_VALUES;

    assertNotNull(values);
    assertTrue(values.contains(ApplicationConstants.SORT_BY_DATE_APPLIED));
    assertEquals(1, values.size());
  }

  @Test
  void validSortOrderValues_expectContainsAscAndDesc() {
    List<String> values = ApplicationValidSearchParameters.VALID_SORT_ORDER_VALUES;

    assertNotNull(values);
    assertTrue(values.contains("asc"));
    assertTrue(values.contains("desc"));
    assertEquals(2, values.size());
  }

  @Test
  void validStatusValues_expectContainsAllEnumValues() {
    List<String> values = ApplicationValidSearchParameters.VALID_STATUS_VALUES;
    List<String> expectedValues =
        Arrays.stream(ApplicationStatus.values()).map(Enum::name).toList();

    assertNotNull(values);
    assertEquals(expectedValues.size(), values.size());
    assertTrue(values.containsAll(expectedValues));
  }
}
