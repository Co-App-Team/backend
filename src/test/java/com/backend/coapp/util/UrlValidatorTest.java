package com.backend.coapp.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UrlValidatorTest {

  @Test
  public void isValidUrl_whenValidHttpUrl_expectTrue() {
    assertTrue(UrlValidator.isValidUrl("http://example.com"));
    assertTrue(UrlValidator.isValidUrl("http://www.example.com"));
    assertTrue(UrlValidator.isValidUrl("http://example.com/path"));
  }

  @Test
  public void isValidUrl_whenValidHttpsUrl_expectTrue() {
    assertTrue(UrlValidator.isValidUrl("https://example.com"));
    assertTrue(UrlValidator.isValidUrl("https://www.example.com"));
    assertTrue(UrlValidator.isValidUrl("https://example.com/path"));
  }

  @Test
  public void isValidUrl_whenUrlWithWhitespace_expectFalse() {
    assertFalse(UrlValidator.isValidUrl("  https://example.com  "));
  }

  @Test
  public void isValidUrl_whenNull_expectFalse() {
    assertFalse(UrlValidator.isValidUrl(null));
  }

  @Test
  public void isValidUrl_whenEmpty_expectFalse() {
    assertFalse(UrlValidator.isValidUrl(""));
  }

  @Test
  public void isValidUrl_whenBlank_expectFalse() {
    assertFalse(UrlValidator.isValidUrl("   "));
  }

  @Test
  public void isValidUrl_whenNoProtocol_expectFalse() {
    assertFalse(UrlValidator.isValidUrl("example.com"));
    assertFalse(UrlValidator.isValidUrl("www.example.com"));
  }

  @Test
  public void isValidUrl_whenInvalidProtocol_expectFalse() {
    assertFalse(UrlValidator.isValidUrl("ftp://example.com"));
    assertFalse(UrlValidator.isValidUrl("file://example.com"));
  }

  @Test
  public void constants_expectCorrectRegex() {
    assertEquals("^(https?://).*", UrlValidator.URL_REGEX);
  }
}
