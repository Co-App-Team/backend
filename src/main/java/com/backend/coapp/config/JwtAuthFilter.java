package com.backend.coapp.config;

import com.backend.coapp.exception.JwtExpiredException;
import com.backend.coapp.exception.JwtInvalidTokenException;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import com.backend.coapp.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * JwtAuthFilter
 *
 * <p>This will run once per request (except for request for auth. This will extract access token
 * (JWT) from each request and verify it. If invalid JWT, return 403/401 immediately.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  /**
   * Perform filter and verify JWT token
   *
   * @param request an API call
   * @param response a response to return
   * @param filterChain next filter in the chain
   * @throws ServletException exceptions propagate up the filter chain
   * @throws IOException exceptions propagate up the filter chain
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("Authorization".equals(cookie.getName())) {
          authHeader = cookie.getValue();
          break;
        }
      }
    }
    String jwt;
    String userIdentity;

    if (authHeader == null) {
      filterChain.doFilter(request, response);
      return; // Return without update SecurityContextHolder
    }

    jwt = authHeader;
    try {
      userIdentity = jwtService.extractUserIdentity(jwt);
      if (userIdentity == null || userIdentity.isBlank()) {
        throw new JwtInvalidTokenException();
      }
      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userIdentity);
        if (jwtService.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
          throw new JwtInvalidTokenException();
        }
      }
    } catch (JwtExpiredException ex) {
      this.handleException(
          response, HttpStatus.UNAUTHORIZED, AuthErrorCode.TOKEN_EXPIRE.name(), ex.getMessage());
      return;
    } catch (JwtInvalidTokenException ex) {
      this.handleException(
          response, HttpStatus.UNAUTHORIZED, AuthErrorCode.INVALID_TOKEN.name(), ex.getMessage());
      return;
    } catch (UsernameNotFoundException ex) {
      this.handleException(
          response,
          HttpStatus.UNAUTHORIZED,
          AuthErrorCode.EMAIL_NOT_REGISTERED.name(),
          ex.getMessage());
      return;
    } catch (Exception ex) {
      String errorMessage = "ERROR: JWT Service failed: " + ex.getMessage();
      log.error(errorMessage);
      this.handleException(
          response,
          HttpStatus.INTERNAL_SERVER_ERROR,
          SystemErrorCode.INTERNAL_ERROR.name(),
          "Can NOT authorize. Please try again");
      return;
    }
    filterChain.doFilter(request, response);
  }

  /**
   * @param response response to populate
   * @param status status of the request
   * @param errorCode error code for client to classify
   * @param message error message
   * @throws IOException when writing response fail
   */
  private void handleException(
      HttpServletResponse response, HttpStatus status, String errorCode, String message)
      throws IOException {

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", errorCode);
    errorResponse.put("message", message);

    ObjectMapper mapper = new ObjectMapper();
    response.getWriter().write(mapper.writeValueAsString(errorResponse));
  }
}
