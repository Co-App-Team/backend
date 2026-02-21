package com.backend.coapp.repository;

import com.backend.coapp.model.document.UserGenAIUsageModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserGenAIUsageRepository extends MongoRepository<UserGenAIUsageModel, String> {

  /**
   * Find user's GenAI usage record by userId
   *
   * @param userId the ID of the user
   * @return UserGenAIUsageModel user's GenAI usage record
   */
  UserGenAIUsageModel findUserGenAIUsageModelByUserId(String userId);
}
