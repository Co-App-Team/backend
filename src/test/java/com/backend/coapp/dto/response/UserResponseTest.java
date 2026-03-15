package com.backend.coapp.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class UserResponseTest {
  @Test
  void getterMethod_expectInitValues() {
    UserResponse response = new UserResponse("foo", "woof", "foo@mail.com");

    assertEquals("foo", response.getFirstName());
    assertEquals("woof", response.getLastName());
    assertEquals("foo@mail.com", response.getEmail());
  }

  @Test
  void toMap_expectMapWithInitValues() {
    UserResponse response = new UserResponse("foo", "woof", "foo@mail.com");
    Map<String, Object> expectMap =
        Map.of("firstName", "foo", "lastName", "woof", "email", "foo@mail.com");

    assertEquals(expectMap, response.toMap());
  }
}
