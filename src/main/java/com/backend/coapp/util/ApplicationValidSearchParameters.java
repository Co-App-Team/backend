package com.backend.coapp.util;

import com.backend.coapp.model.enumeration.ApplicationStatus;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

/** valid values for the filterable/sortable query parameters on GET /api/application. */
@UtilityClass
public class ApplicationValidSearchParameters {

  /** Valid sort-by fields */
  public static final List<String> VALID_SORT_BY_VALUES =
      List.of(ApplicationConstants.SORT_BY_DATE_APPLIED);

  /** Valid sort order values. */
  public static final List<String> VALID_SORT_ORDER_VALUES = List.of("asc", "desc");

  /** All valid status values, direct from ApplicationStatus enum */
  public static final List<String> VALID_STATUS_VALUES =
      Arrays.stream(ApplicationStatus.values()).map(Enum::name).toList();
}
