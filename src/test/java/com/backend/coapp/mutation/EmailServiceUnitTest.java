package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.auth.EmailInvalidAddressException;
import com.backend.coapp.exception.auth.EmailServiceException;
import com.backend.coapp.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailServiceUnitTest {

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

  @Test
  void sendEmail_whenSubjectIsNull_expectException() {
    assertThrows(
        EmailServiceException.class, () -> emailService.sendEmail(validEmail, null, validBody));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_whenSubjectIsBlank_expectException() {
    assertThrows(
        EmailServiceException.class, () -> emailService.sendEmail(validEmail, "   ", validBody));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_whenSubjectIsEmpty_expectException() {
    assertThrows(
        EmailServiceException.class, () -> emailService.sendEmail(validEmail, "", validBody));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  // -------------------------------------------------------------------------
  // sendEmail — body validation
  // -------------------------------------------------------------------------

  @Test
  void sendEmail_whenBodyIsNull_expectException() {
    assertThrows(
        EmailServiceException.class, () -> emailService.sendEmail(validEmail, validSubject, null));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_whenBodyIsBlank_expectException() {
    assertThrows(
        EmailServiceException.class, () -> emailService.sendEmail(validEmail, validSubject, "   "));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_whenBodyIsEmpty_expectException() {
    assertThrows(
        EmailServiceException.class, () -> emailService.sendEmail(validEmail, validSubject, ""));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  // -------------------------------------------------------------------------
  // sendEmail — email validation
  // -------------------------------------------------------------------------

  @Test
  void sendEmail_whenEmailIsNull_expectException() {
    assertThrows(
        EmailInvalidAddressException.class,
        () -> emailService.sendEmail(null, validSubject, validBody));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_whenEmailIsInvalid_expectException() {
    assertThrows(
        EmailInvalidAddressException.class,
        () -> emailService.sendEmail("notanemail", validSubject, validBody));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_whenEmailMissingDomain_expectException() {
    assertThrows(
        EmailInvalidAddressException.class,
        () -> emailService.sendEmail("user@", validSubject, validBody));
    verify(mockMailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_whenEmailMissingAtSign_expectException() {
    assertThrows(
        EmailInvalidAddressException.class,
        () -> emailService.sendEmail("userexample.com", validSubject, validBody));
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
