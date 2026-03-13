package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.auth.*;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserRepository;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Parts of the unit test are written with help of Claude (Sonnet 4.6) */
@SpringBootTest
@Testcontainers
class AuthServiceTest {
  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private AuthenticationManager authenticationManager;

  private EmailService emailService;
  private AuthService authService;
  private JwtService jwtService;
  private UserModel fooUserNotActivated;
  private UserModel fooUserActivated;
  private String rawPassword = "password123";

  @BeforeEach
  void setUp() {
    this.userRepository.deleteAll();
    this.fooUserNotActivated =
        new UserModel(
            "123",
            "foo@mail.com",
            this.passwordEncoder.encode(rawPassword),
            "foo",
            "woof",
            false,
            123);
    this.userRepository.save(fooUserNotActivated);
    this.fooUserActivated =
        new UserModel(
            "789",
            "fooActivated@mail.com",
            this.passwordEncoder.encode(rawPassword),
            "fooActivated",
            "woof",
            true,
            UserModel.DEFAULT_VERIFICATION_CODE);
    this.userRepository.save(this.fooUserActivated);
    this.emailService = Mockito.mock(EmailService.class);

    this.jwtService = Mockito.mock(JwtService.class);
    this.authService =
        new AuthService(
            this.userRepository,
            this.emailService,
            this.authenticationManager,
            this.jwtService,
            this.passwordEncoder);
  }

  @Test
  void constructor_expectUsedInstancesPassed() {
    assertEquals(this.userRepository, this.authService.getUserRepository());
    assertEquals(this.emailService, this.authService.getEmailService());
  }

  @Test
  void createNewUser_whenEmailAlreadyUsed_expectException() {
    assertThrows(
        AuthEmailAlreadyUsedException.class,
        () ->
            this.authService.createNewUser(
                this.fooUserNotActivated.getEmail(), "newpassword", "foo", "woof"));
  }

  @Test
  void createNewUser_whenNewEmailIsUsed_expectNewUserCreatedInDatabase() {
    this.authService.createNewUser("newUser@mail.com", "newPassword", "user", "new");
    UserModel newUser = this.userRepository.findUserModelByEmail("newUser@mail.com");

    assertNotNull(newUser);
    assertEquals("newUser@mail.com", newUser.getEmail());
    assertTrue(this.passwordEncoder.matches("newPassword", newUser.getPassword()));
    assertEquals("user", newUser.getFirstName());
    assertEquals("new", newUser.getLastName());
    assertNotEquals(UserModel.DEFAULT_VERIFICATION_CODE, newUser.getVerificationCode());
    assertNotNull(newUser.getId());
  }

  @Test
  void createNewUser_whenNewEmailIsUsed_expectEmailSentWith() {
    this.authService.createNewUser("newUser@mail.com", "newPassword", "user", "new");
    UserModel expectUser = this.userRepository.findUserModelByEmail("newUser@mail.com");
    assertNotNull(expectUser);
    verify(this.emailService, times(1)).sendEmail(eq("newUser@mail.com"), anyString(), anyString());

    ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);

    verify(this.emailService)
        .sendEmail(eq("newUser@mail.com"), anyString(), emailBodyCaptor.capture());

    String regex =
        String.format(
            "(?s).*\\b\\d{%d}\\b.*",
            AuthService
                .NUMS_VERIFICATION_CODE); // Regex to check if the email body contains 6 digits.
    assertTrue(emailBodyCaptor.getValue().matches(regex));

    Pattern pattern = Pattern.compile("\\b\\d+\\b"); // To parse all numbers
    Matcher matcher = pattern.matcher(emailBodyCaptor.getValue());
    assertTrue(matcher.find());
    int codeInEmail = Integer.parseInt(matcher.group());

