package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.GenAIQuotaExceededException;
import com.backend.coapp.exception.GenAIUsageManagementServiceException;
import com.backend.coapp.exception.UserNotExistException;
import com.backend.coapp.model.document.UserGenAIUsageModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserGenAIUsageRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.util.GenAIUsageConstants;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class GenAIUsageManagementServiceTest {
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
  public void setUp() {
    this.userRepository.deleteAll();
    this.userGenAIUsageRepository.deleteAll();
    this.userRepository.save(this.fooUser);
    this.userGenAIUsageRepository.save(this.userGenAIUsageModel);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepository, userRepository);
  }

  @Test
  public void checkAndIncrementUsage_whenUserExistInUsageRepoAlready_expectIncreaseByOne() {
    this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId());

    UserGenAIUsageModel userUsageRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    assertNotNull(userUsageRecord);
    assertEquals(this.userGenAIUsageModel.getRequestCount() + 1, userUsageRecord.getRequestCount());
  }

  @Test
  public void checkAndIncrementUsage_whenUserNotExistInUsageRepoYet_expectIncreaseByOne() {
    this.userGenAIUsageRepository.deleteAll();
    this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId());

    UserGenAIUsageModel userUsageRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    assertNotNull(userUsageRecord);
    assertEquals(1, userUsageRecord.getRequestCount());
  }

  @Test
  public void checkAndIncrementUsage_whenUserNotExistInUserRepoYet_expectException() {
    this.userGenAIUsageRepository.deleteAll();
    this.userRepository.deleteAll();

    assertThrows(
        UserNotExistException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId()));
  }

  @Test
  public void checkAndIncrementUsage_whenPastMonth_expectResetLimit() {
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
  public void checkAndIncrementUsage_whenPastYear_expectResetLimit() {
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
  public void checkAndIncrementUsage_whenOverLimit_expectException() {
    UserGenAIUsageModel userRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    userRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10);
    this.userGenAIUsageRepository.save(userRecord);

    assertThrows(
        GenAIQuotaExceededException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId()));

    UserGenAIUsageModel userUsageRecord =
        this.userGenAIUsageRepository.findUserGenAIUsageModelByUserId(this.fooUser.getId());
    assertNotNull(userUsageRecord);
    assertEquals(
        GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10, userUsageRecord.getRequestCount());
  }

  @Test
  public void checkAndIncrementUsage_whenUserUsageRepoOperationFail_expectException() {
    UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    UserGenAIUsageRepository userGenAIUsageRepositoryMock =
        Mockito.mock(UserGenAIUsageRepository.class);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepositoryMock, userRepositoryMock);

    when(userGenAIUsageRepositoryMock.findUserGenAIUsageModelByUserId(anyString()))
        .thenThrow(new RuntimeException());

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId()));

    verifyNoInteractions(userRepositoryMock);
    verify(userGenAIUsageRepositoryMock, times(1))
        .findUserGenAIUsageModelByUserId(this.fooUser.getId());
  }

  @Test
  public void checkAndIncrementUsage_whenUserRepoOperationFail_expectException() {
    UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    UserGenAIUsageRepository userGenAIUsageRepositoryMock =
        Mockito.mock(UserGenAIUsageRepository.class);
    this.genAIUsageManagementService =
        new GenAIUsageManagementService(userGenAIUsageRepositoryMock, userRepositoryMock);

    when(userGenAIUsageRepositoryMock.findUserGenAIUsageModelByUserId(anyString()))
        .thenReturn(null);
    when(userRepositoryMock.findUserModelById(anyString())).thenThrow(new RuntimeException());

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> this.genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId()));

    verify(userRepositoryMock, times(1)).findUserModelById(this.fooUser.getId());
    verify(userGenAIUsageRepositoryMock, times(1))
        .findUserGenAIUsageModelByUserId(this.fooUser.getId());
  }

  @Test
  public void checkAndIncrementUsage_whenTwoConcurrentRequests_expectOneSuccessOneFailure()
      throws InterruptedException {
    CyclicBarrier barrier = new CyclicBarrier(2); // forces both threads to meet before proceeding
    CountDownLatch doneLatch = new CountDownLatch(2);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    ExecutorService executor = Executors.newFixedThreadPool(2);

    for (int i = 0; i < 2; i++) {
      executor.submit(
          () -> {
            try {
              barrier.await(); // both threads must reach here before either proceeds
              genAIUsageManagementService.checkAndIncrementUsage(this.fooUser.getId());
              successCount.incrementAndGet();
            } catch (GenAIUsageManagementServiceException e) {
              failureCount.incrementAndGet();
            } catch (Exception e) {
              failureCount.incrementAndGet();
            } finally {
              doneLatch.countDown();
            }
          });
    }

    doneLatch.await(10, TimeUnit.SECONDS); // timeout to avoid hanging forever
    executor.shutdown();

    assertEquals(1, successCount.get());
    assertEquals(1, failureCount.get());
  }
}
