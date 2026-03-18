package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.genai.ConcurrencyException;
import com.backend.coapp.exception.genai.GenAIQuotaExceededException;
import com.backend.coapp.exception.genai.GenAIUsageManagementServiceException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.UserGenAIUsageModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserGenAIUsageRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.GenAIUsageManagementService;
import com.backend.coapp.util.GenAIUsageConstants;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.OptimisticLockingFailureException;

/* these tests were written with the help of Claude Sonnet 4.6 and revised by Eric Hodgson */
class GenAIUsageManagementServiceUnitTest {

  private GenAIUsageManagementService genAIUsageManagementService;
  private UserGenAIUsageRepository mockUserGenAIUsageRepository;
  private UserRepository mockUserRepository;

  private final String USER_ID = "123";

  private UserModel fooUser;
  private UserGenAIUsageModel userGenAIUsageModel;

  @BeforeEach
  void setUp() {
    mockUserGenAIUsageRepository = Mockito.mock(UserGenAIUsageRepository.class);
    mockUserRepository = Mockito.mock(UserRepository.class);

    fooUser =
        new UserModel(
            USER_ID,
            "foo@mail.com",
            "password",
            "foo",
            "woof",
            true,
            UserModel.DEFAULT_VERIFICATION_CODE);

    userGenAIUsageModel =
        new UserGenAIUsageModel(
            USER_ID, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, LocalDateTime.now());

    genAIUsageManagementService =
        new GenAIUsageManagementService(mockUserGenAIUsageRepository, mockUserRepository);
  }

  // -------------------------------------------------------------------------
  // checkAndIncrementUsage — existing usage record
  // -------------------------------------------------------------------------

  @Test
  void checkAndIncrementUsage_whenUserExistInUsageRepoAlready_expectIncreaseByOne() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenReturn(userGenAIUsageModel);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    int originalCount = userGenAIUsageModel.getRequestCount(); // capture BEFORE service call

