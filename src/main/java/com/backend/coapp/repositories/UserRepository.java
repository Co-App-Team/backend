package com.backend.coapp.repositories;

import com.backend.coapp.models.document.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to interact with users in the database.
 */
@Repository
public interface UserRepository extends MongoRepository<UserModel, String> {
  /**
   * Finds the user given an id.
   *
   * @param id string Users id
   * @return a user
   */
  UserModel findUserModelById(String id);
}