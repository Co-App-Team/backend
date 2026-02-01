package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.AuthAccountNotYetActivatedException;
import com.backend.coapp.exception.AuthEmailAlreadyUsedException;
import com.backend.coapp.exception.AuthEmailNotRegisteredException;
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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class AuthServiceTest {
  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private UserRepository userRepository;
  private EmailService emailService;
  private AuthService authService;
  private UserModel fooUserNotActivated;
  private UserModel fooUserActivated;

  @BeforeEach
  public void setUp() {
    this.userRepository.deleteAll();
    this.fooUserNotActivated = new UserModel("foo@mail.com", "password123", "foo", "woof", 123);
    this.userRepository.save(fooUserNotActivated);
    this.fooUserActivated =
        new UserModel(
            "fooActivated@mail.com",
            "password123",
            "fooActivated",
            "woof",
            UserModel.DEFAULT_VERIFICATION_CODE);
    this.fooUserActivated.setVerified(true);
    this.fooUserActivated.setForgotPasswordCode(789);
    this.userRepository.save(this.fooUserActivated);
    this.emailService = Mockito.mock(EmailService.class);
    this.authService = new AuthService(this.userRepository, this.emailService);
  }

  @Test
  public void constructor_expectUsedInstancesPassed() {
    assertEquals(this.userRepository, this.authService.getUserRepository());
    assertEquals(this.emailService, this.authService.getEmailService());
  }

  @Test
  public void createNewUser_whenEmailAlreadyUsed_expectException() {
    assertThrows(
        AuthEmailAlreadyUsedException.class,
        () ->
            this.authService.createNewUser(
                this.fooUserNotActivated.getEmail(), "newpassword", "foo", "woof"));
  }

  @Test
  public void createNewUser_whenNewEmailIsUsed_expectNewUserCreatedInDatabase() {
    this.authService.createNewUser("newUser@mail.com", "newPassword", "user", "new");
    UserModel newUser = this.userRepository.findUserModelByEmail("newUser@mail.com");

    assertNotNull(newUser);
    assertEquals("newUser@mail.com", newUser.getEmail());
    assertEquals("newPassword", newUser.getPassword());
    assertEquals("user", newUser.getFirstName());
    assertEquals("new", newUser.getLastName());
    assertNotEquals(UserModel.DEFAULT_VERIFICATION_CODE, newUser.getVerificationCode());
    assertNotNull(newUser.getId());
  }

  @Test
  public void createNewUser_whenNewEmailIsUsed_expectEmailSentWith() {
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
  public void verifyUser_whenUserNotYetRegistered_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.authService.verifyUser("notFoo@mail.com", 123));
  }

  @Test
  public void verifyUser_whenUserAlreadyRegisteredWithWrongCode_expectFalseVerificationStatus() {
    boolean verify = this.authService.verifyUser(this.fooUserNotActivated.getEmail(), 000);
    assertFalse(verify);
    assertFalse(
        this.userRepository
            .findUserModelByEmail(this.fooUserNotActivated.getEmail())
            .getVerified());
  }

  @Test
  public void verifyUser_whenUserAlreadyRegisteredWithCorrectCode_expectTrueVerificationStatus() {
    boolean verify = this.authService.verifyUser(this.fooUserNotActivated.getEmail(), 123);
    assertTrue(verify);
    assertTrue(
        this.userRepository
            .findUserModelByEmail(this.fooUserNotActivated.getEmail())
            .getVerified());
  }

  @Test
  public void verifyUser_whenUserAlreadyVerified_expectTrueVerificationStatus() {
    this.fooUserNotActivated.setVerified(true);
    this.userRepository.save(this.fooUserNotActivated);

    boolean verify = this.authService.verifyUser(this.fooUserNotActivated.getEmail(), 000);
    assertTrue(verify);
    assertTrue(
        this.userRepository
            .findUserModelByEmail(this.fooUserNotActivated.getEmail())
            .getVerified());
  }

  @Test
  public void resetVerifyCode_whenUserNotYetRegistered_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.authService.resetVerifyCode("notFoo@mail.com"));
  }

  @Test
  public void resetVerifyCode_whenUserAlreadyRegistered_expectEmailSentWithCode() {
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
  public void forgotPasswordCode_whenUserNotYetRegistered_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.authService.forgotPassword("notFoo@mail.com"));
  }

  @Test
  public void forgotPasswordCode_whenAccountNotActivatedYet_expectException() {
    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () -> this.authService.forgotPassword(this.fooUserNotActivated.getEmail()));
  }

  @Test
  public void forgotPasswordCode_whenAccountAlreadyActivated_expectEmailSentWithCode() {
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

    assertEquals(currFooUser.getForgotPasswordCode(), codeInEmail);
  }

  ///

  @Test
  public void updatePassword_whenUserNotYetRegistered_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.authService.updatePassword("notFoo@mail.com", 123, "newPassword"));
  }

  @Test
  public void updatePassword_whenUserNotYetActivate_expectException() {
    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () ->
            this.authService.updatePassword(
                this.fooUserNotActivated.getEmail(), 123, "newPassword"));
  }

  @Test
  public void updatePassword_whenUserAlreadyActivatedWithWrongCode_expectFalseVerificationStatus() {
    boolean update =
        this.authService.updatePassword(this.fooUserActivated.getEmail(), 000, "newPassword123");
    assertFalse(update);
    UserModel userFromDatabase =
        this.userRepository.findUserModelByEmail(this.fooUserActivated.getEmail());
    assertEquals(this.fooUserActivated.getPassword(), userFromDatabase.getPassword());
    assertEquals(
        this.fooUserActivated.getForgotPasswordCode(), userFromDatabase.getForgotPasswordCode());
  }

  @Test
  public void verifyUser_whenUserAlreadyRegisteredCorrectCode_expectTrueVerificationStatus() {
    boolean update =
        this.authService.updatePassword(
            this.fooUserActivated.getEmail(),
            this.fooUserActivated.getForgotPasswordCode(),
            "newPassword123");
    assertTrue(update);
    UserModel userFromDatabase =
        this.userRepository.findUserModelByEmail(this.fooUserActivated.getEmail());
    assertEquals("newPassword123", userFromDatabase.getPassword());
    assertEquals(UserModel.DEFAULT_VERIFICATION_CODE, userFromDatabase.getForgotPasswordCode());
  }
}
