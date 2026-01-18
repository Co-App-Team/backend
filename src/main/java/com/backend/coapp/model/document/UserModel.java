package com.backend.coapp.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Class for users.
 * Incomplete, but here as proof of concept for now for database interaction.
 */
@Document(collection = "users")
public class UserModel {

  @Id
  public String id;

  public String firstName;
  public String lastName;

  /**
   * Constructor with basic information.
   *
   * @param firstName String, first name
   * @param lastName  String, last name
   */
  public UserModel(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
