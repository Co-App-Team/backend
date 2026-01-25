package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;

  @BeforeEach
  public void setUp() {
    this.userRepository = Mockito.mock(UserRepository.class);
    this.userService = new UserService(this.userRepository);
  }

  @Test
  public void testSameInitInstance() {
    assertSame(this.userRepository, this.userService.getUserRepository());
  }

  @Test
  public void testGetDummyUser() {
    UserResponse user = this.userService.getDummyUser();

    assertNotNull(user);
    assert (user.getFirstName().equals("Dummy Firstname"));
    assert (user.getLastName().equals("Dummy Lastname"));
    assert (user.getEmail().equals("foo@mail.com"));
  }
}
