package com.backend.coapp.dto.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** DTO response for returning user information. */
@Getter
@AllArgsConstructor
public class UserResponse {
  // JSON keys
  private String firstName;
  private String lastName;
  private String email;

  /**
   * Create a map, where keys are object attributes name and values are attributes' values
   *
   * @return Map
   */
  public Map<String, Object> toMap() {
    return Map.of(
        "firstName", this.firstName,
        "lastName", this.lastName,
        "email", this.email);
  }
}
