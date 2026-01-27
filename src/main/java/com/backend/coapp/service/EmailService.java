package com.backend.coapp.service;

import com.backend.coapp.exception.EmailInvalidAddressException;
import com.backend.coapp.exception.EmailServiceException;
import java.util.regex.Pattern;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email Service
 *
 * <p>This handles all business logic related to sending email.
 */
@Service
public class EmailService {

  /** Singleton service and repository * */
  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * Sending an email to provided address with provided subject and body.
   *
   * @param targetEmail String email address to send to
   * @param subject String subject of the email
   * @param body String body of the email
   */
  public void sendEmail(String targetEmail, String subject, String body) {
    if (subject == null || subject.isBlank()) {
      throw new EmailServiceException("Email subject is null or empty.");
    }

    if (body == null || body.isBlank()) {
      throw new EmailServiceException("Email body is null or empty");
    }

    if (!validateEmail(targetEmail)) {
      throw new EmailInvalidAddressException("Invalid provided email: " + targetEmail);
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(targetEmail);
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);
    } catch (MailException internalMainException) {
      throw new EmailServiceException(internalMainException.getMessage());
    }
  }

  /**
   * This helper function is responsible for validating emails
   *
   * <p>Reference: https://www.geeksforgeeks.org/java/check-email-address-valid-not-java/
   *
   * @param email String email
   * @return boolean
   */
  private boolean validateEmail(String email) {
    String emailRegex =
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    Pattern p = Pattern.compile(emailRegex);
    return email != null && p.matcher(email).matches();
  }
}
