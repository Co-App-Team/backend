package com.backend.coapp.service;

import com.backend.coapp.dto.UserDTO;
import com.backend.coapp.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * User Service
 *
 * <p>This handles all business logic related to User.
 */
@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * This is function is a proof of concept.
   *
   * @return String
   */
  public UserDTO getDummyUser() {
    return new UserDTO("Dummy Firstname", "Dummy Lastname", "foo@mail.com");
  }

  /**
   * Get userRepository instance.
   *
   * <p>For testing only
   */
  public UserRepository getUserRepository() {
    return this.userRepository;
  }
}
