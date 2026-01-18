package com.backend.coapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.coapp.models.document.UserModel;
import com.backend.coapp.repositories.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Unit tests for UserModel and its repository.
 * Proof of concept, for now.
 */
@SpringBootTest
@Testcontainers
public class UserRepositoryTests {
  @Container
  @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired
  UserRepository repository;

  UserModel john;
  UserModel jane;
  UserModel bob;

  /**
   * Runs before each test to reset the data.
   */
  @BeforeEach
  public void setUp() {
    repository.deleteAll();

    john = repository.save(new UserModel("John", "Doe"));
    jane = repository.save(new UserModel("Jane", "Doe"));
    bob = repository.save(new UserModel("Bob", "Smith"));
  }

  @Test
  public void setsIdOnSave() {
    UserModel user = repository.save(new UserModel("Test", "User"));

    assertThat(user.id).isNotNull();
  }

  @Test
  public void findsUserById() {
    UserModel found = repository.findUserModelById(john.id);

    assertThat(found).isNotNull();
    assertThat(found.firstName).isEqualTo("John");
    assertThat(found.lastName).isEqualTo("Doe");
  }

  @Test
  public void findsAllUsers() {
    List<UserModel> users = repository.findAll();

    assertThat(users).hasSize(3)
      .extracting("firstName")
      .contains("John", "Jane", "Bob");
  }

  @Test
  public void deletesUser() {
    repository.deleteById(bob.id);

    List<UserModel> users = repository.findAll();
    assertThat(users).hasSize(2);

    UserModel deleted = repository.findUserModelById(bob.id);
    assertThat(deleted).isNull();
  }

  @Test
  public void updatesUser() {
    john.firstName = "Jonathan";
    john.lastName = "Dorothy";
    repository.save(john);

    UserModel updated = repository.findUserModelById(john.id);
    assertThat(updated.firstName).isEqualTo("Jonathan");
    assertThat(updated.lastName).isEqualTo("Dorothy");
  }

  @Test
  public void returnsNullForNonExistentId() {
    UserModel notFound = repository.findUserModelById("fake-id");

    assertThat(notFound).isNull();
  }
}