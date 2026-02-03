package com.backend.coapp.model.document;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.enumeration.UserRolesEnum;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** This test is for UserModel without MongoDB. */
public class UserModelTest {
  private UserModel testFullUserModel;

  @BeforeEach
  public void setUp() {
    this.testFullUserModel =
        new UserModel( // This will refresh testUserModel for every test
            "1", "user@mail.com", "secret", "FirstName", "LastName", false, 123);
  }

  @Test
  public void getterMethods_expectInitValues() {
    assertEquals("1", this.testFullUserModel.getId());
    assertEquals("user@mail.com", this.testFullUserModel.getEmail());
    assertEquals("secret", this.testFullUserModel.getPassword());
    assertEquals("FirstName", this.testFullUserModel.getFirstName());
    assertEquals("LastName", this.testFullUserModel.getLastName());
    assertFalse(this.testFullUserModel.getVerified());
    assertEquals(123, this.testFullUserModel.getVerificationCode());
  }

  @Test
  public void setFirstName_expectOnlyFirstNameChange() {
    this.testFullUserModel.setFirstName("foo");
    assertEquals("1", this.testFullUserModel.getId());
    assertEquals("user@mail.com", this.testFullUserModel.getEmail());
    assertEquals("secret", this.testFullUserModel.getPassword());
    assertEquals("foo", this.testFullUserModel.getFirstName());
    assertEquals("LastName", this.testFullUserModel.getLastName());
    assertFalse(this.testFullUserModel.getVerified());
    assertEquals(123, this.testFullUserModel.getVerificationCode());
  }

  @Test
  public void setLastName_expectOnlyLastNameChange() {
    this.testFullUserModel.setLastName("foo");
    assertEquals("1", this.testFullUserModel.getId());
    assertEquals("user@mail.com", this.testFullUserModel.getEmail());
    assertEquals("secret", this.testFullUserModel.getPassword());
    assertEquals("FirstName", this.testFullUserModel.getFirstName());
    assertEquals("foo", this.testFullUserModel.getLastName());
    assertFalse(this.testFullUserModel.getVerified());
    assertEquals(123, this.testFullUserModel.getVerificationCode());
  }

  @Test
  public void setVerified_expectVerifiedChange() {
    this.testFullUserModel.setVerified(true);
    assertEquals("1", this.testFullUserModel.getId());
    assertEquals("user@mail.com", this.testFullUserModel.getEmail());
    assertEquals("secret", this.testFullUserModel.getPassword());
    assertEquals("FirstName", this.testFullUserModel.getFirstName());
    assertEquals("LastName", this.testFullUserModel.getLastName());
    assertTrue(this.testFullUserModel.getVerified());
    assertEquals(123, this.testFullUserModel.getVerificationCode());
  }

  @Test
  public void setVerificationCode_expectVerificationCodeChange() {
    this.testFullUserModel.setVerificationCode(999);
    assertEquals("1", this.testFullUserModel.getId());
    assertEquals("user@mail.com", this.testFullUserModel.getEmail());
    assertEquals("secret", this.testFullUserModel.getPassword());
    assertEquals("FirstName", this.testFullUserModel.getFirstName());
    assertEquals("LastName", this.testFullUserModel.getLastName());
    assertFalse(this.testFullUserModel.getVerified());
    assertEquals(999, this.testFullUserModel.getVerificationCode());
  }

  @Test
  public void setPassword_expectPasswordChange() {
    this.testFullUserModel.setPassword("NewPassword123");
    assertEquals("1", this.testFullUserModel.getId());
    assertEquals("user@mail.com", this.testFullUserModel.getEmail());
    assertEquals("NewPassword123", this.testFullUserModel.getPassword());
    assertEquals("FirstName", this.testFullUserModel.getFirstName());
    assertEquals("LastName", this.testFullUserModel.getLastName());
    assertFalse(this.testFullUserModel.getVerified());
    assertEquals(123, this.testFullUserModel.getVerificationCode());
  }

  @Test
  public void constructor_whenNoIDProvided_expectAutoGenerateID() {
    UserModel testLoginUserModel =
        new UserModel("user@mail.com", "secret", "FirstName", "LastName", 123);
    assert (testLoginUserModel.getId() == null);
    assert (testLoginUserModel.getEmail().equals("user@mail.com"));
    assert (testLoginUserModel.getPassword().equals("secret"));
    assert (testLoginUserModel.getFirstName().equals("FirstName"));
    assert (testLoginUserModel.getLastName().equals("LastName"));
    assert (testLoginUserModel.getVerificationCode() == 123);
    assert (!testLoginUserModel.getVerified());
  }

  @Test
  public void getAuthorities_expectUserRoleOnly() {
    Collection<? extends GrantedAuthority> roles = this.testFullUserModel.getAuthorities();
    assertNotNull(roles);
    assertEquals(1, roles.size());

    GrantedAuthority authority = roles.iterator().next();
    assertTrue(authority instanceof SimpleGrantedAuthority);
    assertEquals(UserRolesEnum.USER_ROLE.name(), authority.getAuthority());
  }

  @Test
  public void getUsername_expectReturnEmail() {
    assertEquals("user@mail.com", this.testFullUserModel.getUsername());
  }

  @Test
  public void accountStatus_expectAllTrue() {
    assertTrue(this.testFullUserModel.isAccountNonExpired());
    assertTrue(this.testFullUserModel.isAccountNonLocked());
    assertTrue(this.testFullUserModel.isCredentialsNonExpired());
    assertTrue(this.testFullUserModel.isEnabled());
  }
}
