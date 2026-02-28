package com.backend.coapp.service;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.UserExperienceModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserExperienceRepository;
import com.backend.coapp.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
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

  private final UserExperienceRepository userExperienceRepository;
  private final CompanyRepository companyRepository;

  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      UserExperienceRepository userExperienceRepository,
      CompanyRepository companyRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userExperienceRepository = userExperienceRepository;
    this.companyRepository = companyRepository;
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
   * @throws UserInvalidPasswordChangeException if new password is same with old password.
   */
  public void updateUserPassword(String userID, String oldPassword, String newPassword)
      throws UserServiceFailException,
          AuthEmailNotRegisteredException,
          AuthAccountNotYetActivatedException,
          AuthBadCredentialException,
          UserInvalidPasswordChangeException {
    UserModel user;
    if (oldPassword.equals(newPassword)) {
      throw new UserInvalidPasswordChangeException();
    }
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
      throw new UserServiceFailException(e.getMessage());
    }
    if (user == null) {
      throw new UserNotExistException();
    }
    return user;
  }

  /**
   * Get all experience of the user
   *
   * @param userId ID of the user
   * @return List of user experience model
   */
  public List<UserExperienceModel> getAllUserExperience(String userId) {
    try {
      return this.userExperienceRepository.findAllByUserId(userId);
    } catch (Exception e) {
      throw new UserServiceFailException(e.getMessage());
    }
  }

  /**
   * Create new experience for the user
   *
   * @param userId ID of the user
   * @param companyId ID of the company of the experience
   * @param roleTitle role title of the experience
   * @param roleDescription description of the role
   * @param startDate start date of the experience
   * @param endDate end date of the experience (can be null - current job)
   * @return created experience
   */
  public UserExperienceModel createNewUserExperience(
      String userId,
      String companyId,
      String roleTitle,
      String roleDescription,
      LocalDate startDate,
      LocalDate endDate) {
    try {
      if (companyId == null) throw new UserServiceFailException("Company ID cannot be null.");
      if (roleTitle == null) throw new UserServiceFailException("Role title cannot be null.");
      if (roleDescription == null)
        throw new UserServiceFailException("Role description cannot be null.");
      if (startDate == null) throw new UserServiceFailException("Start date cannot be null.");
      if (endDate != null && endDate.isBefore(startDate)) {
        throw new UserServiceFailException("End date must be after start date.");
      }
      if (!this.companyRepository.existsById(companyId)) {
        throw new CompanyNotFoundException();
      }
      UserExperienceModel newUserExperience =
          new UserExperienceModel(
              userId, companyId, roleTitle, roleDescription, startDate, endDate);
      return this.userExperienceRepository.save(newUserExperience);
    } catch (CompanyNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new UserServiceFailException(e.getMessage());
    }
  }

  /**
   * Delete existing experience
   *
   * @param experienceId ID of the experience
   * @param userId ID of the user that the experience is supposed to belong to
   */
  public void deleteUserExperience(String experienceId, String userId) {
    try {
      UserExperienceModel experience =
          this.userExperienceRepository
              .findById(experienceId)
              .orElseThrow(ExperienceNotFoundException::new);

      if (!experience.getUserId().equals(userId)) {
        throw new ExperienceNotOwnException("Can NOT delete.");
      }
      this.userExperienceRepository.deleteById(experienceId);
    } catch (ExperienceNotOwnException | ExperienceNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new UserServiceFailException(e.getMessage());
    }
  }

  /**
   * Update user (for PUT operation). Need to pass all attributes, including values of both
   * unchanged and changed attributes
   *
   * @param experienceId ID of the experience
   * @param userId ID of the user
   * @param companyId ID of the company
   * @param roleTitle role title of the experience
   * @param roleDescription role description of the experience
   * @param startDate start date of the experience
   * @param endDate end date of the experience.
   */
  public void updateUserExperience(
      String experienceId,
      String userId,
      String companyId,
      String roleTitle,
      String roleDescription,
      LocalDate startDate,
      LocalDate endDate) {
    try {
      if (companyId == null) throw new UserServiceFailException("Company ID cannot be null.");
      if (roleTitle == null) throw new UserServiceFailException("Role title cannot be null.");
      if (roleDescription == null)
        throw new UserServiceFailException("Role description cannot be null.");
      if (startDate == null) throw new UserServiceFailException("Start date cannot be null.");
      if (endDate != null && endDate.isBefore(startDate)) {
        throw new UserServiceFailException("End date must be after start date.");
      }

      UserExperienceModel experience =
          this.userExperienceRepository
              .findById(experienceId)
              .orElseThrow(ExperienceNotFoundException::new);

      if (!experience.getUserId().equals(userId)) {
        throw new ExperienceNotOwnException("Cannot update.");
      }

      if (!this.companyRepository.existsById(companyId)) {
        throw new CompanyNotFoundException();
      }

      experience.setCompanyId(companyId);
      experience.setRoleTitle(roleTitle);
      experience.setRoleDescription(roleDescription);
      experience.setStartDate(startDate);
      experience.setEndDate(endDate);

      this.userExperienceRepository.save(experience);

    } catch (ExperienceNotOwnException
        | ExperienceNotFoundException
        | CompanyNotFoundException
        | UserServiceFailException e) {
      throw e;
    } catch (Exception e) {
      throw new UserServiceFailException(e.getMessage());
    }
  }
}
