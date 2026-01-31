package com.backend.coapp.model.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** User model */
@Getter
@SuppressWarnings("LombokSetterMayBeUsed")
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class UserModel {

  /** Constants * */
  public static final Integer DEFAULT_VERIFICATION_CODE = -1;

  /** Attributes * */
  @Id private String id;

  @Indexed(unique = true)
  private String email;

  private String password; // @TODO: we need to hash this to enhance security.

  private String firstName;
  private String lastName;

  private Boolean verified;
  private Integer verificationCode = DEFAULT_VERIFICATION_CODE; // -1 by default

  private Integer forgotPasswordCode = DEFAULT_VERIFICATION_CODE;

  /**
   * Constructor with all information except for id. This will be helpful for creating account
   *
   * @param email String user email
   * @param password String password
   * @param firstName String first name
   * @param lastName String last name
   * @param verificationCode int confirmation code
   */
  public UserModel(
      String email, String password, String firstName, String lastName, int verificationCode) {
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.verificationCode = verificationCode;
    this.verified = false;
  }

  /** Setter methods */
  public void setFirstName(String newFirstName) {
    this.firstName = newFirstName;
  }

  public void setLastName(String newLastName) {
    this.lastName = newLastName;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public void setVerificationCode(int newVerificationCode) {
    this.verificationCode = newVerificationCode;
  }

  public void setForgotPasswordCode(int forgotPasswordCode){
    this.forgotPasswordCode = forgotPasswordCode;
  }
}
