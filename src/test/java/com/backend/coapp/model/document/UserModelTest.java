package com.backend.coapp.model.document;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.enumeration.UserRoles;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** This test is for UserModel without MongoDB. */
class UserModelTest {
  private UserModel testFullUserModel;

  @BeforeEach
  void setUp() {
    this.testFullUserModel =
        new UserModel( // This will refresh testUserModel for every test
            "1", "user@mail.com", "secret", "FirstName", "LastName", false, 123);
  }

  @Test
  void getterMethods_expectInitValues() {
    assertEquals("1", this.testFullUserModel.getId());
    assertEquals("user@mail.com", this.testFullUserModel.getEmail());
    assertEquals("secret", this.testFullUserModel.getPassword());
    assertEquals("FirstName", this.testFullUserModel.getFirstName());
    assertEquals("LastName", this.testFullUserModel.getLastName());
    assertFalse(this.testFullUserModel.getVerified());
    assertEquals(123, this.testFullUserModel.getVerificationCode());
  }

  @Test
  void setFirstName_expectOnlyFirstNameChange() {
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
  void setLastName_expectOnlyLastNameChange() {
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
  void setVerified_expectVerifiedChange() {
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
  void setVerificationCode_expectVerificationCodeChange() {
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
  void setPassword_expectPasswordChange() {
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
  void constructor_whenNoIDProvided_expectAutoGenerateID() {
    UserModel testLoginUserModel =
        new UserModel("user@mail.com", "secret", "FirstName", "LastName", 123);
    assertNull(testLoginUserModel.getId());
    assertThat(testLoginUserModel.getEmail()).isEqualTo("user@mail.com");
    assertThat(testLoginUserModel.getPassword()).isEqualTo("secret");
    assertThat(testLoginUserModel.getFirstName()).isEqualTo("FirstName");
    assertThat(testLoginUserModel.getLastName()).isEqualTo("LastName");
    assertEquals(123, testLoginUserModel.getVerificationCode());
    assertTrue(!testLoginUserModel.getVerified());
  }

  @Test
  void getAuthorities_expectUserRoleOnly() {
    Collection<? extends GrantedAuthority> roles = this.testFullUserModel.getAuthorities();
    assertNotNull(roles);
    assertEquals(1, roles.size());

    GrantedAuthority authority = roles.iterator().next();
    assertInstanceOf(SimpleGrantedAuthority.class, authority);
    assertEquals(UserRoles.USER_ROLE.name(), authority.getAuthority());
  }

  @Test
  void getUsername_expectReturnEmail() {
    assertEquals("1", this.testFullUserModel.getUsername());
  }

  @Test
  void accountStatus_expectAllTrue() {
    assertTrue(this.testFullUserModel.isAccountNonExpired());
    assertTrue(this.testFullUserModel.isAccountNonLocked());
    assertTrue(this.testFullUserModel.isCredentialsNonExpired());
  }

  @Test
  void isEnabled_expectMatchVerified() {
    assertEquals(this.testFullUserModel.isEnabled(), this.testFullUserModel.getVerified());
  }
}