    genAIUsageManagementService.checkAndIncrementUsage(USER_ID);

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(originalCount + 1, captor.getValue().getRequestCount());
  }

  @Test
  void checkAndIncrementUsage_whenUserNotExistInUsageRepoYet_expectIncreaseByOne() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(null);
    when(mockUserRepository.findUserModelById(USER_ID)).thenReturn(fooUser);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    genAIUsageManagementService.checkAndIncrementUsage(USER_ID);

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(1, captor.getValue().getRequestCount());
  }

  @Test
  void checkAndIncrementUsage_whenUserNotExistInUserRepoYet_expectException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(null);
    when(mockUserRepository.findUserModelById(USER_ID)).thenReturn(null);

    assertThrows(
        UserNotFoundException.class,
        () -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));
  }

  // -------------------------------------------------------------------------
  // checkAndIncrementUsage — reset logic
  // -------------------------------------------------------------------------

  @Test
  void checkAndIncrementUsage_whenPastMonth_expectResetLimit() {
    UserGenAIUsageModel staleRecord =
        new UserGenAIUsageModel(
            USER_ID,
            GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT,
            LocalDateTime.now().minusMonths(1));
    staleRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10);

    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenReturn(staleRecord);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(1, captor.getValue().getRequestCount());
    assertEquals(LocalDateTime.now().getMonth(), captor.getValue().getLastReset().getMonth());
    assertEquals(
        LocalDateTime.now().getDayOfMonth(), captor.getValue().getLastReset().getDayOfMonth());
    assertEquals(LocalDateTime.now().getYear(), captor.getValue().getLastReset().getYear());
  }

  @Test
  void checkAndIncrementUsage_whenPastYear_expectResetLimit() {
    UserGenAIUsageModel staleRecord =
        new UserGenAIUsageModel(
            USER_ID,
            GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT,
            LocalDateTime.now().minusYears(1));
    staleRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10);

    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenReturn(staleRecord);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(1, captor.getValue().getRequestCount());
    assertEquals(LocalDateTime.now().getMonth(), captor.getValue().getLastReset().getMonth());
    assertEquals(
        LocalDateTime.now().getDayOfMonth(), captor.getValue().getLastReset().getDayOfMonth());
    assertEquals(LocalDateTime.now().getYear(), captor.getValue().getLastReset().getYear());
  }

  // -------------------------------------------------------------------------
  // checkAndIncrementUsage — quota limit
  // -------------------------------------------------------------------------

  @Test
  void checkAndIncrementUsage_whenOverLimit_expectException() {
    UserGenAIUsageModel overLimitRecord =
        new UserGenAIUsageModel(
            USER_ID, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, LocalDateTime.now());
    overLimitRecord.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 10);

    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenReturn(overLimitRecord);

    assertThrows(
        GenAIQuotaExceededException.class,
        () -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));

    verify(mockUserGenAIUsageRepository, never()).save(any());
  }

  // -------------------------------------------------------------------------
  // checkAndIncrementUsage — failure cases
  // -------------------------------------------------------------------------

  @Test
  void checkAndIncrementUsage_whenUserUsageRepoOperationFail_expectException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(anyString()))
        .thenThrow(new RuntimeException("DB failed"));

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));

    verifyNoInteractions(mockUserRepository);
    verify(mockUserGenAIUsageRepository, times(1)).findUserGenAIUsageModelByUserId(USER_ID);
  }

  @Test
  void checkAndIncrementUsage_whenUserRepoOperationFail_expectException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(anyString()))
        .thenReturn(null);
    when(mockUserRepository.findUserModelById(anyString()))
        .thenThrow(new RuntimeException("DB failed"));

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));

    verify(mockUserRepository, times(1)).findUserModelById(USER_ID);
    verify(mockUserGenAIUsageRepository, times(1)).findUserGenAIUsageModelByUserId(USER_ID);
  }

  @Test
  void checkAndIncrementUsage_whenTwoConcurrentRequests_expectException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(anyString()))
        .thenReturn(userGenAIUsageModel);
    when(mockUserGenAIUsageRepository.save(any()))
        .thenThrow(new OptimisticLockingFailureException("conflict"));

    assertThrows(
        ConcurrencyException.class,
        () -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));

    verifyNoInteractions(mockUserRepository);
    verify(mockUserGenAIUsageRepository, times(1)).findUserGenAIUsageModelByUserId(USER_ID);
    verify(mockUserGenAIUsageRepository, times(1)).save(any());
  }

  // -------------------------------------------------------------------------
  // decrementUsage
  // -------------------------------------------------------------------------

  @ParameterizedTest(name = "decrementUsage [{index}] count {0} -> {1}")
  @CsvSource({"5, 4", "1, 0", "0, 0"})
  void decrementUsage_whenUserExists_expectCorrectCount(int initialCount, int expectedCount) {
    UserGenAIUsageModel usageRecord =
        new UserGenAIUsageModel(
            USER_ID, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, LocalDateTime.now());
    usageRecord.setRequestCount(initialCount);
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenReturn(usageRecord);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    genAIUsageManagementService.decrementUsage(USER_ID);

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(expectedCount, captor.getValue().getRequestCount());
  }

  @Test
  void decrementUsage_whenUsageRecordNotFound_expectGenAIUsageManagementServiceException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(null);

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> genAIUsageManagementService.decrementUsage(USER_ID));

    verify(mockUserGenAIUsageRepository, never()).save(any());
  }

  @Test
  void decrementUsage_whenUsageRepoOperationFails_expectGenAIUsageManagementServiceException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(anyString()))
        .thenThrow(new RuntimeException("DB failed"));

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> genAIUsageManagementService.decrementUsage(USER_ID));

    verify(mockUserGenAIUsageRepository, times(1)).findUserGenAIUsageModelByUserId(USER_ID);
  }

  @Test
  void decrementUsage_whenOptimisticLockingFails_expectConcurrencyException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(anyString()))
        .thenReturn(userGenAIUsageModel);
    when(mockUserGenAIUsageRepository.save(any()))
        .thenThrow(new OptimisticLockingFailureException("conflict"));

    assertThrows(
        ConcurrencyException.class, () -> genAIUsageManagementService.decrementUsage(USER_ID));

    verify(mockUserGenAIUsageRepository, times(1)).save(any());
  }

  @Test
  void checkAndIncrementUsage_whenAtExactLimit_expectException() {
    UserGenAIUsageModel atLimitRecord =
        new UserGenAIUsageModel(
            USER_ID, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, LocalDateTime.now());
    atLimitRecord.setRequestCount(
        GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT); // exactly at limit

    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenReturn(atLimitRecord);

    assertThrows(
        GenAIQuotaExceededException.class,
        () -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));

    verify(mockUserGenAIUsageRepository, never()).save(any());
  }

  // -------------------------------------------------------------------------
  // getNumberOfRequestLeft
  // -------------------------------------------------------------------------

  @Test
  void getNumberOfRequestLeft_whenNoUsageRecord_expectDefaultLimit() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(null);

    int result = genAIUsageManagementService.getNumberOfRequestLeft(USER_ID);

    assertEquals(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, result);
  }

  @Test
  void getNumberOfRequestLeft_whenUsageRecordExists_expectRemainingRequests() {
    userGenAIUsageModel.setRequestCount(3);
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenReturn(userGenAIUsageModel);

    int result = genAIUsageManagementService.getNumberOfRequestLeft(USER_ID);

    assertEquals(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT - 3, result);
  }

  @Test
  void getNumberOfRequestLeft_whenRequestCountExceedsLimit_expectZero() {
    userGenAIUsageModel.setRequestCount(GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT + 5);
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenReturn(userGenAIUsageModel);

    int result = genAIUsageManagementService.getNumberOfRequestLeft(USER_ID);

    assertEquals(0, result);
  }

  @Test
  void getNumberOfRequestLeft_whenDbFails_expectException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID))
        .thenThrow(new RuntimeException("DB failed"));

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> genAIUsageManagementService.getNumberOfRequestLeft(USER_ID));
  }
}
