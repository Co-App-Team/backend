package com.backend.coapp.mutation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserExperienceModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserExperienceRepository;
import com.backend.coapp.repository.UserRepository;
import com.backend.coapp.service.UserService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

public class UserServiceUnitTest {

  private UserService userService;
  private UserRepository mockUserRepository;
  private UserExperienceRepository mockUserExperienceRepository;
  private CompanyRepository mockCompanyRepository;
  private PasswordEncoder passwordEncoder;

  private UserModel fooUserNotActivated;
  private UserModel fooUserActivated;
  private UserExperienceModel fooExperience1;
  private UserExperienceModel fooExperience2;
  private CompanyModel fooCompany;

  private final String RAW_PASSWORD = "password123";
  private final LocalDate START_DATE = LocalDate.now().minusYears(1);
  private final LocalDate END_DATE = LocalDate.now();

  @BeforeEach
  public void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    mockUserRepository = Mockito.mock(UserRepository.class);
    mockUserExperienceRepository = Mockito.mock(UserExperienceRepository.class);
    mockCompanyRepository = Mockito.mock(CompanyRepository.class);

    fooUserNotActivated =
        new UserModel(
            "123", "foo@mail.com", passwordEncoder.encode(RAW_PASSWORD), "foo", "woof", false, 123);

    fooUserActivated =
        new UserModel(
            "789",
            "fooActivated@mail.com",
            passwordEncoder.encode(RAW_PASSWORD),
            "fooActivated",
            "woof",
            true,
            UserModel.DEFAULT_VERIFICATION_CODE);
    fooUserActivated.setVerified(true);

    fooExperience1 =
        new UserExperienceModel(
            "789", "companyA", "Software Engineer", "Some description", START_DATE, END_DATE);
    ReflectionTestUtils.setField(fooExperience1, "id", "exp_001");

    fooExperience2 =
        new UserExperienceModel(
            "789", "companyB", "Tech Lead", "Another description", START_DATE, null);
    ReflectionTestUtils.setField(fooExperience2, "id", "exp_002");

    fooCompany = new CompanyModel("Foo Company", "Foo Location", "https://foo.com");
    ReflectionTestUtils.setField(fooCompany, "id", "company_001");

