package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.EmailInvalidAddressException;
import com.backend.coapp.exception.EmailServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/** Parts of the unit test are written with help of Claude (Sonnet 4.6) */
public class EmailServiceTest {
  private JavaMailSender mailSender;
  private EmailService emailService;

  @BeforeEach
  public void setUp() {
    this.mailSender = Mockito.mock(JavaMailSender.class);
    this.emailService = new EmailService(mailSender);
  }

  @Test
  public void sendEmail_whenTargetEmailInvalid_expectException() {
    assertThrows(
        EmailInvalidAddressException.class,
        () -> this.emailService.sendEmail("", "Foo Subject", "Woof woof"));
    assertThrows(
        EmailInvalidAddressException.class,
        () -> this.emailService.sendEmail(null, "Foo Subject", "Woof woof"));
    assertThrows(
        EmailInvalidAddressException.class,
        () -> this.emailService.sendEmail("foo", "Foo Subject", "Woof woof"));
    assertThrows(
        EmailInvalidAddressException.class,
        () -> this.emailService.sendEmail("foo@", "Foo Subject", "Woof woof"));
    assertThrows(
        EmailInvalidAddressException.class,
        () -> this.emailService.sendEmail("foo.com", "Foo Subject", "Woof woof"));
    assertThrows(
        EmailInvalidAddressException.class,
        () -> this.emailService.sendEmail("foo@mail.", "Foo Subject", "Woof woof"));
    assertThrows(
        EmailInvalidAddressException.class,
        () -> this.emailService.sendEmail("@mail.com", "Foo Subject", "Woof woof"));
  }

  @Test
  public void sendEmail_whenSubjectInvalid_expectException() {
    assertThrows(
        EmailServiceException.class,
        () -> this.emailService.sendEmail("foo@mail.com", null, "Woof woof"));
    assertThrows(
        EmailServiceException.class,
        () -> this.emailService.sendEmail("foo@mail.com", "", "Woof woof"));
  }

  @Test
  public void sendEmail_whenBodyInvalid_expectException() {
    assertThrows(
        EmailServiceException.class,
        () -> this.emailService.sendEmail("foo@mail.com", "foo", null));
    assertThrows(
        EmailServiceException.class, () -> this.emailService.sendEmail("foo@mail.com", "foo", ""));
  }

  @Test
  public void sendEmail_whenMailServiceFail_expectException() {
    doThrow(new MailException("Mail Sender fail.") {})
        .when(this.mailSender)
        .send(any(SimpleMailMessage.class));
    EmailServiceException exception =
        assertThrows(
            EmailServiceException.class,
            () -> emailService.sendEmail("foo@mail.com", "foo", "woof woof"));

    assertEquals("JavaMailSender Service Failure. Mail Sender fail.", exception.getMessage());
  }

  @Test
  public void sendEmail_whenSendSuccessfully_expectNoException() {
    this.emailService.sendEmail("foo@mail.com", "Foo", "woof woof");

    ArgumentCaptor<SimpleMailMessage> captorMessage =
        ArgumentCaptor.forClass(SimpleMailMessage.class);

    verify(this.mailSender, times(1)).send(captorMessage.capture());
    SimpleMailMessage sentMessage = captorMessage.getValue();
    assertEquals("foo@mail.com", sentMessage.getTo()[0]);
    assertEquals("Foo", sentMessage.getSubject());
    assertEquals("woof woof", sentMessage.getText());
  }
}
