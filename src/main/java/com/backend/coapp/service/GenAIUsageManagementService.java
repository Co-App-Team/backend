package com.backend.coapp.service;

import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.exception.genai.ConcurrencyException;
import com.backend.coapp.exception.genai.GenAIQuotaExceededException;
import com.backend.coapp.exception.genai.GenAIUsageManagementServiceException;
import com.backend.coapp.model.document.UserGenAIUsageModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserGenAIUsageRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.util.GenAIUsageConstants;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

/** Business logic relate to manage users' GenAI usage */
@Service
@Getter
public class GenAIUsageManagementService {
  private final UserGenAIUsageRepository userGenAIUsageRepository;
  private final UserRepository userRepository;

  @Autowired
  public GenAIUsageManagementService(
      UserGenAIUsageRepository userGenAIUsageRepository, UserRepository userRepository) {
    this.userGenAIUsageRepository = userGenAIUsageRepository;
    this.userRepository = userRepository;
  }

  /**
   * Check if user exceed GenAI usage limit. If not, increase by one for the current request.
   *
   * @param userId ID of user request using GenAI
   * @throws GenAIUsageManagementServiceException when something goes wrong (Internal)
   * @throws UserNotFoundException when the ID of the user doesn't exist in the database
   * @throws GenAIQuotaExceededException when the user exceeds GenAI usage limit
   * @throws ConcurrencyException when the user request at the same time
   */
  public void checkAndIncrementUsage(String userId)
      throws GenAIUsageManagementServiceException,
          UserNotFoundException,
          GenAIQuotaExceededException,
          ConcurrencyException {
    try {
      UserGenAIUsageModel userUsageRecord =
          this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(userId);

      if (userUsageRecord == null) {
        UserModel user = this.userRepository.findUserModelById(userId);
        if (user == null) {
          throw new UserNotFoundException();
        }

        userUsageRecord =
            new UserGenAIUsageModel(
                userId, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, LocalDateTime.now());
      }

      LocalDateTime now = LocalDateTime.now();
      if (userUsageRecord.getLastReset().getMonth() != now.getMonth()
          || userUsageRecord.getLastReset().getYear() != now.getYear()) {
        userUsageRecord.setRequestCount(0);
        userUsageRecord.setLastReset(now);
      }

      if (userUsageRecord.getRequestCount() >= userUsageRecord.getMonthlyLimit()) {
        throw new GenAIQuotaExceededException();
      }

      userUsageRecord.setRequestCount(userUsageRecord.getRequestCount() + 1);
      userGenAIUsageRepository.save(userUsageRecord);
    } catch (UserNotFoundException | GenAIQuotaExceededException e) {
      throw e;
    } catch (OptimisticLockingFailureException e) {
      throw new ConcurrencyException("Another request is in progress, please try again.");
    } catch (Exception e) {
      throw new GenAIUsageManagementServiceException(e.getMessage());
    }
  }

  /**
   * Decrement GenAI usage of the existing record of a user
   *
   * @param userId ID of the user
   * @throws ConcurrencyException when the user request at the same time
   * @throws GenAIUsageManagementServiceException when something goes wrong (Internal)
   */
  public void decrementUsage(String userId)
      throws ConcurrencyException, GenAIUsageManagementServiceException {
    try {
      UserGenAIUsageModel userUsageRecord =
          this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(userId);
      if (userUsageRecord == null) {
        throw new GenAIUsageManagementServiceException(
            "GenAI record not found for user %s".formatted(userId));
      }

      if (userUsageRecord.getRequestCount() >= 1) {
        userUsageRecord.setRequestCount(userUsageRecord.getRequestCount() - 1);
      }
      userGenAIUsageRepository.save(userUsageRecord);

    } catch (OptimisticLockingFailureException e) {
      throw new ConcurrencyException("Another request is in progress, please try again.");
    } catch (Exception e) {
      throw new GenAIUsageManagementServiceException(e.getMessage());
    }
  }
}
