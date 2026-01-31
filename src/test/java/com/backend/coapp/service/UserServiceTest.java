package com.backend.coapp.service;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;

  @BeforeEach
  public void setUp() {
    this.userRepository = Mockito.mock(UserRepository.class);
    this.userService = new UserService(this.userRepository);
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertSame(this.userRepository, this.userService.getUserRepository());
  }

  @Test
  public void getDummyUser_expectInitValues() {
    UserResponse user = this.userService.getDummyUser();

    assertNotNull(user);
    assertEquals("Dummy Firstname",user.getFirstName());
    assertEquals ("Dummy Lastname",user.getLastName());
    assertEquals ("foo@mail.com",user.getEmail());
  }
}
