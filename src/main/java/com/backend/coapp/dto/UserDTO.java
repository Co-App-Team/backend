package com.backend.coapp.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDTO {
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
