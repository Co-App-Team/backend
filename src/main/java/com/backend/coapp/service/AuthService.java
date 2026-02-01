package com.backend.coapp.service;

import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserRepository;
import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Authentication Service
 *
 * <p>This handles all business logic related to authentication.
 */
@Service
@Slf4j
@Getter // For testing
public class AuthService {

  public static final int NUMS_VERIFICATION_CODE = 6;

  /** Singleton service and repository * */
  private final UserRepository userRepository;

  private final EmailService emailService;

  @Autowired
  public AuthService(UserRepository userRepository, EmailService emailService) {
    this.userRepository = userRepository;
    this.emailService = emailService;
  }

  /**
   * Create a new user and send verification code to user email.
   *
   * @param email user email
   * @param password password
   * @param firstName User first name
   * @param lastName User last name
   * @throws AuthEmailAlreadyUsedException if provided email has been used with an existing account
   * @throws EmailServiceException if there is a failure in EmailService
   * @throws EmailInvalidAddressException if provided email has invalid format
   */
  public void createNewUser(String email, String password, String firstName, String lastName)
      throws AuthEmailAlreadyUsedException, EmailServiceException, EmailInvalidAddressException {
    UserModel userIfExist = this.userRepository.findUserModelByEmail(email);

    if (userIfExist != null) {
      log.warn("WARNING LOG: User tried to create a new account with an email that has been used.");
      throw new AuthEmailAlreadyUsedException();
    } else {
      int verificationCode = this.generateVerificationCode();
      String emailSubject = "Verification code for your new account";
      String emailBody = this.generateEmailBodyWithVerificationCode(verificationCode);

      UserModel newUser = new UserModel(email, password, firstName, lastName, verificationCode);
      this.userRepository.save(newUser);

      this.emailService.sendEmail(email, emailSubject, emailBody);
    }
  }

  /**
   * Verify confirmation code to activate new account
   *
   * @param email user email
   * @param verifyCode verification code
   * @throws AuthEmailNotRegisteredException if there aren't any accounts associated with provided
   *     email.
   */
  public void verifyUser(String email, int verifyCode)
      throws AuthEmailNotRegisteredException, IncorrectCodeException {
    UserModel user = this.userRepository.findUserModelByEmail(email);

    if (user == null) {
      throw new AuthEmailNotRegisteredException();
    } else {
      boolean verified = user.getVerified();
      if (!verified) {
        user.setVerified(user.getVerificationCode() == verifyCode);
        verified = user.getVerified();
        if (verified) {
          user.setVerificationCode(UserModel.DEFAULT_VERIFICATION_CODE);
          this.userRepository.save(user);
        } else {
          throw new IncorrectCodeException();
        }
      }
    }
  }

  /**
   * Reset verification code and resend the new code to user email.
   *
   * @param email user email
   * @throws EmailServiceException if there is failure with EmailService
   * @throws AuthEmailNotRegisteredException if there aren't any accounts associated with provided
   *     email.
   */
  public void resetVerifyCode(String email)
      throws EmailServiceException, AuthEmailNotRegisteredException {
    UserModel user = this.userRepository.findUserModelByEmail(email);

    if (user == null) {
      throw new AuthEmailNotRegisteredException();
    } else {
      int newVerifyCode = this.generateVerificationCode();

      String emailSubject = "New verification code";
      String emailBody = this.generateEmailBodyWithVerificationCode(newVerifyCode);
      user.setVerificationCode(newVerifyCode);
      this.userRepository.save(user);

      this.emailService.sendEmail(email, emailSubject, emailBody);
    }
  }

  /**
   * @param email Email associated to an account that client wants to reset password
   * @throws AuthEmailNotRegisteredException when there aren't any accounts associated with the
   *     email
   * @throws AuthAccountNotYetActivatedException when the account has not been activated yet.
   */
  public void forgotPassword(String email)
      throws AuthEmailNotRegisteredException, AuthAccountNotYetActivatedException {
    UserModel user = this.userRepository.findUserModelByEmail(email);
    if (user == null) {
      throw new AuthEmailNotRegisteredException();
    } else if (!user.getVerified()) {
      throw new AuthAccountNotYetActivatedException();
    }
    int newVerifyCode = this.generateVerificationCode();

    String emailSubject = "Forgot password? New verification code";
    String emailBody = this.generateEmailBodyWithVerificationCode(newVerifyCode);
    user.setForgotPasswordCode(newVerifyCode);
    this.userRepository.save(user);

    this.emailService.sendEmail(email, emailSubject, emailBody);
  }

  /**
   * @param email Email associated to an account that client wants to reset password
   * @param forgotPasswordCode Forgot password code provided by client
   * @param newPassword New password
   * @throws AuthEmailNotRegisteredException when there aren't any accounts associated with the
   *     email
   * @throws AuthAccountNotYetActivatedException when the account has not been activated yet.
   * @return true if forgot password code match the code sent to user and update password
   *     successfully; false otherwise
   */
  public void updatePassword(String email, Integer forgotPasswordCode, String newPassword)
      throws AuthEmailNotRegisteredException,
          AuthAccountNotYetActivatedException,
          IncorrectCodeException {
    UserModel user = this.userRepository.findUserModelByEmail(email);
    if (user == null) {
      throw new AuthEmailNotRegisteredException();
    } else if (!user.getVerified()) {
      throw new AuthAccountNotYetActivatedException();
    }

    boolean updatedPassword = user.getForgotPasswordCode().equals(forgotPasswordCode);
    if (updatedPassword) {
      user.setPassword(newPassword);
      user.setForgotPasswordCode(UserModel.DEFAULT_VERIFICATION_CODE);
      this.userRepository.save(user);
    } else {
      throw new IncorrectCodeException();
    }
  }

  /**
   * Generate verification code of NUMS_VERIFICATION_CODE digits.
   *
   * @return int
   */
  private int generateVerificationCode() {
    Random random = new Random();
    int lowerBound = (int) Math.pow(10, NUMS_VERIFICATION_CODE - 1);
    int upperRange = lowerBound * 9;
    return lowerBound + random.nextInt(upperRange);
  }

  private String generateEmailBodyWithVerificationCode(int verificationCode) {
    return """
                Dear user,

                Your confirmation code is: %d

                Please do NOT share this code.

                Thank you,
                CoApp Team.
                """
        .formatted(verificationCode);
  }
}
