package com.backend.coapp.model.document;

import com.backend.coapp.model.enumeration.UserRoles;
import jakarta.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** User model */
@Getter
@SuppressWarnings("LombokSetterMayBeUsed")
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class UserModel implements UserDetails {

  /** Constants * */
  public static final Integer DEFAULT_VERIFICATION_CODE = -1;

  /** Attributes * */
  @Id private String id;

  @Indexed(unique = true)
  private String email;

  @NotBlank(message = "Password cannot be empty")
  private String password;

  @NotBlank(message = "First name cannot be empty")
  private String firstName;

  @NotBlank(message = "Last name cannot be empty")
  private String lastName;

  private Boolean verified;
  private Integer verificationCode = DEFAULT_VERIFICATION_CODE; // -1 by default

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

  public void setPassword(String newPassword) {
    this.password = newPassword;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(UserRoles.USER_ROLE.name()));
  }

  @Override
  public String getUsername() {
    return this.id;
  }

  @Override
  public boolean isEnabled() {
    return this.verified;
  }

  /**
   * The following methods are currently not used but required to be overridden These following
   * methods are required for Spring Security setup
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }
}
