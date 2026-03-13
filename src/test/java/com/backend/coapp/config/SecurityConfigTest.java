package com.backend.coapp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.backend.coapp.model.enumeration.AuthErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

/** Parts of these tests is written with help of Claude (Sonnet 4.6) */
class SecurityConfigTest {

  private AuthenticationEntryPoint authenticationEntryPoint;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    SecurityConfig securityConfig =
        new SecurityConfig(null, null); // pass nulls since we only test the entry point
    this.authenticationEntryPoint = securityConfig.customAuthenticationEntryPoint();
    this.request = new MockHttpServletRequest();
    this.response = new MockHttpServletResponse();
  }

  @Test
  void customAuthenticationEntryPoint_whenCalled_expect401Status() throws Exception {
    authenticationEntryPoint.commence(
        request, response, new AuthenticationException("Unauthorized") {});

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  void customAuthenticationEntryPoint_whenCalled_expectJsonContentType() throws Exception {
    authenticationEntryPoint.commence(
        request, response, new AuthenticationException("Unauthorized") {});

    assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
  }

  @Test
  void customAuthenticationEntryPoint_whenCalled_expectCorrectResponseBody() throws Exception {
    authenticationEntryPoint.commence(
        request, response, new AuthenticationException("Unauthorized") {});

    String body = response.getContentAsString();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> responseBody = mapper.readValue(body, Map.class);

    assertEquals(AuthErrorCode.INVALID_TOKEN.name(), responseBody.get("error"));
    assertEquals("Authentication is required.", responseBody.get("message"));
  }

  @Test
  void customAuthenticationEntryPoint_whenCalled_expectBothErrorAndMessageFieldsPresent()
      throws Exception {
    authenticationEntryPoint.commence(
        request, response, new AuthenticationException("Unauthorized") {});

    String body = response.getContentAsString();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> responseBody = mapper.readValue(body, Map.class);

    assertTrue(responseBody.containsKey("error"));
    assertTrue(responseBody.containsKey("message"));
  }
}
