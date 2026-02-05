package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.JwtExpiredException;
import com.backend.coapp.exception.JwtInvalidTokenException;
import com.backend.coapp.exception.JwtServiceFailException;
import com.backend.coapp.model.document.UserModel;
import io.jsonwebtoken.*;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceTest {
  private JwtService jwtService;
  private UserDetails userDetails;
  private String validToken;
  private static final String TEST_SECRET_KEY =
      "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
  private static final long EXPIRATION_TIME = 3600000; // 1 hour in milliseconds

  @BeforeEach
  public void setUp() {
    this.jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
    ReflectionTestUtils.setField(jwtService, "jwtExpirationInMilliseconds", EXPIRATION_TIME);

    this.userDetails = new UserModel("foo@mail.com", "password123", "foo", "woof", 123);
    this.validToken = this.jwtService.generateToken(userDetails);
  }

  @Test
  void generateToken_whenExtraClaims_expectValidToken() {
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", "USER");
    extraClaims.put("iss", "Co-App");

    String token = jwtService.generateToken(extraClaims, this.userDetails);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
  }

  @Test
  void generateToken_whenExtraClaimsDifferentType_expectValidToken() {
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", "USER");
    extraClaims.put("iss", "Co-App");
    extraClaims.put("specialID", 123);

    String token = jwtService.generateToken(extraClaims, this.userDetails);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
  }

  @Test
  void generateToken_whenEmptyClaims_expectValidToken() {
    Map<String, Object> extraClaims = new HashMap<>();

    String token = jwtService.generateToken(extraClaims, this.userDetails);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
  }

  @Test
  void generateToken_whenNullExtraClaims_expectException() {
    JwtServiceFailException ex =
        assertThrows(
            JwtServiceFailException.class, () -> jwtService.generateToken(null, this.userDetails));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  void generateToken_whenNullUserDetail_expectException() {
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", "USER");
    extraClaims.put("iss", "Co-App");
    JwtServiceFailException ex =
        assertThrows(
            JwtServiceFailException.class, () -> jwtService.generateToken(extraClaims, null));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  void generateToken_whenNullUsername_expectException() {
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", "USER");
    extraClaims.put("iss", "Co-App");
    UserDetails badUser = new UserModel(null, "password123", "foo", "woof", 123);
    JwtServiceFailException ex =
        assertThrows(
            JwtServiceFailException.class, () -> jwtService.generateToken(extraClaims, badUser));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  void generateToken_whenEmptyUsername_expectException() {
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", "USER");
    extraClaims.put("iss", "Co-App");
    UserDetails badUser = new UserModel("", "password123", "foo", "woof", 123);
    JwtServiceFailException ex =
        assertThrows(
            JwtServiceFailException.class, () -> jwtService.generateToken(extraClaims, badUser));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  void generateToken_whenUserDetailOnly_expectValidToken() {
    String token = jwtService.generateToken(this.userDetails);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
  }

  @Test
  void generateToken_whenNullUserDetailUserDetailOnly_expectException() {
    JwtServiceFailException ex =
        assertThrows(JwtServiceFailException.class, () -> jwtService.generateToken(null));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  void generateToken_whenNullUsernameUserDetailOnly_expectException() {
    UserDetails badUser = new UserModel(null, "password123", "foo", "woof", 123);
    JwtServiceFailException ex =
        assertThrows(JwtServiceFailException.class, () -> jwtService.generateToken(badUser));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void generateToken_whenUnexpectedException_expectException() {
    // This test is written with MockedStatic suggested by Claude
    try (MockedStatic<Jwts> jwtsMock = mockStatic(Jwts.class)) {
      JwtBuilder mockJwtBuilder = mock(JwtBuilder.class);

      // Setup mock chain
      jwtsMock.when(Jwts::builder).thenReturn(mockJwtBuilder);

      // Make it throw a generic JwtException
      when(mockJwtBuilder.setClaims(anyMap())).thenThrow(new JwtException("Unexpected JWT error"));

      JwtServiceFailException ex =
          assertThrows(
              JwtServiceFailException.class,
              () -> {
                jwtService.generateToken(this.userDetails);
              });

      assertTrue(ex.getMessage().contains("Unexpected JWT error"));
    }
  }

  @Test
  public void extractUserEmail_expectMatchEmail() {
    assertEquals(this.userDetails.getUsername(), this.jwtService.extractUserEmail(this.validToken));
  }

  @Test
  public void extractUserEmail_whenTokenNull_expectException() {
    JwtInvalidTokenException ex =
        assertThrows(JwtInvalidTokenException.class, () -> jwtService.extractUserEmail(null));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void extractUserEmail_whenTokenBlank_expectException() {
    JwtInvalidTokenException ex =
        assertThrows(JwtInvalidTokenException.class, () -> jwtService.extractUserEmail(""));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void extractUserEmail_whenTokenAlreadyExpired_expectException() {
    ReflectionTestUtils.setField(
        jwtService, "jwtExpirationInMilliseconds", -1000L); // Set expiration date to the past
    String expiredToken = jwtService.generateToken(userDetails);
    JwtExpiredException ex =
        assertThrows(JwtExpiredException.class, () -> jwtService.extractUserEmail(expiredToken));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void extractUserEmail_whenBadToken_expectException() {
    String badToken = "this.is.not.a.valid.jwt";
    JwtInvalidTokenException ex =
        assertThrows(JwtInvalidTokenException.class, () -> jwtService.extractUserEmail(badToken));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void extractUserEmail_whenUnsupportedToken_expectException() {
    String badToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";
    JwtInvalidTokenException ex =
        assertThrows(JwtInvalidTokenException.class, () -> jwtService.extractUserEmail(badToken));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void extractUserEmail_whenDifferentSignature_expectException() {
    ReflectionTestUtils.setField(
        jwtService, "secretKey", "9CSl1rBlVaK0rugrqEH1YcdMeFAITc4a87wK5tgLR3a");

    JwtInvalidTokenException ex =
        assertThrows(
            JwtInvalidTokenException.class, () -> jwtService.extractUserEmail(this.validToken));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void extractUserEmail_whenUnexpectedException_expectException() {
    // This test is written with MockedStatic suggested by Claude
    try (MockedStatic<Jwts> jwtsMock = mockStatic(Jwts.class)) {
      JwtParserBuilder mockParserBuilder = mock(JwtParserBuilder.class);
      JwtParser mockParser = mock(JwtParser.class);

      // Setup mock chain
      jwtsMock.when(Jwts::parserBuilder).thenReturn(mockParserBuilder);
      when(mockParserBuilder.setSigningKey(any(Key.class))).thenReturn(mockParserBuilder);
      when(mockParserBuilder.build()).thenReturn(mockParser);

      // Make it throw a generic JwtException
      when(mockParser.parseClaimsJws(anyString()))
          .thenThrow(new JwtException("Unexpected JWT error"));

      JwtServiceFailException ex =
          assertThrows(
              JwtServiceFailException.class,
              () -> {
                jwtService.extractUserEmail(this.validToken);
              });

      assertTrue(ex.getMessage().contains("Unexpected JWT error"));
    }
  }

  @Test
  public void isTokenValid_whenUserDetailMatch_expectTrue() {
    boolean isValid = this.jwtService.isTokenValid(this.validToken, this.userDetails);
    assertTrue(isValid);
  }

  @Test
  public void isTokenValid_whenUserDetailNotMatch_expectFalse() {
    UserDetails fooUser = new UserModel("notFoo@mail.com", "password123", "foo", "woof", 123);
    boolean isValid = this.jwtService.isTokenValid(this.validToken, fooUser);
    assertFalse(isValid);
  }

  @Test
  public void isTokenValid_whenTokenAlreadyExpired_expectException() {
    ReflectionTestUtils.setField(
        jwtService, "jwtExpirationInMilliseconds", -1000L); // Set expiration date to the past
    String expiredToken = this.jwtService.generateToken(userDetails);
    JwtExpiredException ex =
        assertThrows(
            JwtExpiredException.class,
            () -> this.jwtService.isTokenValid(expiredToken, this.userDetails));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void isTokenValid_whenTokenWasModified_expectException() {
    String badToken = "this.is.not.a.valid.jwt";
    JwtInvalidTokenException ex =
        assertThrows(
            JwtInvalidTokenException.class,
            () -> this.jwtService.isTokenValid(badToken, this.userDetails));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void isTokenValid_whenTokenNull_expectException() {
    JwtInvalidTokenException ex =
        assertThrows(
            JwtInvalidTokenException.class, () -> jwtService.isTokenValid(null, this.userDetails));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }

  @Test
  public void isTokenValid_whenTokenBlank_expectException() {
    JwtInvalidTokenException ex =
        assertThrows(
            JwtInvalidTokenException.class, () -> jwtService.isTokenValid("", this.userDetails));

    String errMessage = ex.getMessage();
    assertNotNull(errMessage);
    assertFalse(errMessage.isBlank());
  }
}
