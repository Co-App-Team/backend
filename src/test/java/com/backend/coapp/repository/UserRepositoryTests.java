package com.backend.coapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.backend.coapp.model.document.UserModel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Unit tests for UserModel and its repository. Proof of concept, for now. */
@SpringBootTest
@Testcontainers
class UserRepositoryTests {
  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired UserRepository repository;

  UserModel john;
  UserModel jane;
  UserModel bob;

  /** Runs before each test to reset the data. */
  @BeforeEach
  void setUp() {
    repository.deleteAll();

    john = repository.save(new UserModel("john@mail.com", "password123", "John", "Johnson", 123));
    jane = repository.save(new UserModel("jane@mail.com", "secure456", "Jane", "Smith", 345));
    bob = repository.save(new UserModel("bob@mail.com", "mypassword789", "Bob", "Williams", 567));
  }

  @Test
  void saveNew_whenNoIdDefined_expectSetsIdOnSave() {
    UserModel user = repository.save(new UserModel("user@mail.com", "secret", "foo", "woof", 123));

    assertThat(user.getId()).isNotNull();
  }

  @Test
  void findsUserById_expectReturnJohn() {
    UserModel found = repository.findUserModelById(john.getId());

    assertThat(found).isNotNull();
    assertThat(found.getFirstName()).isEqualTo("John");
    assertThat(found.getLastName()).isEqualTo("Johnson");
    assertThat(found.getId() != null);
    assertThat(found.getEmail()).isEqualTo(john.getEmail());
    assertThat(found.getPassword()).isEqualTo(john.getPassword());
    assertThat(found.getVerificationCode()).isEqualTo(john.getVerificationCode());
    assertThat(found.getVerified()).isEqualTo(john.getVerified());
  }

  @Test
  void findUserModelByEmail_whenFindByJohnEmail_expectJohnUserModel() {
    UserModel found = repository.findUserModelByEmail(john.getEmail());

    assertThat(found).isNotNull();
    assertThat(found.getFirstName()).isEqualTo("John");
    assertThat(found.getLastName()).isEqualTo("Johnson");
    assertNotNull(found.getId());
    assertThat(found.getEmail()).isEqualTo(john.getEmail());
    assertThat(found.getPassword()).isEqualTo(john.getPassword());
    assertThat(found.getVerificationCode()).isEqualTo(john.getVerificationCode());
    assertThat(found.getVerified()).isEqualTo(john.getVerified());
  }

  @Test
  void findUserModelByEmail_whenFindNonExistEmail_expectNull() {
    UserModel found = repository.findUserModelByEmail("foo@mail.com");
    assertNull(found);
  }

  @Test
  void findsAllUsers_expectReturn3Users() {
    List<UserModel> users = repository.findAll();

    assertThat(users).hasSize(3).extracting("firstName").contains("John", "Jane", "Bob");
    assertThat(users).hasSize(3).extracting("lastName").contains("Johnson", "Smith", "Williams");
    assertThat(users)
        .hasSize(3)
        .extracting("password")
        .contains("password123", "secure456", "mypassword789");
    assertThat(users)
        .hasSize(3)
        .extracting("email")
        .contains("john@mail.com", "jane@mail.com", "bob@mail.com");
  }

  @Test
  void deleteById_whenDeleteBob_expectDeleteBob() {
    repository.deleteById(bob.getId());

    List<UserModel> users = repository.findAll();
    assertThat(users).hasSize(2);

    UserModel deleted = repository.findUserModelById(bob.getId());
    assertThat(deleted).isNull();
  }

  @Test
  void updatesUser_whenUpdateJohnFirstAndLastName_expectJohnNameUpdate() {
    john.setFirstName("Jonathan");
    john.setLastName("Dorothy");
    repository.save(john);

    UserModel updated = repository.findUserModelById(john.getId());
    assertThat(updated.getFirstName()).isEqualTo("Jonathan");
    assertThat(updated.getLastName()).isEqualTo("Dorothy");
  }

  @Test
  void findUserModelById_whenUserNotExist_expectReturnNull() {
    UserModel notFound = repository.findUserModelById("fake-id");

    assertThat(notFound).isNull();
  }
}
