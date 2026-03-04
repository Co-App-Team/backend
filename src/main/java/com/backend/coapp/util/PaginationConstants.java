package com.backend.coapp.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PaginationConstants {
  // Company pagination constants
  public static final int COMPANY_DEFAULT_PAGE = 0;
  public static final int COMPANY_DEFAULT_SIZE = 20;
  public static final int COMPANY_MAX_SIZE = 100;
  public static final int COMPANY_MIN_SIZE = 1;
  public static final Boolean DEFAULT_USE_PAGINATION = false;

  // for @RequestParam strings to not be hardcoded
  public static final String COMPANY_DEFAULT_PAGE_STR = "0";
  public static final String COMPANY_DEFAULT_SIZE_STR = "20";
  public static final String DEFAULT_USE_PAGINATION_STR = "false";
  // review constants
  public static final int REVIEW_DEFAULT_PAGE = 0;
  public static final int REVIEW_DEFAULT_SIZE = 10;
  public static final int REVIEW_MAX_SIZE = 50;
  public static final String REVIEW_DEFAULT_PAGE_STR = "0";
  public static final String REVIEW_DEFAULT_SIZE_STR = "10";
}
