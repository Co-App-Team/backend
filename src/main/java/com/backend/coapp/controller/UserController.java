package com.backend.coapp.controller;

import com.backend.coapp.dto.request.UpdatePasswordWithOldPasswordRequest;
import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.service.UserService;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @PatchMapping("/update-password")
  public ResponseEntity<Map<String, Object>> updatePassword(
      @RequestBody UpdatePasswordWithOldPasswordRequest request) {
    request.validateRequest();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userID = auth.getName();
    this.userService.updateUserPassword(userID, request.getOldPassword(), request.getNewPassword());

    return ResponseEntity.ok().body(Map.of("message", "Updated password successfully."));
  }

  @GetMapping("/about-me")
  public ResponseEntity<Map<String, Object>> aboutMe() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userID = auth.getName();

    UserModel userModel = this.userService.getUserInformationFromUserID(userID);
    UserResponse userResponse = UserResponse.fromModel(userModel);

    return ResponseEntity.ok().body(userResponse.toMap());
  }
}
