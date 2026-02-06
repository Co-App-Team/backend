package com.backend.coapp.service;

import com.backend.coapp.exception.JwtExpiredException;
import com.backend.coapp.exception.JwtInvalidTokenException;
import com.backend.coapp.exception.JwtServiceFailException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/** JWT services: handling all business logic related to JWT token */
@Service
public class JwtService {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpirationInMilliseconds;

  /**
   * Generate JWT token with extra claims and user ID
   *
   * <p>We use user email as user ID in this case
   *
   * @param extraClaims extra claim that we want to include to generate JWT
   * @param userDetails for which user we want to generate JWT for
   * @return JWT token as a String throws JwtServiceFailException if input is invalid or unknown
   *     failure in JWT
   */
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails)
      throws JwtServiceFailException {
    validateUserDetail(userDetails);
    if (extraClaims == null) {
      throw new JwtServiceFailException("Extra claim can not be NULL");
    }
    try {
      return Jwts.builder()
          .setClaims(extraClaims)
          .setSubject(userDetails.getUsername())
          .setIssuedAt(new Date(System.currentTimeMillis()))
          .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMilliseconds))
          .signWith(this.getSignInKey(), SignatureAlgorithm.HS256)
          .compact();
    } catch (JwtException e) {
      throw new JwtServiceFailException(e.getMessage());
    }
  }

  /**
   * Generate JWT token with user ID only We use user email as user ID in this case
   *
   * @param userDetails for which user we want to generate JWT for
   * @return JWT token as a String throws JwtServiceFailException if input is invalid or unknown
   *     failure in JWT
   */
  public String generateToken(UserDetails userDetails) throws JwtServiceFailException {
    validateUserDetail(userDetails);
    return this.generateToken(new HashMap<>(), userDetails);
  }

  /**
   * Check if the token is valid
   *
   * @param token provided token from client
   * @param userDetails information about which user that the token is supposed to belong to
   * @return true if the token is valid; false if the token doesn't belong to the right user or the
   *     token expire
   * @throws JwtExpiredException when the token already expired
   * @throws JwtInvalidTokenException when bad JWT token
   * @throws JwtServiceFailException for unknown exception or bad input parameters
   */
  public boolean isTokenValid(String token, UserDetails userDetails)
      throws JwtExpiredException, JwtInvalidTokenException, JwtServiceFailException {
    if (token == null || token.isBlank()) {
      throw new JwtInvalidTokenException();
    }
    validateUserDetail(userDetails);

    String userEmail = this.extractUserEmail(token);
    return (userEmail.equals(userDetails.getUsername()));
  }

  /**
   * Extract the user email
   *
   * @param token JWT token
   * @return email
   * @throws JwtExpiredException when the token already expired
   * @throws JwtInvalidTokenException when bad JWT token
   * @throws JwtServiceFailException for unknown exception or bad input parameters
   */
  public String extractUserEmail(String token)
      throws JwtExpiredException, JwtInvalidTokenException, JwtServiceFailException {

    if (token == null || token.isBlank()) {
      throw new JwtInvalidTokenException();
    }
    return extractSpecificClaim(token, Claims::getSubject); // In JWT, the subject is the user email
  }

  /**
   * Get expiration duration of a token
   *
   * @return expiration duration in milliseconds.
   */
  public long getExpirationDurationInMilliseconds() {
    return this.jwtExpirationInMilliseconds;
  }

  /**
   * Extract a specific claim from a token
   *
   * @param token to extract the desire claim
   * @param claimResolver resolver (must be callable/function) to extract the desire claim
   * @return the desire claim wanted to extract
   * @throws JwtExpiredException when the token already expired
   * @throws JwtInvalidTokenException when bad JWT token
   * @throws JwtServiceFailException for unknown exception
   */
  private <T> T extractSpecificClaim(String token, Function<Claims, T> claimResolver)
      throws JwtExpiredException, JwtInvalidTokenException, JwtServiceFailException {
    Claims claims = this.extractAllClaims(token);
    return claimResolver.apply(claims);
  }

  /**
   * Extract all the claim of a token
   *
   * @param token token that we want to extract all the claims
   * @return Claims
   * @throws JwtExpiredException when the token already expired
   * @throws JwtInvalidTokenException when bad JWT token
   * @throws JwtServiceFailException for unknown exception
   */
  private Claims extractAllClaims(String token)
      throws JwtExpiredException, JwtInvalidTokenException, JwtServiceFailException {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(this.getSignInKey())
          .build()
          .parseClaimsJws(token)
          .getBody();

    } catch (ExpiredJwtException e) {
      throw new JwtExpiredException();

    } catch (MalformedJwtException | UnsupportedJwtException | SignatureException e) {
      // Bad JWT token
      throw new JwtInvalidTokenException();

    } catch (JwtException e) {
      throw new JwtServiceFailException(e.getMessage());
    }
  }

  /**
   * Return secret that is used to digitally sign the JWT
   *
   * @return Key
   */
  private Key getSignInKey() {
    byte[] keyInByte = Decoders.BASE64.decode(this.secretKey);
    return Keys.hmacShaKeyFor(keyInByte);
  }

  /**
   * Validate UserDetails
   *
   * @param userDetails provided UserDetails
   * @throws JwtServiceFailException when userDetails is invalid
   */
  private void validateUserDetail(UserDetails userDetails) throws JwtServiceFailException {
    if (userDetails == null) {
      throw new JwtServiceFailException("UserDetail can NOT be null");
    }
    if (userDetails.getUsername() == null || userDetails.getUsername().isBlank()) {
      throw new JwtServiceFailException("Username can NOT be null or empty");
    }
  }
}
