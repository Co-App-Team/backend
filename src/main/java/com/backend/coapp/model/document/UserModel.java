package com.backend.coapp.model.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User model
 */
@Getter
@SuppressWarnings("LombokSetterMayBeUsed")
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class UserModel {

  @Id private String id;

  @Indexed(unique = true)
  private String email;
  private String password;

  private String firstName;
  private String lastName;

  /**
   * Constructor with email and password for login
   * @param email String user email
   * @param password String user password
   */
  public UserModel(String email, String password){
    this.email = email;
    this.password = password; //@TODO: We should hash for better security
  }

  /**
   * Constructor with all information except for id. This will be helpful for creating account
   * @param email String user email
   * @param password String password
   * @param firstName String first name
   * @param lastName String last name
   */
  public UserModel(String email, String password, String firstName, String lastName){
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  /**
   * Setter methods
   */
  public void setFirstName(String newFirstName) {
    this.firstName = newFirstName;
  }

  public void setLastName(String newLastName){
    this.lastName = newLastName;
  }

}