    assertEquals(expectUser.getVerificationCode(), codeInEmail);
  }

  @Test
  void verifyUser_whenUserNotYetRegistered_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.authService.verifyUser("notFoo@mail.com", 123));
  }

  @Test
  void verifyUser_whenUserAlreadyRegisteredWithWrongCode_expectFalseVerificationStatus() {
    assertThrows(
        IncorrectCodeException.class,
        () -> this.authService.verifyUser(this.fooUserNotActivated.getEmail(), 000));
    assertFalse(
        this.userRepository
            .findUserModelByEmail(this.fooUserNotActivated.getEmail())
            .getVerified());
  }

  @Test
  void verifyUser_whenUserAlreadyRegisteredWithCorrectCode_expectTrueVerificationStatus() {
    assertDoesNotThrow(() -> this.authService.verifyUser(this.fooUserNotActivated.getEmail(), 123));
    assertTrue(
        this.userRepository
            .findUserModelByEmail(this.fooUserNotActivated.getEmail())
            .getVerified());
  }

  @Test
  void verifyUser_whenUserAlreadyVerified_expectException() {
    this.fooUserNotActivated.setVerified(true);
    this.userRepository.save(this.fooUserNotActivated);

    assertThrows(
        AuthAccountAlreadyVerifyException.class,
        () -> this.authService.verifyUser(this.fooUserNotActivated.getEmail(), 000));
    assertTrue(
        this.userRepository
            .findUserModelByEmail(this.fooUserNotActivated.getEmail())
            .getVerified());
  }

  @Test
  void resetVerifyCode_whenUserNotYetRegistered_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.authService.resetVerifyCode("notFoo@mail.com"));
  }

  @Test
  void resetVerifyCode_whenUserAlreadyVerified_expectException() {
    this.fooUserNotActivated.setVerified(true);
    this.userRepository.save(this.fooUserNotActivated);

    assertThrows(
        AuthAccountAlreadyVerifyException.class,
        () -> this.authService.resetVerifyCode(this.fooUserNotActivated.getEmail()));
    assertTrue(
        this.userRepository
            .findUserModelByEmail(this.fooUserNotActivated.getEmail())
            .getVerified());
  }

  @Test
  void resetVerifyCode_whenUserAlreadyRegistered_expectEmailSentWithCode() {
    this.authService.resetVerifyCode(this.fooUserNotActivated.getEmail());
    UserModel currFooUser =
        this.userRepository.findUserModelByEmail(this.fooUserNotActivated.getEmail());
    assertNotNull(currFooUser);

    verify(this.emailService, times(1))
        .sendEmail(eq(this.fooUserNotActivated.getEmail()), anyString(), anyString());

    ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);

    verify(this.emailService)
        .sendEmail(eq(this.fooUserNotActivated.getEmail()), anyString(), emailBodyCaptor.capture());

    String regex =
        String.format(
            "(?s).*\\b\\d{%d}\\b.*",
            AuthService
                .NUMS_VERIFICATION_CODE); // Regex to check if the email body contains 6 digits.
    assertTrue(emailBodyCaptor.getValue().matches(regex));

    Pattern pattern = Pattern.compile("\\b\\d+\\b"); // To parse all numbers
    Matcher matcher = pattern.matcher(emailBodyCaptor.getValue());
    assertTrue(matcher.find());
    int codeInEmail = Integer.parseInt(matcher.group());

    assertEquals(currFooUser.getVerificationCode(), codeInEmail);
  }

  @Test
  void forgotPassword_whenUserNotYetRegistered_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.authService.forgotPassword("notFoo@mail.com"));
  }

  @Test
  void forgotPassword_whenAccountNotActivatedYet_expectException() {
    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () -> this.authService.forgotPassword(this.fooUserNotActivated.getEmail()));
  }

  @Test
  void forgotPassword_whenAccountAlreadyActivated_expectEmailSentWithCode() {
    this.authService.forgotPassword(this.fooUserActivated.getEmail());
    UserModel currFooUser =
        this.userRepository.findUserModelByEmail(this.fooUserActivated.getEmail());
    assertNotNull(currFooUser);

    verify(this.emailService, times(1))
        .sendEmail(eq(this.fooUserActivated.getEmail()), anyString(), anyString());

    ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);

    verify(this.emailService)
        .sendEmail(eq(this.fooUserActivated.getEmail()), anyString(), emailBodyCaptor.capture());

    String regex =
        String.format(
            "(?s).*\\b\\d{%d}\\b.*",
            AuthService
                .NUMS_VERIFICATION_CODE); // Regex to check if the email body contains 6 digits.
    assertTrue(emailBodyCaptor.getValue().matches(regex));

    Pattern pattern = Pattern.compile("\\b\\d+\\b"); // To parse all numbers
    Matcher matcher = pattern.matcher(emailBodyCaptor.getValue());
    assertTrue(matcher.find());
    int codeInEmail = Integer.parseInt(matcher.group());

    assertEquals(currFooUser.getVerificationCode(), codeInEmail);
  }

  @Test
  void updatePassword_whenUserNotYetRegistered_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.authService.updatePassword("notFoo@mail.com", 123, "newPassword"));
  }

  @Test
  void updatePassword_whenUserNotYetActivate_expectException() {
    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () ->
            this.authService.updatePassword(
                this.fooUserNotActivated.getEmail(), 123, "newPassword"));
  }

  @Test
  void updatePassword_whenUserAlreadyActivatedWithWrongCode_expectFalseVerificationStatus() {
    assertThrows(
        IncorrectCodeException.class,
        () ->
            this.authService.updatePassword(
                this.fooUserActivated.getEmail(), 000, "newPassword123"));
    UserModel userFromDatabase =
        this.userRepository.findUserModelByEmail(this.fooUserActivated.getEmail());
    assertEquals(this.fooUserActivated.getPassword(), userFromDatabase.getPassword());
    assertEquals(
        this.fooUserActivated.getVerificationCode(), userFromDatabase.getVerificationCode());
  }

  @Test
  void verifyUser_whenUserAlreadyRegisteredCorrectCode_expectTrueVerificationStatus() {
    assertDoesNotThrow(
        () ->
            this.authService.updatePassword(
                this.fooUserActivated.getEmail(),
                this.fooUserActivated.getVerificationCode(),
                "newPassword123"));
    UserModel userFromDatabase =
        this.userRepository.findUserModelByEmail(this.fooUserActivated.getEmail());
    assertTrue(this.passwordEncoder.matches("newPassword123", userFromDatabase.getPassword()));
    assertEquals(UserModel.DEFAULT_VERIFICATION_CODE, userFromDatabase.getVerificationCode());
  }

  @Test
  void getTokenExpireDurationInSeconds_expectExpireDurationFromJwtService() {
    when(this.jwtService.getExpirationDurationInMilliseconds()).thenReturn(100000L);

    assertEquals(100L, this.authService.getTokenExpireDurationInSeconds());
  }

  @Test
  void login_whenSuccess_expectReturnToken() {

    when(this.jwtService.generateToken(any())).thenReturn("DummyToken");
    String token = this.authService.login(this.fooUserActivated.getEmail(), this.rawPassword);

    assertEquals("DummyToken", token);
  }

  @Test
  void login_whenAccountNotActivate_expectException() {
    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () -> this.authService.login(this.fooUserNotActivated.getEmail(), this.rawPassword));
  }

  @Test
  void login_whenIncorrectPassword_expectException() {
    assertThrows(
        AuthBadCredentialException.class,
        () -> this.authService.login(this.fooUserActivated.getEmail(), "fooPassword"));
  }

  @Test
  void login_whenAccountNotExist_expectException() {
    assertThrows(
        AuthBadCredentialException.class,
        () -> this.authService.login("user.not.registerd@mail.com", "fooPassword"));
  }

  @Test
  void login_whenUnknownJWTFail_expectException() {

    when(this.jwtService.generateToken(any())).thenThrow(new JwtServiceFailException("Foo fail"));

    assertThrows(
        JwtServiceFailException.class,
        () -> this.authService.login(this.fooUserActivated.getEmail(), this.rawPassword));
  }

  @Test
  void login_whenUnknownDBFailure_expectException() {
    UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
    when(mockUserRepo.findUserModelByEmail(anyString())).thenThrow(new RuntimeException());
    this.authService =
        new AuthService(
            mockUserRepo,
            this.emailService,
            this.authenticationManager,
            this.jwtService,
            this.passwordEncoder);
    assertThrows(
        AuthenticationServiceException.class,
        () ->
            this.authService.login(
                this.fooUserActivated.getEmail(), this.fooUserActivated.getPassword()));
  }
}
