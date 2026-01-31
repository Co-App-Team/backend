package com.backend.coapp.service;

import com.backend.coapp.dto.response.UserResponse;
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
}