    userService =
        new UserService(
            mockUserRepository,
            passwordEncoder,
            mockUserExperienceRepository,
            mockCompanyRepository);
  }

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  @Test
  public void constructor_expectSameInitInstance() {
    assertSame(mockUserRepository, userService.getUserRepository());
  }

  // -------------------------------------------------------------------------
  // getDummyUser
  // -------------------------------------------------------------------------

  @Test
  public void getDummyUser_expectInitValues() {
    UserResponse user = userService.getDummyUser();

    assertNotNull(user);
    assertEquals("Dummy Firstname", user.getFirstName());
    assertEquals("Dummy Lastname", user.getLastName());
    assertEquals("foo@mail.com", user.getEmail());
  }

  // -------------------------------------------------------------------------
  // updateUserPassword
  // -------------------------------------------------------------------------

  @Test
  public void updateUserPassword_whenOldPasswordMatch_expectNoException() {
    when(mockUserRepository.findUserModelById("789")).thenReturn(fooUserActivated);
    when(mockUserRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> userService.updateUserPassword("789", RAW_PASSWORD, "newPassword"));

    ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
    verify(mockUserRepository).save(captor.capture());
    assertTrue(passwordEncoder.matches("newPassword", captor.getValue().getPassword()));
  }

  @Test
  public void updatePassword_whenNoAccountFound_expectException() {
    when(mockUserRepository.findUserModelById("notExistUser")).thenReturn(null);

    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> userService.updateUserPassword("notExistUser", "1", "2"));
  }

  @Test
  public void updatePassword_whenAccountNotYetActivated_expectException() {
    when(mockUserRepository.findUserModelById("123")).thenReturn(fooUserNotActivated);

    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () -> userService.updateUserPassword("123", RAW_PASSWORD, "newPassword"));
  }

  @Test
  public void updatePassword_whenOldPasswordNotMatch_expectException() {
    when(mockUserRepository.findUserModelById("789")).thenReturn(fooUserActivated);

    assertThrows(
        AuthBadCredentialException.class,
        () -> userService.updateUserPassword("789", "wrongPassword", "newPassword"));
  }

  @Test
  public void updatePassword_whenNewPasswordSameWithOldPassword_expectException() {
    when(mockUserRepository.findUserModelById("789")).thenReturn(fooUserActivated);

    assertThrows(
        UserInvalidPasswordChangeException.class,
        () -> userService.updateUserPassword("789", "fooEmail", "fooEmail"));
  }

  @Test
  public void updatePassword_whenDatabaseSaveOperationFail_expectException() {
    when(mockUserRepository.findUserModelById("789")).thenReturn(fooUserActivated);
    when(mockUserRepository.save(any(UserModel.class))).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () -> userService.updateUserPassword("789", RAW_PASSWORD, "newPassword"));

    verify(mockUserRepository, times(1)).save(any(UserModel.class));
  }

  @Test
  public void updatePassword_whenDatabaseFindOperationFail_expectException() {
    when(mockUserRepository.findUserModelById(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () -> userService.updateUserPassword("789", RAW_PASSWORD, "newPassword"));

    verify(mockUserRepository, never()).save(any(UserModel.class));
  }

  // -------------------------------------------------------------------------
  // getUserInformationFromUserID
  // -------------------------------------------------------------------------

  @Test
  public void getUserInformationFromUserID_whenUserExists_expectReturnUser() {
    when(mockUserRepository.findUserModelById("789")).thenReturn(fooUserActivated);

    UserModel user = userService.getUserInformationFromUserID("789");

    assertThat(fooUserActivated).usingRecursiveComparison().isEqualTo(user);
  }

  @Test
  public void getUserInformationFromUserID_whenUserNotExists_expectException() {
    when(mockUserRepository.findUserModelById("999")).thenReturn(null);

    assertThrows(
        UserNotExistException.class, () -> userService.getUserInformationFromUserID("999"));
  }

  @Test
  public void getUserInformationFromUserID_whenUserRepoFail_expectException() {
    when(mockUserRepository.findUserModelById(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class, () -> userService.getUserInformationFromUserID("789"));
  }

  // -------------------------------------------------------------------------
  // getAllUserExperience
  // -------------------------------------------------------------------------

  @Test
  public void getAllUserExperience_whenUserHasExperiences_expectReturnAll() {
    when(mockUserExperienceRepository.findAllByUserId("789"))
        .thenReturn(List.of(fooExperience1, fooExperience2));

    List<UserExperienceModel> results = userService.getAllUserExperience("789");

    assertThat(results).hasSize(2).extracting("companyId").contains("companyA", "companyB");
  }

  @Test
  public void getAllUserExperience_whenUserHasNoExperiences_expectReturnEmptyList() {
    when(mockUserExperienceRepository.findAllByUserId("789")).thenReturn(List.of());

    List<UserExperienceModel> results = userService.getAllUserExperience("789");

    assertThat(results).isEmpty();
  }

  @Test
  public void getAllUserExperience_whenRepositoryThrows_expectUserServiceFailException() {
    when(mockUserExperienceRepository.findAllByUserId(any())).thenThrow(new RuntimeException());

    assertThrows(UserServiceFailException.class, () -> userService.getAllUserExperience("789"));
  }

  // -------------------------------------------------------------------------
  // createNewUserExperience
  // -------------------------------------------------------------------------

  @Test
  public void createNewUserExperience_whenValidArgs_expectReturnSavedModel() {
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockUserExperienceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    UserExperienceModel result =
        userService.createNewUserExperience(
            "789", "company_001", "Software Engineer", "Some description", START_DATE, END_DATE);

    assertNotNull(result);
    assertEquals("789", result.getUserId());
    assertEquals("company_001", result.getCompanyId());
    assertEquals("Software Engineer", result.getRoleTitle());
    assertEquals("Some description", result.getRoleDescription());
    assertEquals(START_DATE, result.getStartDate());
    assertEquals(END_DATE, result.getEndDate());
  }

  @Test
  public void createNewUserExperience_whenEndDateIsNull_expectReturnSavedModel() {
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockUserExperienceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    UserExperienceModel result =
        userService.createNewUserExperience(
            "789", "company_001", "Software Engineer", "Some description", START_DATE, null);

    assertNotNull(result);
    assertNull(result.getEndDate());
  }

  @Test
  public void createNewUserExperience_whenCompanyNotFound_expectCompanyNotFoundException() {
    when(mockCompanyRepository.existsById("nonExistentCompanyId")).thenReturn(false);

    assertThrows(
        CompanyNotFoundException.class,
        () ->
            userService.createNewUserExperience(
                "789",
                "nonExistentCompanyId",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void createNewUserExperience_whenRepositoryThrows_expectUserServiceFailException() {
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockUserExperienceRepository.save(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.createNewUserExperience(
                "789",
                "company_001",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void createNewUserExperience_whenCompanyIdIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.createNewUserExperience(
                "789", null, "Software Engineer", "Some description", START_DATE, END_DATE));
  }

  @Test
  public void createNewUserExperience_whenRoleTitleIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.createNewUserExperience(
                "789", "company_001", null, "Some description", START_DATE, END_DATE));
  }

  @Test
  public void createNewUserExperience_whenRoleDescriptionIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.createNewUserExperience(
                "789", "company_001", "Software Engineer", null, START_DATE, END_DATE));
  }

  @Test
  public void createNewUserExperience_whenStartDateIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.createNewUserExperience(
                "789", "company_001", "Software Engineer", "Some description", null, END_DATE));
  }

  @Test
  public void
      createNewUserExperience_whenEndDateIsBeforeStartDate_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.createNewUserExperience(
                "789",
                "company_001",
                "Software Engineer",
                "Some description",
                START_DATE,
                START_DATE.minusDays(1)));
  }

  @Test
  public void createNewUserExperience_whenEndDateIsAfterStartDate_expectSuccess() {
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockUserExperienceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    UserExperienceModel result =
        userService.createNewUserExperience(
            "789", "company_001", "Software Engineer", "Some description", START_DATE, END_DATE);

    assertThat(result).isNotNull();
  }

  // -------------------------------------------------------------------------
  // deleteUserExperience
  // -------------------------------------------------------------------------

  @Test
  public void deleteUserExperience_whenValidArgs_expectExperienceDeleted() {
    when(mockUserExperienceRepository.findById("exp_001")).thenReturn(Optional.of(fooExperience1));

    assertDoesNotThrow(() -> userService.deleteUserExperience("exp_001", "789"));

    verify(mockUserExperienceRepository, times(1)).deleteById("exp_001");
  }

  @Test
  public void deleteUserExperience_whenExperienceNotFound_expectExperienceNotFoundException() {
    when(mockUserExperienceRepository.findById("nonExistentExperienceId"))
        .thenReturn(Optional.empty());

    assertThrows(
        ExperienceNotFoundException.class,
        () -> userService.deleteUserExperience("nonExistentExperienceId", "789"));
  }

  @Test
  public void
      deleteUserExperience_whenExperienceNotOwnedByUser_expectExperienceNotOwnedException() {
    when(mockUserExperienceRepository.findById("exp_001")).thenReturn(Optional.of(fooExperience1));

    assertThrows(
        ExperienceNotOwnedException.class,
        () -> userService.deleteUserExperience("exp_001", "anotherUserId"));
  }

  @Test
  public void deleteUserExperience_whenRepositoryThrows_expectUserServiceFailException() {
    when(mockUserExperienceRepository.findById(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class, () -> userService.deleteUserExperience("exp_001", "789"));
  }

  // -------------------------------------------------------------------------
  // updateUserExperience
  // -------------------------------------------------------------------------

  @Test
  public void updateUserExperience_whenValidArgs_expectAllFieldsUpdated() {
    LocalDate newStartDate = LocalDate.now().minusYears(2);
    LocalDate newEndDate = LocalDate.now().minusMonths(1);

    when(mockUserExperienceRepository.findById("exp_001")).thenReturn(Optional.of(fooExperience1));
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockUserExperienceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    userService.updateUserExperience(
        "exp_001",
        "789",
        "company_001",
        "Senior Software Engineer",
        "Updated description",
        newStartDate,
        newEndDate);

    ArgumentCaptor<UserExperienceModel> captor = ArgumentCaptor.forClass(UserExperienceModel.class);
    verify(mockUserExperienceRepository).save(captor.capture());
    assertEquals("company_001", captor.getValue().getCompanyId());
    assertEquals("Senior Software Engineer", captor.getValue().getRoleTitle());
    assertEquals("Updated description", captor.getValue().getRoleDescription());
    assertEquals(newStartDate, captor.getValue().getStartDate());
    assertEquals(newEndDate, captor.getValue().getEndDate());
  }

  @Test
  public void updateUserExperience_whenEndDateIsNull_expectEndDateCleared() {
    when(mockUserExperienceRepository.findById("exp_001")).thenReturn(Optional.of(fooExperience1));
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockUserExperienceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    userService.updateUserExperience(
        "exp_001", "789", "company_001", "Software Engineer", "Some description", START_DATE, null);

    ArgumentCaptor<UserExperienceModel> captor = ArgumentCaptor.forClass(UserExperienceModel.class);
    verify(mockUserExperienceRepository).save(captor.capture());
    assertNull(captor.getValue().getEndDate());
  }

  @Test
  public void updateUserExperience_whenCompanyIdIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.updateUserExperience(
                "exp_001",
                "789",
                null,
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenRoleTitleIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.updateUserExperience(
                "exp_001", "789", "company_001", null, "Some description", START_DATE, END_DATE));
  }

  @Test
  public void updateUserExperience_whenRoleDescriptionIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.updateUserExperience(
                "exp_001", "789", "company_001", "Software Engineer", null, START_DATE, END_DATE));
  }

  @Test
  public void updateUserExperience_whenStartDateIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.updateUserExperience(
                "exp_001",
                "789",
                "company_001",
                "Software Engineer",
                "Some description",
                null,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenStartDateAfterEndDate_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.updateUserExperience(
                "exp_001",
                "789",
                "company_001",
                "Software Engineer",
                "Some description",
                START_DATE,
                START_DATE.minusDays(1)));
  }

  @Test
  public void updateUserExperience_whenExperienceNotFound_expectExperienceNotFoundException() {
    when(mockUserExperienceRepository.findById("nonExistentExperienceId"))
        .thenReturn(Optional.empty());

    assertThrows(
        ExperienceNotFoundException.class,
        () ->
            userService.updateUserExperience(
                "nonExistentExperienceId",
                "789",
                "company_001",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void
      updateUserExperience_whenExperienceNotOwnedByUser_expectExperienceNotOwnedException() {
    when(mockUserExperienceRepository.findById("exp_001")).thenReturn(Optional.of(fooExperience1));
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);

    assertThrows(
        ExperienceNotOwnedException.class,
        () ->
            userService.updateUserExperience(
                "exp_001",
                "anotherUserId",
                "company_001",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenCompanyNotFound_expectCompanyNotFoundException() {
    when(mockUserExperienceRepository.findById("exp_001")).thenReturn(Optional.of(fooExperience1));
    when(mockCompanyRepository.existsById("nonExistentCompanyId")).thenReturn(false);

    assertThrows(
        CompanyNotFoundException.class,
        () ->
            userService.updateUserExperience(
                "exp_001",
                "789",
                "nonExistentCompanyId",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenRepositoryThrows_expectUserServiceFailException() {
    when(mockUserExperienceRepository.findById(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () ->
            userService.updateUserExperience(
                "exp_001",
                "789",
                "company_001",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenUpdated_expectPersistedInDatabase() {
    when(mockUserExperienceRepository.findById("exp_001")).thenReturn(Optional.of(fooExperience1));
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockUserExperienceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    userService.updateUserExperience(
        "exp_001",
        "789",
        "company_001",
        "Senior Software Engineer",
        "Updated description",
        START_DATE,
        END_DATE);

    ArgumentCaptor<UserExperienceModel> captor = ArgumentCaptor.forClass(UserExperienceModel.class);
    verify(mockUserExperienceRepository).save(captor.capture());
    assertThat(captor.getValue()).isNotNull();
    assertThat(captor.getValue().getRoleTitle()).isEqualTo("Senior Software Engineer");
  }
}
