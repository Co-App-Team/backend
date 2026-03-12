package com.backend.coapp.controller;

import com.backend.coapp.dto.request.UpdatePasswordWithOldPasswordRequest;
import com.backend.coapp.dto.request.UserExperienceRequest;
import com.backend.coapp.dto.response.UserExperienceResponse;
import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.model.document.UserExperienceModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * User controllers
 *
 * <p>User related REST APIs
 */
@RestController
@Slf4j
@Getter // For testing only
@RequestMapping("/api/user")
public class UserController {

  /** Singleton service and repository * */
  private final UserService userService;

  /** Constructor */
  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * This API endpoint is a proof of concept. This just return a dummy user that doesn't exist in
   * the database.
   *
   * @return ResponseEntity
   */
  @GetMapping("/dummyUser")
  public ResponseEntity<Map<String, Object>> getDummyUser() {
    UserResponse dummyUser = this.userService.getDummyUser();
    log.info("INFO: GET dummyUser API is called.");
    return ResponseEntity.ok().body(dummyUser.toMap());
  }

  /**
   * Update user's password if user provides the old password right
   *
   * @param request UpdatePasswordWithOldPasswordRequest
   * @param authentication authentication object
   * @return ResponseEntity
   */
  @PatchMapping("/update-password")
  public ResponseEntity<Map<String, Object>> updatePassword(
      @RequestBody UpdatePasswordWithOldPasswordRequest request, Authentication authentication) {
    request.validateRequest();
    UserModel user = (UserModel) authentication.getPrincipal();
    String userID = user.getId();
    this.userService.updateUserPassword(userID, request.getOldPassword(), request.getNewPassword());

    return ResponseEntity.ok().body(Map.of("message", "Updated password successfully."));
  }

  /**
   * Get basic information about user
   *
   * @param authentication authentication object
   * @return ResponseEntity user information
   */
  @GetMapping("/about-me")
  public ResponseEntity<Map<String, Object>> aboutMe(Authentication authentication) {
    UserModel user = (UserModel) authentication.getPrincipal();
    String userID = user.getId();

    UserModel userModel = this.userService.getUserInformationFromUserID(userID);
    UserResponse userResponse = UserResponse.fromModel(userModel);

    return ResponseEntity.ok().body(userResponse.toMap());
  }

  /**
   * Get all experience of the user
   *
   * @param authentication authentication object
   * @return ResponseEntity - list of experiences of the user.
   */
  @GetMapping("/experience")
  public ResponseEntity<Map<String, Object>> getAllUserExperience(Authentication authentication) {
    UserModel user = (UserModel) authentication.getPrincipal();
    String userID = user.getId();

    List<UserExperienceModel> userExperienceModelList =
        this.userService.getAllUserExperience(userID);

    List<UserExperienceResponse> userExperienceResponseList = new ArrayList<>();
    for (UserExperienceModel experience : userExperienceModelList) {
      userExperienceResponseList.add(UserExperienceResponse.fromModel(experience));
    }
    return ResponseEntity.ok().body(Map.of("experience", userExperienceResponseList));
  }

  /**
   * Create new experience
   *
   * @param request UserExperienceRequest
   * @param authentication authentication object
   * @return ID of the new experience
   */
  @PostMapping("/experience")
  public ResponseEntity<Map<String, Object>> createNewUserExperience(
      @RequestBody UserExperienceRequest request, Authentication authentication) {
    UserModel user = (UserModel) authentication.getPrincipal();
    String userID = user.getId();
    request.validateRequest();

    UserExperienceModel experience =
        userService.createNewUserExperience(
            userID,
            request.getCompanyId(),
            request.getRoleTitle(),
            request.getRoleDescription(),
            request.getStartDate(),
            request.getEndDate());

    return ResponseEntity.ok().body(Map.of("experienceId", experience.getId()));
  }

  /**
   * Delete a user experience
   *
   * @param experienceId Path variable
   * @param authentication authentication object
   * @return ResponseEntity: Deleted successfully when successfully deletes
   */
  @DeleteMapping("/experience/{experienceId}")
  public ResponseEntity<Map<String, Object>> deleteUserExperience(
      @PathVariable String experienceId, Authentication authentication) {
    UserModel user = (UserModel) authentication.getPrincipal();
    String userID = user.getId();
    if (experienceId.isBlank()) {
      throw new InvalidRequestException("Experience ID can NOT be null or empty");
    }

    userService.deleteUserExperience(experienceId, userID);

    return ResponseEntity.ok().body(Map.of("message", "Deleted successfully."));
  }

  /**
   * Update existing experience
   *
   * @param experienceId path id of the experience
   * @param request UserExperienceRequest
   * @param authentication authentication object
   * @return Update successfully message
   */
  @PatchMapping("/experience/{experienceId}")
  public ResponseEntity<Map<String, Object>> updateUserExperience(
      @PathVariable String experienceId,
      @RequestBody UserExperienceRequest request,
      Authentication authentication) {
    UserModel user = (UserModel) authentication.getPrincipal();
    String userID = user.getId();
    request.validateRequest();
    if (experienceId.isBlank()) {
      throw new InvalidRequestException("Experience ID can NOT be null or empty");
    }

    userService.updateUserExperience(
        experienceId,
        userID,
        request.getCompanyId(),
        request.getRoleTitle(),
        request.getRoleDescription(),
        request.getStartDate(),
        request.getEndDate());

    return ResponseEntity.ok().body(Map.of("message", "Update successfully."));
  }
}
