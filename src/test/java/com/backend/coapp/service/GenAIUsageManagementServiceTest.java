package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.genai.ConcurrencyException;
import com.backend.coapp.exception.genai.GenAIQuotaExceededException;
import com.backend.coapp.exception.genai.GenAIUsageManagementServiceException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.UserGenAIUsageModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserGenAIUsageRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.util.GenAIUsageConstants;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.OptimisticLockingFailureException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Parts of the unit test are written with help of Claude (Sonnet 4.6) */
@SpringBootTest
@Testcontainers
class GenAIUsageManagementServiceTest {
  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private UserRepository userRepository;
  @Autowired private UserGenAIUsageRepository userGenAIUsageRepository;

  private GenAIUsageManagementService genAIUsageManagementService;

  private final UserModel fooUser =
      new UserModel(
          "123",
          "foo@mail.com",
          "password",
          "foo",
          "woof",
          true,
          UserModel.DEFAULT_VERIFICATION_CODE);

  private final UserGenAIUsageModel userGenAIUsageModel =
      new UserGenAIUsageModel(
          this.fooUser.getId(),
          GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT,
          LocalDateTime.now());

  @BeforeEach
  void setUp() {
    this.userRepository.deleteAll();
    this.userGenAIUsageRepository.deleteAll();
    this.userRepository.save(this.fooUser);
    this.userGenAIUsageRepository.save(this.userGenAIUsageModel);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepository, userRepository);
  }

  @Test
  void checkAndIncrementUsage_whenUserExistInUsageRepoAlready_expectIncreaseByOne() {
    this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId());

    UserGenAIUsageModel userUsageRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    assertNotNull(userUsageRecord);
    assertEquals(this.userGenAIUsageModel.getRequestCount() + 1, userUsageRecord.getRequestCount());
  }

  @Test
  void checkAndIncrementUsage_whenUserNotExistInUsageRepoYet_expectIncreaseByOne() {
    this.userGenAIUsageRepository.deleteAll();
    this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId());

    UserGenAIUsageModel userUsageRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    assertNotNull(userUsageRecord);
    assertEquals(1, userUsageRecord.getRequestCount());
  }

  @Test
  void checkAndIncrementUsage_whenUserNotExistInUserRepoYet_expectException() {
    this.userGenAIUsageRepository.deleteAll();
    this.userRepository.deleteAll();
    String fooUserId = this.fooUser.getId();
    assertThrows(
        UserNotFoundException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(fooUserId));
  }

  @Test
  void checkAndIncrementUsage_whenPastMonth_expectResetLimit() {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10);
    userRecord.setLastReset(this.userGenAIUsageModel.getLastReset().minusMonths(1));
    this.userGenAIUsageRepository.save(userRecord);

    assertDoesNotThrow(
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId()));

    UserGenAIUsageModel userUsageRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    assertNotNull(userUsageRecord);
    assertEquals(1, userUsageRecord.getRequestCount());
    assertEquals(LocalDateTime.now().getMonth(), userUsageRecord.getLastReset().getMonth());
    assertEquals(
        LocalDateTime.now().getDayOfMonth(), userUsageRecord.getLastReset().getDayOfMonth());
    assertEquals(LocalDateTime.now().getYear(), userUsageRecord.getLastReset().getYear());
  }

  @Test
  void checkAndIncrementUsage_whenPastYear_expectResetLimit() {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10);
    userRecord.setLastReset(this.userGenAIUsageModel.getLastReset().minusYears(1));
    this.userGenAIUsageRepository.save(userRecord);

    assertDoesNotThrow(
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId()));

    UserGenAIUsageModel userUsageRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    assertNotNull(userUsageRecord);
    assertEquals(1, userUsageRecord.getRequestCount());
    assertEquals(LocalDateTime.now().getMonth(), userUsageRecord.getLastReset().getMonth());
    assertEquals(
        LocalDateTime.now().getDayOfMonth(), userUsageRecord.getLastReset().getDayOfMonth());
    assertEquals(LocalDateTime.now().getYear(), userUsageRecord.getLastReset().getYear());
  }

  @Test
  void checkAndIncrementUsage_whenOverLimit_expectException() {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10);
    this.userGenAIUsageRepository.save(userRecord);
    String fooUserId = this.fooUser.getId();
    assertThrows(
        GenAIQuotaExceededException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(fooUserId));

    UserGenAIUsageModel userUsageRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(fooUserId);
    assertNotNull(userUsageRecord);
    assertEquals(
        GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10, userUsageRecord.getRequestCount());
  }

  @Test
  void checkAndIncrementUsage_whenUserUsageRepoOperationFail_expectException() {
    UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    UserGenAIUsageRepository userGenAIUsageRepositoryMock =
        Mockito.mock(UserGenAIUsageRepository.class);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepositoryMock, userRepositoryMock);

    when(userGenAIUsageRepositoryMock.findUserGenAIUsageModelByUserId(anyString()))
        .thenThrow(new RuntimeException());
    String fooUserId = this.fooUser.getId();
    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(fooUserId));

    verifyNoInteractions(userRepositoryMock);
    verify(userGenAIUsageRepositoryMock, times(1))
        .findUserGenAIUsageModelByUserId(this.fooUser.getId());
  }

  @Test
  void checkAndIncrementUsage_whenUserRepoOperationFail_expectException() {
    UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    UserGenAIUsageRepository userGenAIUsageRepositoryMock =
        Mockito.mock(UserGenAIUsageRepository.class);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepositoryMock, userRepositoryMock);

    when(userGenAIUsageRepositoryMock.findUserGenAIUsageModelByUserId(anyString()))
        .thenReturn(null);
    when(userRepositoryMock.findUserModelById(anyString())).thenThrow(new RuntimeException());

    String fooUserId = this.fooUser.getId();
    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(fooUserId));

    verify(userRepositoryMock, times(1)).findUserModelById(fooUserId);
    verify(userGenAIUsageRepositoryMock, times(1)).findUserGenAIUsageModelByUserId(fooUserId);
  }

  @Test
  void checkAndIncrementUsage_whenTwoConcurrentRequests_expectException() {
    UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    UserGenAIUsageRepository userGenAIUsageRepositoryMock =
        Mockito.mock(UserGenAIUsageRepository.class);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepositoryMock, userRepositoryMock);

    when(userGenAIUsageRepositoryMock.findUserGenAIUsageModelByUserId(anyString()))
        .thenReturn(this.userGenAIUsageModel);

    when(userGenAIUsageRepositoryMock.save(any()))
        .thenThrow(new OptimisticLockingFailureException("foo"));
    String fooUserId = this.fooUser.getId();
    assertThrows(
        ConcurrencyException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(fooUserId));

    verifyNoInteractions(userRepositoryMock);
    verify(userGenAIUsageRepositoryMock, times(1)).findUserGenAIUsageModelByUserId(fooUserId);

    verify(userGenAIUsageRepositoryMock, times(1)).save(any());
  }

  @ParameterizedTest
  @CsvSource({"5, 4", "1, 0", "0, 0"})
  void decrementUsage_expectCorrectCount(int initialCount, int expectedCount) {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(initialCount);
    this.userGenAIUsageRepository.save(userRecord);

    this.genAIUsageManagementService.decrementUsage(this.fooUser.getId());

    UserGenAIUsageModel updatedRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    assertEquals(expectedCount, updatedRecord.getRequestCount());
  }

  @Test
  void decrementUsage_whenUsageRecordNotFound_expectGenAIUsageManagementServiceException() {
    this.userGenAIUsageRepository.deleteAll();
    String fooUserId = this.fooUser.getId();
    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> this.genAIUsageManagementService.decrementUsage(fooUserId));
  }

  @Test
  void decrementUsage_whenUsageRepoOperationFails_expectGenAIUsageManagementServiceException() {
    UserGenAIUsageRepository userGenAIUsageRepositoryMock =
        Mockito.mock(UserGenAIUsageRepository.class);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepositoryMock, userRepository);

    when(userGenAIUsageRepositoryMock.findUserGenAIUsageModelByUserId(anyString()))
        .thenThrow(new RuntimeException("DB failed"));
    String fooUserId = this.fooUser.getId();
    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> this.genAIUsageManagementService.decrementUsage(fooUserId));

    verify(userGenAIUsageRepositoryMock, times(1)).findUserGenAIUsageModelByUserId(fooUserId);
  }

  @Test
  void decrementUsage_whenOptimisticLockingFails_expectConcurrencyException() {
    UserGenAIUsageRepository userGenAIUsageRepositoryMock =
        Mockito.mock(UserGenAIUsageRepository.class);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepositoryMock, userRepository);

    when(userGenAIUsageRepositoryMock.findUserGenAIUsageModelByUserId(anyString()))
        .thenReturn(this.userGenAIUsageModel);
    when(userGenAIUsageRepositoryMock.save(any()))
        .thenThrow(new OptimisticLockingFailureException("conflict"));
    String fooUserId = this.fooUser.getId();
    assertThrows(
        ConcurrencyException.class,
        () -> this.genAIUsageManagementService.decrementUsage(fooUserId));

    verify(userGenAIUsageRepositoryMock, times(1)).save(any());
  }

  @Test
  void getNumberOfRequestLeft_whenNoUsageRecordExists_expectDefaultLimit() {
    this.userGenAIUsageRepository.deleteAll();

    int result = this.genAIUsageManagementService.getNumberOfRequestLeft(this.fooUser.getId());

    assertEquals(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, result);
  }

  @Test
  void getNumberOfRequestLeft_whenUsageRecordExists_expectCorrectRemainingCount() {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(3);
    this.userGenAIUsageRepository.save(userRecord);

    int result = this.genAIUsageManagementService.getNumberOfRequestLeft(this.fooUser.getId());

    assertEquals(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT - 3, result);
  }

  @Test
  void getNumberOfRequestLeft_whenRequestCountIsZero_expectFullLimit() {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(0);
    this.userGenAIUsageRepository.save(userRecord);

    int result = this.genAIUsageManagementService.getNumberOfRequestLeft(this.fooUser.getId());

    assertEquals(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, result);
  }

  @Test
  void getNumberOfRequestLeft_whenRequestCountAtLimit_expectZeroRequestLeft() {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT);
    this.userGenAIUsageRepository.save(userRecord);

    int result = this.genAIUsageManagementService.getNumberOfRequestLeft(this.fooUser.getId());

    assertEquals(0, result);
  }

  @Test
  void getNumberOfRequestLeft_whenRequestCountOverLimit_expectZeroRequestLeft() {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 1);
    this.userGenAIUsageRepository.save(userRecord);

    int result = this.genAIUsageManagementService.getNumberOfRequestLeft(this.fooUser.getId());

    assertEquals(0, result);
  }

  @Test
  void getNumberOfRequestLeft_whenRepoOperationFails_expectGenAIUsageManagementServiceException() {
    UserGenAIUsageRepository userGenAIUsageRepositoryMock =
        Mockito.mock(UserGenAIUsageRepository.class);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepositoryMock, userRepository);

    when(userGenAIUsageRepositoryMock.findUserGenAIUsageModelByUserId(anyString()))
        .thenThrow(new RuntimeException("DB failed"));
    String fooUserId = this.fooUser.getId();
    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> this.genAIUsageManagementService.getNumberOfRequestLeft(fooUserId));

    verify(userGenAIUsageRepositoryMock, times(1)).findUserGenAIUsageModelByUserId(fooUserId);
  }
}
