package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import com.backend.coapp.dto.UserDTO;
import com.backend.coapp.service.UserService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserControllerTest {

  private UserService userService;
  private UserController userController;

  @BeforeEach
  public void setUp() {
    this.userService = Mockito.mock(UserService.class);
    this.userController = new UserController(this.userService);
  }

  @Test
  public void testSameInitInstance() {
    assertSame(this.userController.getUserService(), this.userService);
  }

  @Test
  public void testgetDummyUser() {
    UserDTO dummyUser = new UserDTO("Dummy Firstname", "Dummy Lastname", "foo@mail.com");
    when(this.userService.getDummyUser()).thenReturn(dummyUser);

    ResponseEntity<Map<String, Object>> response = this.userController.getDummyUser();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("foo@mail.com", response.getBody().get("email"));
    assertEquals("Dummy Firstname", response.getBody().get("firstName"));
    assertEquals("Dummy Lastname", response.getBody().get("lastName"));
  }
}
