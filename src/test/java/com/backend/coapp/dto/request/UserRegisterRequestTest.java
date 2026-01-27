package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

public class UserRegisterRequestTest {

  @Test
  public void getMethods_expectInitValues() {
    UserRegisterRequest request = new UserRegisterRequest("foo@mail.com", "123", "foo", "woof");

    assertEquals("foo@mail.com", request.getEmail());
    assertEquals("123", request.getPassword());
    assertEquals("foo", request.getFirstName());
    assertEquals("woof", request.getLastName());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    UserRegisterRequest request = new UserRegisterRequest("foo@mail.com", "123", "foo", "woof");
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidEmail_expectException() {
    UserRegisterRequest requestEmailNull = new UserRegisterRequest(null, "123", "foo", "woof");
    assertThrows(InvalidRequestException.class, requestEmailNull::validateRequest);

    UserRegisterRequest requestEmailBlank = new UserRegisterRequest("", "123", "foo", "woof");
    assertThrows(InvalidRequestException.class, requestEmailNull::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidPassword_expectException() {
    UserRegisterRequest requestPasswordNull =
        new UserRegisterRequest("foo@mail.com", null, "foo", "woof");
    assertThrows(InvalidRequestException.class, requestPasswordNull::validateRequest);

    UserRegisterRequest requestPasswordBlank =
        new UserRegisterRequest("foo@mail.com", "", "foo", "woof");
    assertThrows(InvalidRequestException.class, requestPasswordBlank::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidFirstname_expectException() {
    UserRegisterRequest requestFirstnameNull =
        new UserRegisterRequest("foo@mail.com", "123", null, "woof");
    assertThrows(InvalidRequestException.class, requestFirstnameNull::validateRequest);

    UserRegisterRequest requestFirstnameBlank =
        new UserRegisterRequest("foo@mail.com", "123", "", "woof");
    assertThrows(InvalidRequestException.class, requestFirstnameBlank::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidLastname_expectException() {
    UserRegisterRequest requestLastnameNull =
        new UserRegisterRequest("foo@mail.com", "123", "foo", null);
    assertThrows(InvalidRequestException.class, requestLastnameNull::validateRequest);

    UserRegisterRequest requestLastnameBlank =
        new UserRegisterRequest("foo@mail.com", "123", "foo", "");
    assertThrows(InvalidRequestException.class, requestLastnameBlank::validateRequest);
  }
}
