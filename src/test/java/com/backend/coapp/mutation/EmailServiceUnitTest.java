package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.auth.EmailInvalidAddressException;
import com.backend.coapp.exception.auth.EmailServiceException;
import com.backend.coapp.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/* these tests were written with the help of Claude Sonnet 4.6 and revised by Eric Hodgson */
class EmailServiceUnitTest {

  private EmailService emailService;
  private JavaMailSender mockMailSender;

  private final String validEmail = "test@example.com";
  private final String validSubject = "Test Subject";
  private final String validBody = "Test body content";

  @BeforeEach
  void setUp() {
    mockMailSender = Mockito.mock(JavaMailSender.class);
    emailService = new EmailService(mockMailSender);
  }

  // -------------------------------------------------------------------------
  // sendEmail — subject validation
  // -------------------------------------------------------------------------

  @ParameterizedTest(name = "sendEmail_whenSubjectIsInvalid [{index}] subject=\"{0}\"")
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void sendEmail_whenSubjectIsNullOrBlankOrEmpty_expectException(String subject) {
    assertThrows(
        EmailServiceException.class, () -> emailService.sendEmail(validEmail, subject, validBody));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  // -------------------------------------------------------------------------
  // sendEmail — body validation
  // -------------------------------------------------------------------------

  @ParameterizedTest(name = "sendEmail_whenBodyIsInvalid [{index}] body=\"{0}\"")
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void sendEmail_whenBodyIsNullOrBlankOrEmpty_expectException(String body) {
    assertThrows(
        EmailServiceException.class, () -> emailService.sendEmail(validEmail, validSubject, body));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  // -------------------------------------------------------------------------
  // sendEmail — email validation
  // -------------------------------------------------------------------------

  @ParameterizedTest(name = "sendEmail_whenEmailIsInvalid [{index}] email=\"{0}\"")
  @NullSource
  @ValueSource(strings = {"notanemail", "user@", "userexample.com"})
  void sendEmail_whenEmailIsInvalid_expectException(String email) {
    assertThrows(
        EmailInvalidAddressException.class,
        () -> emailService.sendEmail(email, validSubject, validBody));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  // -------------------------------------------------------------------------
  // sendEmail — success
  // -------------------------------------------------------------------------

  @Test
  void sendEmail_whenValidInputs_expectMailSenderCalled() {
    assertDoesNotThrow(() -> emailService.sendEmail(validEmail, validSubject, validBody));
    verify(mockMailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_whenValidInputs_expectCorrectMessageFields() {
    emailService.sendEmail(validEmail, validSubject, validBody);

    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mockMailSender).send(captor.capture());

    SimpleMailMessage sent = captor.getValue();
    assertArrayEquals(new String[] {validEmail}, sent.getTo());
    assertEquals(validSubject, sent.getSubject());
    assertEquals(validBody, sent.getText());
  }

  // -------------------------------------------------------------------------
  // sendEmail — mail sender failure
  // -------------------------------------------------------------------------

  @Test
  void sendEmail_whenMailSenderThrows_expectEmailServiceException() {
    doThrow(new MailSendException("SMTP failure"))
        .when(mockMailSender)
        .send(any(SimpleMailMessage.class));

    assertThrows(
        EmailServiceException.class,
        () -> emailService.sendEmail(validEmail, validSubject, validBody));
  }
}
