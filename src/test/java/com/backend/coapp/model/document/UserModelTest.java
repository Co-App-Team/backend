package com.backend.coapp.model.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** This test is for UserModel without MongoDB. */
public class UserModelTest {
  private UserModel testFullUserModel;

  @BeforeEach
  public void setUp() {
    this.testFullUserModel =

        new UserModel( // This will refresh testUserModel for every test
            "1", "user@mail.com", "secret", "FirstName", "LastName",false,123);
  }

  @Test
  public void testGetter() {
    assert (this.testFullUserModel.getId().equals("1"));
    assert (this.testFullUserModel.getEmail().equals("user@mail.com"));
    assert (this.testFullUserModel.getPassword().equals("secret"));
    assert (this.testFullUserModel.getFirstName().equals("FirstName"));
    assert (this.testFullUserModel.getLastName().equals("LastName"));
    assert (!this.testFullUserModel.getVerified());
    assert (this.testFullUserModel.getVerificationCode() == 123);
  }

  @Test
  public void testSetFirstName() {
    this.testFullUserModel.setFirstName("foo");
    assert (this.testFullUserModel.getFirstName().equals("foo"));
    assert (this.testFullUserModel.getLastName().equals("LastName"));

    this.testFullUserModel.setLastName("woof");
    assert (this.testFullUserModel.getLastName().equals(("woof")));
    assert (this.testFullUserModel.getFirstName().equals("foo"));
  }


  @Test
  public void testExceptIDInit() {
    UserModel testLoginUserModel =
        new UserModel("user@mail.com", "secret", "FirstName", "LastName",123);
    assert (testLoginUserModel.getId() == null);
    assert (testLoginUserModel.getEmail().equals("user@mail.com"));
    assert (testLoginUserModel.getPassword().equals("secret"));
    assert (testLoginUserModel.getFirstName().equals("FirstName"));
    assert (testLoginUserModel.getLastName().equals("LastName"));
    assert (testLoginUserModel.getVerificationCode() ==  123);
    assert (!testLoginUserModel.getVerified());
  }
}
