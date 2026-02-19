package com.backend.coapp.service;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User Service
 *
 * <p>This handles all business logic related to User.
 */
@Slf4j
@Service
@Getter // For testing only
public class UserService {
  /** Singleton service and repository * */
  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * This is function is a proof of concept.
   *
   * @return String
   */
  public UserResponse getDummyUser() {
    return new UserResponse("Dummy Firstname", "Dummy Lastname", "foo@mail.com");
  }

  /**
   * Update user password
   *
   * @param userID user's ID
   * @param oldPassword old password. Only update password if old password is correctly provided
   * @param newPassword new password to update
   * @throws UserServiceFailException if database fail to perform operation
   * @throws AuthEmailNotRegisteredException if there is no account associated with the email.
   * @throws AuthEmailNotRegisteredException if account not yet activated.
   * @throws AuthBadCredentialException if old password is incorrect.
   */
  public void udpateUserPassword(String userID, String oldPassword, String newPassword)
      throws UserServiceFailException,
          AuthEmailNotRegisteredException,
          AuthAccountNotYetActivatedException,
          AuthBadCredentialException {
    UserModel user;
    try {
      user = this.userRepository.findUserModelById(userID);

    } catch (Exception e) {
      throw new UserServiceFailException("Fail to update your password. Please try again later");
    }
    if (user == null) {
      throw new AuthEmailNotRegisteredException();
    }

    if (!user.getVerified()) {
      throw new AuthAccountNotYetActivatedException();
    }

    if (!this.passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new AuthBadCredentialException("Incorrect password.");
    }

    try {
      user.setPassword(this.passwordEncoder.encode(newPassword));
      this.userRepository.save(user);

    } catch (Exception e) {
      throw new UserServiceFailException("Fail to update your password. Please try again later");
    }
  }

  /**
   * Get user model by user ID
   *
   * @param userID ID of an user
   * @return UserModel
   * @throws UserServiceFailException when DB fails to search for the user
   * @throws UserNotExistException when user not exist
   */
  public UserModel getUserInformationFromUserID(String userID)
      throws UserServiceFailException, UserNotExistException {
    UserModel user;
    try {
      user = this.userRepository.findUserModelById(userID);

    } catch (Exception e) {
      throw new UserServiceFailException(
          "Fail to retrieve user information. Please try again later");
    }
    if (user == null) {
      throw new UserNotExistException();
    }
    return user;
  }
}
