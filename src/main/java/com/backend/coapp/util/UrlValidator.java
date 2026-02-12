package com.backend.coapp.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for URL validation
 */
@UtilityClass
public class UrlValidator {

  public static final String URL_REGEX = "^(https?://).*";

  /**
   * Validates if a URL starts with http:// or https://
   *
   * @param url The URL to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidUrl(String url) {
    if (url == null || url.isBlank()) {
      return false;
    }
    return url.trim().matches(URL_REGEX);
  }
}
