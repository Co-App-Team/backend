package com.backend.coapp.controller;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.service.UserService;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
}
