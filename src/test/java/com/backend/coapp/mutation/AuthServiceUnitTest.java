package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.auth.*;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.AuthService;
import com.backend.coapp.service.EmailService;
import com.backend.coapp.service.JwtService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthServiceUnitTest {

  private AuthService authService;
  private UserRepository mockUserRepo;
  private EmailService mockEmailService;
  private AuthenticationManager mockAuthManager;
  private JwtService mockJwtService;
  private PasswordEncoder mockPasswordEncoder;

  private UserModel fooUserNotActivated;
  private UserModel fooUserActivated;
  private final String rawPassword = "password123";
  private final String encodedPassword = "encodedPassword123";

  @BeforeEach
  void setUp() {
    mockUserRepo = Mockito.mock(UserRepository.class);
    mockEmailService = Mockito.mock(EmailService.class);
    mockAuthManager = Mockito.mock(AuthenticationManager.class);
    mockJwtService = Mockito.mock(JwtService.class);
    mockPasswordEncoder = Mockito.mock(PasswordEncoder.class);

    fooUserNotActivated =
        new UserModel("123", "foo@mail.com", encodedPassword, "foo", "woof", false, 123);

    fooUserActivated =
        new UserModel(
            "789",
            "fooActivated@mail.com",
            encodedPassword,
            "fooActivated",
            "woof",
            true,
            UserModel.DEFAULT_VERIFICATION_CODE);

    authService =
        new AuthService(
            mockUserRepo, mockEmailService, mockAuthManager, mockJwtService, mockPasswordEncoder);
  }

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  @Test
  void constructor_expectUsedInstancesPassed() {
    assertEquals(mockUserRepo, authService.getUserRepository());
    assertEquals(mockEmailService, authService.getEmailService());
  }

  // -------------------------------------------------------------------------
  // createNewUser
  // -------------------------------------------------------------------------

  @Test
  void createNewUser_whenEmailAlreadyUsed_expectException() {
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);

    assertThrows(
        AuthEmailAlreadyUsedException.class,
        () -> authService.createNewUser("foo@mail.com", "newpassword", "foo", "woof"));
  }

  @Test
  void createNewUser_whenNewEmail_expectUserSavedWithCorrectFields() {
    when(mockUserRepo.findUserModelByEmail("newUser@mail.com")).thenReturn(null);
    when(mockPasswordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
    when(mockUserRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    authService.createNewUser("newUser@mail.com", "newPassword", "user", "new");

    ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
    verify(mockUserRepo).save(captor.capture());

    UserModel saved = captor.getValue();
    assertEquals("newUser@mail.com", saved.getEmail());
    assertEquals("encodedNewPassword", saved.getPassword());
    assertEquals("user", saved.getFirstName());
    assertEquals("new", saved.getLastName());
    assertFalse(saved.getVerified());
    assertNotEquals(UserModel.DEFAULT_VERIFICATION_CODE, saved.getVerificationCode());
  }

  @Test
  void createNewUser_whenNewEmail_expectEmailSentWithVerificationCode() {
    when(mockUserRepo.findUserModelByEmail("newUser@mail.com")).thenReturn(null);
    when(mockPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(mockUserRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    authService.createNewUser("newUser@mail.com", "newPassword", "user", "new");

    ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
    verify(mockUserRepo).save(userCaptor.capture());
    int savedCode = userCaptor.getValue().getVerificationCode();

    ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockEmailService)
        .sendEmail(eq("newUser@mail.com"), anyString(), emailBodyCaptor.capture());

    String regex = String.format("(?s).*\\b\\d{%d}\\b.*", AuthService.NUMS_VERIFICATION_CODE);
    assertTrue(emailBodyCaptor.getValue().matches(regex));

    Pattern pattern = Pattern.compile("\\b\\d+\\b");
    Matcher matcher = pattern.matcher(emailBodyCaptor.getValue());
    assertTrue(matcher.find());
    assertEquals(savedCode, Integer.parseInt(matcher.group()));
  }

  @Test
  void createNewUser_whenNewEmail_expectVerificationCodeIsValidRange() {
    when(mockUserRepo.findUserModelByEmail("newUser@mail.com")).thenReturn(null);
    when(mockPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(mockUserRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    authService.createNewUser("newUser@mail.com", "newPassword", "user", "new");

    ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockEmailService)
        .sendEmail(eq("newUser@mail.com"), anyString(), emailBodyCaptor.capture());

    Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
    Matcher matcher = pattern.matcher(emailBodyCaptor.getValue());
    assertTrue(matcher.find());
    int code = Integer.parseInt(matcher.group());

    int lowerBound = (int) Math.pow(10, AuthService.NUMS_VERIFICATION_CODE - 1);
    int upperBound = (int) Math.pow(10, AuthService.NUMS_VERIFICATION_CODE) - 1;
    assertTrue(code >= lowerBound, "Code " + code + " is below lower bound " + lowerBound);
    assertTrue(code <= upperBound, "Code " + code + " is above upper bound " + upperBound);
  }

  // -------------------------------------------------------------------------
  // verifyUser
  // -------------------------------------------------------------------------

  @Test
  void verifyUser_whenUserNotYetRegistered_expectException() {
    when(mockUserRepo.findUserModelByEmail("notFoo@mail.com")).thenReturn(null);

    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> authService.verifyUser("notFoo@mail.com", 123));
  }

  @Test
  void verifyUser_whenUserAlreadyVerified_expectException() {
    fooUserNotActivated.setVerified(true);
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);

    assertThrows(
        AuthAccountAlreadyVerifyException.class, () -> authService.verifyUser("foo@mail.com", 000));
    verify(mockUserRepo, never()).save(any());
  }

  @Test
  void verifyUser_whenWrongCode_expectException() {
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);

    assertThrows(IncorrectCodeException.class, () -> authService.verifyUser("foo@mail.com", 000));
    verify(mockUserRepo, never()).save(any());
  }

  @Test
  void verifyUser_whenCorrectCode_expectUserSavedAsVerified() {
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);
    when(mockUserRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> authService.verifyUser("foo@mail.com", 123));

    ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
    verify(mockUserRepo).save(captor.capture());
    assertTrue(captor.getValue().getVerified());
    assertEquals(UserModel.DEFAULT_VERIFICATION_CODE, captor.getValue().getVerificationCode());
  }

  // -------------------------------------------------------------------------
  // resetVerifyCode
  // -------------------------------------------------------------------------

  @Test
  void resetVerifyCode_whenUserNotYetRegistered_expectException() {
    when(mockUserRepo.findUserModelByEmail("notFoo@mail.com")).thenReturn(null);

    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> authService.resetVerifyCode("notFoo@mail.com"));
  }

  @Test
  void resetVerifyCode_whenUserAlreadyVerified_expectException() {
    fooUserNotActivated.setVerified(true);
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);

    assertThrows(
        AuthAccountAlreadyVerifyException.class, () -> authService.resetVerifyCode("foo@mail.com"));
    verify(mockUserRepo, never()).save(any());
  }

  @Test
  void resetVerifyCode_whenUserRegistered_expectEmailSentWithNewCode() {
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);
    when(mockUserRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    authService.resetVerifyCode("foo@mail.com");

    ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
    verify(mockUserRepo).save(userCaptor.capture());
    int savedCode = userCaptor.getValue().getVerificationCode();

    ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockEmailService).sendEmail(eq("foo@mail.com"), anyString(), emailBodyCaptor.capture());

    String regex = String.format("(?s).*\\b\\d{%d}\\b.*", AuthService.NUMS_VERIFICATION_CODE);
    assertTrue(emailBodyCaptor.getValue().matches(regex));

    Pattern pattern = Pattern.compile("\\b\\d+\\b");
    Matcher matcher = pattern.matcher(emailBodyCaptor.getValue());
    assertTrue(matcher.find());
    assertEquals(savedCode, Integer.parseInt(matcher.group()));
  }

  // -------------------------------------------------------------------------
  // forgotPassword
  // -------------------------------------------------------------------------

  @Test
  void forgotPassword_whenUserNotYetRegistered_expectException() {
    when(mockUserRepo.findUserModelByEmail("notFoo@mail.com")).thenReturn(null);

    assertThrows(
        AuthEmailNotRegisteredException.class, () -> authService.forgotPassword("notFoo@mail.com"));
  }

  @Test
  void forgotPassword_whenAccountNotActivated_expectException() {
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);

    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () -> authService.forgotPassword("foo@mail.com"));
    verify(mockUserRepo, never()).save(any());
  }

  @Test
  void forgotPassword_whenAccountActivated_expectEmailSentWithCode() {
    when(mockUserRepo.findUserModelByEmail("fooActivated@mail.com")).thenReturn(fooUserActivated);
    when(mockUserRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    authService.forgotPassword("fooActivated@mail.com");

    ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
    verify(mockUserRepo).save(userCaptor.capture());
    int savedCode = userCaptor.getValue().getVerificationCode();

    ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockEmailService)
        .sendEmail(eq("fooActivated@mail.com"), anyString(), emailBodyCaptor.capture());

    String regex = String.format("(?s).*\\b\\d{%d}\\b.*", AuthService.NUMS_VERIFICATION_CODE);
    assertTrue(emailBodyCaptor.getValue().matches(regex));

    Pattern pattern = Pattern.compile("\\b\\d+\\b");
    Matcher matcher = pattern.matcher(emailBodyCaptor.getValue());
    assertTrue(matcher.find());
    assertEquals(savedCode, Integer.parseInt(matcher.group()));
  }

  // -------------------------------------------------------------------------
  // updatePassword
  // -------------------------------------------------------------------------

  @Test
  void updatePassword_whenUserNotYetRegistered_expectException() {
    when(mockUserRepo.findUserModelByEmail("notFoo@mail.com")).thenReturn(null);

    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> authService.updatePassword("notFoo@mail.com", 123, "newPassword"));
  }

  @Test
  void updatePassword_whenUserNotYetActivated_expectException() {
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);

    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () -> authService.updatePassword("foo@mail.com", 123, "newPassword"));
    verify(mockUserRepo, never()).save(any());
  }

  @Test
  void updatePassword_whenWrongCode_expectException() {
    when(mockUserRepo.findUserModelByEmail("fooActivated@mail.com")).thenReturn(fooUserActivated);

    assertThrows(
        IncorrectCodeException.class,
        () -> authService.updatePassword("fooActivated@mail.com", 000, "newPassword123"));
    verify(mockUserRepo, never()).save(any());
  }

  @Test
  void updatePassword_whenCorrectCode_expectPasswordUpdatedAndCodeReset() {
    fooUserActivated.setVerificationCode(654321); // must be non-default to catch the mutation
    when(mockUserRepo.findUserModelByEmail("fooActivated@mail.com")).thenReturn(fooUserActivated);
    when(mockPasswordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
    when(mockUserRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(
        () -> authService.updatePassword("fooActivated@mail.com", 654321, "newPassword123"));

    ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
    verify(mockUserRepo).save(captor.capture());
    assertEquals("encodedNewPassword", captor.getValue().getPassword());
    assertEquals(UserModel.DEFAULT_VERIFICATION_CODE, captor.getValue().getVerificationCode());
  }

  // -------------------------------------------------------------------------
  // getTokenExpireDurationInSeconds
  // -------------------------------------------------------------------------

  @Test
  void getTokenExpireDurationInSeconds_expectExpireDurationFromJwtService() {
    when(mockJwtService.getExpirationDurationInMilliseconds()).thenReturn(100000L);

    assertEquals(100L, authService.getTokenExpireDurationInSeconds());
  }

  // -------------------------------------------------------------------------
  // login
  // -------------------------------------------------------------------------

  @Test
  void login_whenSuccess_expectReturnToken() {
    when(mockUserRepo.findUserModelByEmail("fooActivated@mail.com")).thenReturn(fooUserActivated);
    when(mockPasswordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
    when(mockAuthManager.authenticate(any()))
        .thenReturn(
            new UsernamePasswordAuthenticationToken("fooActivated@mail.com", encodedPassword));
    when(mockJwtService.generateToken(any())).thenReturn("DummyToken");

    assertEquals("DummyToken", authService.login("fooActivated@mail.com", rawPassword));
  }

  @Test
  void login_whenAccountNotActivated_expectException() {
    when(mockUserRepo.findUserModelByEmail("foo@mail.com")).thenReturn(fooUserNotActivated);
    when(mockAuthManager.authenticate(any()))
        .thenReturn(new UsernamePasswordAuthenticationToken("foo@mail.com", encodedPassword));

    assertThrows(
        AuthBadCredentialException.class, () -> authService.login("foo@mail.com", rawPassword));
  }

  @Test
  void login_whenIncorrectPassword_expectException() {
    when(mockUserRepo.findUserModelByEmail("fooActivated@mail.com")).thenReturn(fooUserActivated);
    when(mockAuthManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThrows(
        AuthBadCredentialException.class,
        () -> authService.login("fooActivated@mail.com", "fooPassword"));
  }

  @Test
  void login_whenAccountNotExist_expectException() {
    when(mockUserRepo.findUserModelByEmail("user.not.registered@mail.com")).thenReturn(null);
    when(mockAuthManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThrows(
        AuthBadCredentialException.class,
        () -> authService.login("user.not.registered@mail.com", "fooPassword"));
  }

  @Test
  void login_whenUnknownJWTFail_expectException() {
    when(mockUserRepo.findUserModelByEmail("fooActivated@mail.com")).thenReturn(fooUserActivated);
    when(mockAuthManager.authenticate(any()))
        .thenReturn(
            new UsernamePasswordAuthenticationToken("fooActivated@mail.com", encodedPassword));
    when(mockJwtService.generateToken(any())).thenThrow(new JwtServiceFailException("Foo fail"));

    assertThrows(
        AuthBadCredentialException.class,
        () -> authService.login("fooActivated@mail.com", rawPassword));
  }

  @Test
  void login_whenUnknownDBFailure_expectException() {
    when(mockUserRepo.findUserModelByEmail(anyString())).thenThrow(new RuntimeException());

    assertThrows(
        AuthenticationServiceException.class,
        () -> authService.login("fooActivated@mail.com", encodedPassword));
  }
}
