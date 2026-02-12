package com.backend.coapp.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.JwtExpiredException;
import com.backend.coapp.exception.JwtInvalidTokenException;
import com.backend.coapp.exception.JwtServiceFailException;
import com.backend.coapp.model.enumeration.AuthErrorCodeEnum;
import com.backend.coapp.model.enumeration.SystemErrorCodeEnum;
import com.backend.coapp.model.enumeration.UserRolesEnum;
import com.backend.coapp.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {
  @Mock private JwtService jwtService;

  @Mock private UserDetailsService userDetailsService;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtAuthFilter jwtAuthFilter;

  private final String JWT_TOKEN = "dummyToken";
  private final String USER_EMAIL = "dummyId";
  private UserDetails userDetails;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  public void setUp() {
    this.request = new MockHttpServletRequest();
    this.response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
    this.userDetails =
        User.builder()
            .username(USER_EMAIL)
            .password("dummyPassword")
            .authorities(new SimpleGrantedAuthority(UserRolesEnum.USER_ROLE.name()))
            .build();
  }

  @Test
  public void doFilterInternal_whenEverythingSuccess() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenReturn(USER_EMAIL);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(this.userDetails);
    when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);

    this.jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, atLeastOnce()).extractUserIdentity(JWT_TOKEN);
    verify(userDetailsService, times(1)).loadUserByUsername(USER_EMAIL);
    verify(jwtService, times(1)).isTokenValid(JWT_TOKEN, this.userDetails);
    verify(filterChain).doFilter(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth);
    assertInstanceOf(UsernamePasswordAuthenticationToken.class, auth);
    assertEquals(userDetails, auth.getPrincipal());
  }

  @Test
  void doFilterInternal_whenNoAuthorizationHeader_expectSecurityContextNull() throws Exception {
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService, userDetailsService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  public void doFilterInternal_whenJwtExpire_expect401() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenThrow(new JwtExpiredException());

    this.jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, times(1)).extractUserIdentity(JWT_TOKEN);
    verifyNoInteractions(userDetailsService);
    verifyNoInteractions(filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    Map<String, String> errorResponse = this.parseJsonResponse(response);

    assertTrue(errorResponse.containsKey("message"));
    assertTrue(errorResponse.containsKey("error"));
    assertFalse(errorResponse.get("message").isBlank());
    assertEquals(AuthErrorCodeEnum.TOKEN_EXPIRE.name(), errorResponse.get("error"));
  }

  @Test
  public void doFilterInternal_whenJwtInvalid_expect401() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenThrow(new JwtInvalidTokenException());

    this.jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, times(1)).extractUserIdentity(JWT_TOKEN);
    verifyNoInteractions(userDetailsService);
    verifyNoInteractions(filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    Map<String, String> errorResponse = this.parseJsonResponse(response);

    assertTrue(errorResponse.containsKey("message"));
    assertTrue(errorResponse.containsKey("error"));
    assertFalse(errorResponse.get("message").isBlank());
    assertEquals(AuthErrorCodeEnum.INVALID_TOKEN.name(), errorResponse.get("error"));
  }

  @Test
  public void doFilterInternal_whenJwtServiceFail_expect500() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenThrow(new JwtServiceFailException("foo"));

    this.jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, times(1)).extractUserIdentity(JWT_TOKEN);
    verifyNoInteractions(userDetailsService);
    verifyNoInteractions(filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    Map<String, String> errorResponse = this.parseJsonResponse(response);

    assertTrue(errorResponse.containsKey("message"));
    assertTrue(errorResponse.containsKey("error"));
    assertFalse(errorResponse.get("message").isBlank());
    assertEquals(SystemErrorCodeEnum.INTERNAL_ERROR.name(), errorResponse.get("error"));
  }

  @Test
  public void doFilterInternal_whenExtractUserEmailNull_expect401() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenReturn(null);

    this.jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, times(1)).extractUserIdentity(JWT_TOKEN);
    verifyNoInteractions(userDetailsService);
    verifyNoInteractions(filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void doFilterInternal_whenExtractUserEmailBlank_expect401() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenReturn("");

    this.jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, times(1)).extractUserIdentity(JWT_TOKEN);
    verifyNoInteractions(userDetailsService);
    verifyNoInteractions(filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void doFilterInternal_whenJwtInvalidFromIsTokenValid_expect401() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenReturn(USER_EMAIL);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(this.userDetails);
    when(jwtService.isTokenValid(anyString(), any())).thenReturn(false);

    this.jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, times(1)).extractUserIdentity(JWT_TOKEN);
    verify(userDetailsService).loadUserByUsername(USER_EMAIL);
    verifyNoInteractions(filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    Map<String, String> errorResponse = this.parseJsonResponse(response);

    assertTrue(errorResponse.containsKey("message"));
    assertTrue(errorResponse.containsKey("error"));
    assertFalse(errorResponse.get("message").isBlank());
    assertEquals(AuthErrorCodeEnum.INVALID_TOKEN.name(), errorResponse.get("error"));
  }

  @Test
  public void doFilterInternal_whenUserEmailNotFound_expect401() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenReturn(USER_EMAIL);
    when(userDetailsService.loadUserByUsername(anyString()))
        .thenThrow(new UsernameNotFoundException("foo"));

    this.jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, times(1)).extractUserIdentity(JWT_TOKEN);
    verify(userDetailsService).loadUserByUsername(USER_EMAIL);
    verifyNoInteractions(filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    Map<String, String> errorResponse = this.parseJsonResponse(response);

    assertTrue(errorResponse.containsKey("message"));
    assertTrue(errorResponse.containsKey("error"));
    assertFalse(errorResponse.get("message").isBlank());
    assertEquals(AuthErrorCodeEnum.EMAIL_NOT_REGISTERED.name(), errorResponse.get("error"));
  }

  @Test
  public void doFilterInternal_whenAlreadyAuth_expect200() throws Exception {
    request.addHeader("Authorization", JWT_TOKEN);
    when(jwtService.extractUserIdentity(anyString())).thenReturn(USER_EMAIL);
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    verifyNoInteractions(userDetailsService);
    assertEquals(authToken, SecurityContextHolder.getContext().getAuthentication());
  }

  /**
   * Parse MockHttpServletResponse to Map<String,String>
   *
   * @param response mock HTTP response
   * @return Map<String, String> as JSON of response body
   * @throws UnsupportedEncodingException from MockHttpServletResponse.getContentAsString
   */
  private Map<String, String> parseJsonResponse(MockHttpServletResponse response)
      throws UnsupportedEncodingException {
    String content = response.getContentAsString();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> errorResponse = mapper.readValue(content, Map.class);
    return errorResponse;
  }
}
