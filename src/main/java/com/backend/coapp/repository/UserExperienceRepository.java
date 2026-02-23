package com.backend.coapp.repository;

import com.backend.coapp.model.document.UserExperienceModel;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Repository to keep track of experience of each user */
@Repository
public interface UserExperienceRepository extends MongoRepository<UserExperienceModel, String> {
  /**
   * Find experience of a user given experience ID
   *
   * @param id experience id
   * @return experience record
   */
  UserExperienceModel findUserExperienceModelById(String id);

  /**
   * Find all experience of a user
   *
   * @param userId id of the user
   * @return List of all users' experience
   */
  List<UserExperienceModel> findAllByUserId(String userId);

  /**
   * Delete the exist experience
   *
   * @param id id of the experience record
   */
  void deleteUserExperienceModelById(String id);
}
