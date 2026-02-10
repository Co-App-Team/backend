package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class UserServiceTest {
  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private UserRepository userRepository;
  private UserRepository mockUserRepository;
  private UserService userService;
  private UserModel fooUserNotActivated;
  private UserModel fooUserActivated;

  @BeforeEach
  public void setUp() {
    this.userRepository.deleteAll();
    this.fooUserNotActivated = new UserModel("foo@mail.com", "password123", "foo", "woof", 123);
    this.userRepository.save(fooUserNotActivated);
    this.fooUserActivated =
        new UserModel(
            "fooActivated@mail.com",
            "password123",
            "fooActivated",
            "woof",
            UserModel.DEFAULT_VERIFICATION_CODE);
    this.fooUserActivated.setVerified(true);
    this.userRepository.save(this.fooUserActivated);
    this.userService = new UserService(this.userRepository);
    this.mockUserRepository = Mockito.mock(UserRepository.class);
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertSame(this.userRepository, this.userService.getUserRepository());
  }

  @Test
  public void getDummyUser_expectInitValues() {
    UserResponse user = this.userService.getDummyUser();

    assertNotNull(user);
    assertEquals("Dummy Firstname", user.getFirstName());
    assertEquals("Dummy Lastname", user.getLastName());
    assertEquals("foo@mail.com", user.getEmail());
  }

  @Test
  public void udpateUserPassword_whenOldPasswordMatch_expectNoException() {
    assertDoesNotThrow(
        () ->
            this.userService.udpateUserPassword(
                this.fooUserActivated.getEmail(),
                this.fooUserActivated.getPassword(),
                "newPassword"));
    UserModel user = this.userRepository.findUserModelByEmail(this.fooUserActivated.getEmail());
    assertNotNull(user);
    assertEquals("newPassword", user.getPassword());
  }

  @Test
  public void updatePassword_whenNoAccountFound_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.userService.udpateUserPassword("notExistUser@mail.com", "1", "2"));
  }

  @Test
  public void updatePassword_whenNoAccountNotYetActivated_expectException() {
    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () ->
            this.userService.udpateUserPassword(
                this.fooUserNotActivated.getEmail(),
                this.fooUserNotActivated.getPassword(),
                "newPassword"));
  }

  @Test
  public void updatePassword_whenOldPasswordNotMatch_expectException() {
    assertThrows(
        AuthBadCredentialException.class,
        () ->
            this.userService.udpateUserPassword(
                this.fooUserActivated.getEmail(), "fooEmail", "newPassword"));
  }

  @Test
  public void udpatePassword_whenDatabaseSaveOperationFail_expectException() {
    this.userService = new UserService(this.mockUserRepository);

    when(this.mockUserRepository.save(any(UserModel.class))).thenThrow(new RuntimeException());
    when(this.mockUserRepository.findUserModelByEmail(anyString()))
        .thenReturn(this.fooUserActivated);

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.udpateUserPassword(
                this.fooUserActivated.getEmail(),
                this.fooUserActivated.getPassword(),
                "newPassword"));

    verify(this.mockUserRepository, times(1)).save(any(UserModel.class));
  }

  @Test
  public void udpatePassword_whenDatabaseFindOperationFail_expectException() {
    this.userService = new UserService(this.mockUserRepository);

    when(this.mockUserRepository.findUserModelByEmail(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.udpateUserPassword(
                this.fooUserActivated.getEmail(),
                this.fooUserActivated.getPassword(),
                "newPassword"));

    verify(this.mockUserRepository, never()).save(any(UserModel.class));
  }
}
