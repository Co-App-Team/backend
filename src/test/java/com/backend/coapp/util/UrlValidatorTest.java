package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UrlValidatorTest {

  @Test
  void isValidUrl_whenValidHttpUrl_expectTrue() {
    assertTrue(UrlValidator.isValidUrl("http://example.com"));
    assertTrue(UrlValidator.isValidUrl("http://www.example.com"));
    assertTrue(UrlValidator.isValidUrl("http://example.com/path"));
  }

  @Test
  void isValidUrl_whenValidHttpsUrl_expectTrue() {
    assertTrue(UrlValidator.isValidUrl("https://example.com"));
    assertTrue(UrlValidator.isValidUrl("https://www.example.com"));
    assertTrue(UrlValidator.isValidUrl("https://example.com/path"));
  }

  @Test
  void isValidUrl_whenUrlWithWhitespace_expectFalse() {
    assertFalse(UrlValidator.isValidUrl("  https://example.com  "));
  }

  @Test
  void isValidUrl_whenNull_expectFalse() {
    assertFalse(UrlValidator.isValidUrl(null));
  }

  @Test
  void isValidUrl_whenEmpty_expectFalse() {
    assertFalse(UrlValidator.isValidUrl(""));
  }

  @Test
  void isValidUrl_whenBlank_expectFalse() {
    assertFalse(UrlValidator.isValidUrl("   "));
  }

  @Test
  void isValidUrl_whenNoProtocol_expectFalse() {
    assertFalse(UrlValidator.isValidUrl("example.com"));
    assertFalse(UrlValidator.isValidUrl("www.example.com"));
  }

  @Test
  void isValidUrl_whenInvalidProtocol_expectFalse() {
    assertFalse(UrlValidator.isValidUrl("ftp://example.com"));
    assertFalse(UrlValidator.isValidUrl("file://example.com"));
  }

  @Test
  void constants_expectCorrectRegex() {
    assertEquals("^(https?://).*", UrlValidator.URL_REGEX);
  }
}
