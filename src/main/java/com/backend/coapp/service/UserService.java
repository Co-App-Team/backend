package com.backend.coapp.service;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
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
   * @param email user's email
   * @param oldPassword old password. Only update password if old password is correctly provided
   * @param newPassword new password to update
   * @throws UserServiceFailException if database fail to perform operation
   * @throws AuthEmailNotRegisteredException if there is no account associated with the email.
   * @throws AuthEmailNotRegisteredException if account not yet activated.
   * @throws AuthBadCredentialException if old password is incorrect.
   */
  public void udpateUserPassword(String email, String oldPassword, String newPassword)
      throws UserServiceFailException,
          AuthEmailNotRegisteredException,
          AuthAccountNotYetActivatedException,
          AuthBadCredentialException {
    UserModel user;
    try {
      user = this.userRepository.findUserModelByEmail(email);

    } catch (Exception e) {
      throw new UserServiceFailException("Fail to update your password. Please try again later");
    }
    if (user == null) {
      throw new AuthEmailNotRegisteredException();
    }

    if (!user.getVerified()) {
      throw new AuthAccountNotYetActivatedException();
    }

    if (!user.getPassword().equals(oldPassword)) {
      throw new AuthBadCredentialException("Incorrect password.");
    }

    try {
      user.setPassword(newPassword);
      this.userRepository.save(user);

    } catch (Exception e) {
      throw new UserServiceFailException("Fail to update your password. Please try again later");
    }
  }
}
