package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.UserGenAIUsageModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserGenAIUsageRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.GenAIUsageManagementService;
import com.backend.coapp.util.GenAIUsageConstants;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.OptimisticLockingFailureException;

public class GenAIUsageManagementServiceUnitTest {

  private GenAIUsageManagementService genAIUsageManagementService;
  private UserGenAIUsageRepository mockUserGenAIUsageRepository;
  private UserRepository mockUserRepository;

  private final String USER_ID = "123";

  private UserModel fooUser;
  private UserGenAIUsageModel userGenAIUsageModel;

  @BeforeEach
  public void setUp() {
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
  public void checkAndIncrementUsage_whenUserExistInUsageRepoAlready_expectIncreaseByOne() {
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
  public void checkAndIncrementUsage_whenUserNotExistInUsageRepoYet_expectIncreaseByOne() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(null);
    when(mockUserRepository.findUserModelById(USER_ID)).thenReturn(fooUser);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    genAIUsageManagementService.checkAndIncrementUsage(USER_ID);

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(1, captor.getValue().getRequestCount());
  }

  @Test
  public void checkAndIncrementUsage_whenUserNotExistInUserRepoYet_expectException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(null);
    when(mockUserRepository.findUserModelById(USER_ID)).thenReturn(null);

    assertThrows(
        UserNotExistException.class,
        () -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));
  }

  // -------------------------------------------------------------------------
  // checkAndIncrementUsage — reset logic
  // -------------------------------------------------------------------------

  @Test
  public void checkAndIncrementUsage_whenPastMonth_expectResetLimit() {
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
  public void checkAndIncrementUsage_whenPastYear_expectResetLimit() {
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
  public void checkAndIncrementUsage_whenOverLimit_expectException() {
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
  public void checkAndIncrementUsage_whenUserUsageRepoOperationFail_expectException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(anyString()))
        .thenThrow(new RuntimeException("DB failed"));

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> genAIUsageManagementService.checkAndIncrementUsage(USER_ID));

    verifyNoInteractions(mockUserRepository);
    verify(mockUserGenAIUsageRepository, times(1)).findUserGenAIUsageModelByUserId(USER_ID);
  }

  @Test
  public void checkAndIncrementUsage_whenUserRepoOperationFail_expectException() {
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
  public void checkAndIncrementUsage_whenTwoConcurrentRequests_expectException() {
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

  @Test
  public void decrementUsage_whenUserExistsWithCount_expectDecrementByOne() {
    UserGenAIUsageModel record =
        new UserGenAIUsageModel(
            USER_ID, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, LocalDateTime.now());
    record.setRequestCount(5);

    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(record);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    genAIUsageManagementService.decrementUsage(USER_ID);

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(4, captor.getValue().getRequestCount());
  }

  @Test
  public void decrementUsage_whenRequestCountIsOne_expectDecrementToZero() {
    UserGenAIUsageModel record =
        new UserGenAIUsageModel(
            USER_ID, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, LocalDateTime.now());
    record.setRequestCount(1);

    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(record);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    genAIUsageManagementService.decrementUsage(USER_ID);

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(0, captor.getValue().getRequestCount());
  }

  @Test
  public void decrementUsage_whenRequestCountIsZero_expectCountStaysAtZero() {
    UserGenAIUsageModel record =
        new UserGenAIUsageModel(
            USER_ID, GenAIUsageConstants.DEFAULT_GEN_AI_USAGE_LIMIT, LocalDateTime.now());
    record.setRequestCount(0);

    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(record);
    when(mockUserGenAIUsageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    genAIUsageManagementService.decrementUsage(USER_ID);

    ArgumentCaptor<UserGenAIUsageModel> captor = ArgumentCaptor.forClass(UserGenAIUsageModel.class);
    verify(mockUserGenAIUsageRepository).save(captor.capture());
    assertEquals(0, captor.getValue().getRequestCount());
  }

  @Test
  public void decrementUsage_whenUsageRecordNotFound_expectGenAIUsageManagementServiceException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(USER_ID)).thenReturn(null);

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> genAIUsageManagementService.decrementUsage(USER_ID));

    verify(mockUserGenAIUsageRepository, never()).save(any());
  }

  @Test
  public void
      decrementUsage_whenUsageRepoOperationFails_expectGenAIUsageManagementServiceException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(anyString()))
        .thenThrow(new RuntimeException("DB failed"));

    assertThrows(
        GenAIUsageManagementServiceException.class,
        () -> genAIUsageManagementService.decrementUsage(USER_ID));

    verify(mockUserGenAIUsageRepository, times(1)).findUserGenAIUsageModelByUserId(USER_ID);
  }

  @Test
  public void decrementUsage_whenOptimisticLockingFails_expectConcurrencyException() {
    when(mockUserGenAIUsageRepository.findUserGenAIUsageModelByUserId(anyString()))
        .thenReturn(userGenAIUsageModel);
    when(mockUserGenAIUsageRepository.save(any()))
        .thenThrow(new OptimisticLockingFailureException("conflict"));

    assertThrows(
        ConcurrencyException.class, () -> genAIUsageManagementService.decrementUsage(USER_ID));

    verify(mockUserGenAIUsageRepository, times(1)).save(any());
  }

  @Test
  public void checkAndIncrementUsage_whenAtExactLimit_expectException() {
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
}
