package com.backend.coapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.UserExperienceModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.UserExperienceRepository;
import com.backend.coapp.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Parts of the unit test are generated with help of Claude (Sonnet 4.6) and revised by Bao Ngo */
@SpringBootTest
@Testcontainers
public class UserServiceTest {
  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserExperienceRepository userExperienceRepository;
  @Autowired private CompanyRepository companyRepository;

  private UserRepository mockUserRepository;
  private UserExperienceRepository mockUserExperienceRepository;
  private CompanyRepository mockCompanyRepository;
  private UserService userService;
  private UserModel fooUserNotActivated;
  private UserModel fooUserActivated;
  private final String RAW_PASSWORD = "password123";

  private UserExperienceModel fooExperience1;
  private UserExperienceModel fooExperience2;
  private final LocalDate START_DATE = LocalDate.now().minusYears(1);
  private final LocalDate END_DATE = LocalDate.now();

  private CompanyModel fooCompany;

  // call this in setUp() to seed company data when needed
  private void setUpCompany() {
    this.companyRepository.deleteAll();
    fooCompany =
        this.companyRepository.save(
            new CompanyModel("Foo Company", "Foo Location", "https://foo.com"));
  }

  private void setUpExperiences() {
    // Since not all tests need this. We only call this when it is needed.
    this.userExperienceRepository.deleteAll();
    fooExperience1 =
        this.userExperienceRepository.save(
            new UserExperienceModel(
                fooUserActivated.getId(),
                "companyA",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
    fooExperience2 =
        this.userExperienceRepository.save(
            new UserExperienceModel(
                fooUserActivated.getId(),
                "companyB",
                "Tech Lead",
                "Another description",
                START_DATE,
                null));
  }

  @BeforeEach
  public void setUp() {
    this.userRepository.deleteAll();
    this.userExperienceRepository.deleteAll();
    this.companyRepository.deleteAll();
    this.fooUserNotActivated =
        new UserModel(
            "123",
            "foo@mail.com",
            this.passwordEncoder.encode(RAW_PASSWORD),
            "foo",
            "woof",
            false,
            123);
    this.userRepository.save(fooUserNotActivated);
    this.fooUserActivated =
        new UserModel(
            "789",
            "fooActivated@mail.com",
            this.passwordEncoder.encode(RAW_PASSWORD),
            "fooActivated",
            "woof",
            true,
            UserModel.DEFAULT_VERIFICATION_CODE);
    this.fooUserActivated.setVerified(true);
    this.userRepository.save(this.fooUserActivated);
    this.mockUserRepository = Mockito.mock(UserRepository.class);
    this.mockCompanyRepository = Mockito.mock(CompanyRepository.class);
    this.mockUserExperienceRepository = Mockito.mock(UserExperienceRepository.class);
    this.userService =
        new UserService(
            this.userRepository, this.passwordEncoder, userExperienceRepository, companyRepository);
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertSame(this.userRepository, this.userService.getUserRepository());
  }

  @Test
  public void getDummyUser_expectInitValues() {
    UserResponse user = this.userService.getDummyUser();

    assertNotNull(user);
    assertEquals("Dummy Firstname", user.getFirstName());
    assertEquals("Dummy Lastname", user.getLastName());
    assertEquals("foo@mail.com", user.getEmail());
  }

  @Test
  public void updateUserPassword_whenOldPasswordMatch_expectNoException() {
    assertDoesNotThrow(
        () ->
            this.userService.updateUserPassword(
                this.fooUserActivated.getId(), this.RAW_PASSWORD, "newPassword"));
    UserModel user = this.userRepository.findUserModelById(this.fooUserActivated.getId());
    assertNotNull(user);
    assertTrue(this.passwordEncoder.matches("newPassword", user.getPassword()));
  }

  @Test
  public void updatePassword_whenNoAccountFound_expectException() {
    assertThrows(
        AuthEmailNotRegisteredException.class,
        () -> this.userService.updateUserPassword("notExistUser", "1", "2"));
  }

  @Test
  public void updatePassword_whenNoAccountNotYetActivated_expectException() {
    assertThrows(
        AuthAccountNotYetActivatedException.class,
        () ->
            this.userService.updateUserPassword(
                this.fooUserNotActivated.getId(),
                this.fooUserNotActivated.getPassword(),
                "newPassword"));
  }

  @Test
  public void updatePassword_whenOldPasswordNotMatch_expectException() {
    assertThrows(
        AuthBadCredentialException.class,
        () ->
            this.userService.updateUserPassword(
                this.fooUserActivated.getId(), "fooEmail", "newPassword"));
  }

  @Test
  public void updatePassword_whenNewPasswordSameWithOldPassword_expectException() {
    assertThrows(
        UserInvalidPasswordChangeException.class,
        () ->
            this.userService.updateUserPassword(
                this.fooUserActivated.getId(), "fooEmail", "fooEmail"));
  }

  @Test
  public void udpatePassword_whenDatabaseSaveOperationFail_expectException() {
    this.userService =
        new UserService(
            this.mockUserRepository,
            this.passwordEncoder,
            this.mockUserExperienceRepository,
            this.mockCompanyRepository);

    when(this.mockUserRepository.save(any(UserModel.class))).thenThrow(new RuntimeException());
    when(this.mockUserRepository.findUserModelById(anyString())).thenReturn(this.fooUserActivated);

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.updateUserPassword(
                this.fooUserActivated.getId(), this.RAW_PASSWORD, "newPassword"));

    verify(this.mockUserRepository, times(1)).save(any(UserModel.class));
  }

  @Test
  public void udpatePassword_whenDatabaseFindOperationFail_expectException() {
    this.userService =
        new UserService(
            this.mockUserRepository,
            this.passwordEncoder,
            this.mockUserExperienceRepository,
            this.mockCompanyRepository);

    when(this.mockUserRepository.findUserModelById(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.updateUserPassword(
                this.fooUserActivated.getId(), this.fooUserActivated.getPassword(), "newPassword"));

    verify(this.mockUserRepository, never()).save(any(UserModel.class));
  }

  @Test
  public void getUserInformationFromUserID_whenUserExit_expectReturnUser() {
    UserModel user = this.userService.getUserInformationFromUserID(this.fooUserActivated.getId());

    assertThat(this.fooUserActivated).usingRecursiveComparison().isEqualTo(user);
  }

  @Test
  public void getUserInformationFromUserID_whenUserNotExit_expectException() {
    assertThrows(
        UserNotExistException.class, () -> this.userService.getUserInformationFromUserID("999"));
  }

  @Test
  public void getUserInformationFromUserID_whenUserRepoFail_expectException() {
    this.userService =
        new UserService(
            this.mockUserRepository,
            this.passwordEncoder,
            this.mockUserExperienceRepository,
            this.mockCompanyRepository);

    when(this.mockUserRepository.findUserModelById(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () -> this.userService.getUserInformationFromUserID(this.fooUserActivated.getId()));
  }

  @Test
  public void getAllUserExperience_whenUserHasExperiences_expectReturnAll() {
    setUpExperiences();

    List<UserExperienceModel> results =
        this.userService.getAllUserExperience(fooUserActivated.getId());

    assertThat(results).hasSize(2).extracting("companyId").contains("companyA", "companyB");
  }

  @Test
  public void getAllUserExperience_whenUserHasNoExperiences_expectReturnEmptyList() {
    this.userExperienceRepository.deleteAll();

    List<UserExperienceModel> results =
        this.userService.getAllUserExperience(fooUserActivated.getId());

    assertThat(results).isEmpty();
  }

  @Test
  public void getAllUserExperience_whenRepositoryThrows_expectUserServiceFailException() {
    UserService serviceWithMockRepo =
        new UserService(
            this.userRepository,
            this.passwordEncoder,
            this.mockUserExperienceRepository,
            this.companyRepository);

    when(mockUserExperienceRepository.findAllByUserId(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () -> serviceWithMockRepo.getAllUserExperience(fooUserActivated.getId()));
  }

  @Test
  public void createNewUserExperience_whenValidArgs_expectReturnSavedModel() {
    setUpCompany();

    UserExperienceModel result =
        this.userService.createNewUserExperience(
            fooUserActivated.getId(),
            fooCompany.getId(),
            "Software Engineer",
            "Some description",
            START_DATE,
            END_DATE);

    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(fooUserActivated.getId(), result.getUserId());
    assertEquals(fooCompany.getId(), result.getCompanyId());
    assertEquals("Software Engineer", result.getRoleTitle());
    assertEquals("Some description", result.getRoleDescription());
    assertEquals(START_DATE, result.getStartDate());
    assertEquals(END_DATE, result.getEndDate());
  }

  @Test
  public void createNewUserExperience_whenEndDateIsNull_expectReturnSavedModel() {
    setUpCompany();

    UserExperienceModel result =
        this.userService.createNewUserExperience(
            fooUserActivated.getId(),
            fooCompany.getId(),
            "Software Engineer",
            "Some description",
            START_DATE,
            null);

    assertNotNull(result);
    assertNull(result.getEndDate());
  }

  @Test
  public void createNewUserExperience_whenCompanyNotFound_expectCompanyNotFoundException() {
    this.companyRepository.deleteAll();

    assertThrows(
        CompanyNotFoundException.class,
        () ->
            this.userService.createNewUserExperience(
                fooUserActivated.getId(),
                "nonExistentCompanyId",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void createNewUserExperience_whenRepositoryThrows_expectUserServiceFailException() {
    setUpCompany();

    UserService serviceWithMockRepo =
        new UserService(
            this.userRepository,
            this.passwordEncoder,
            this.mockUserExperienceRepository, // ← inject mock to simulate failure
            this.companyRepository);

    when(mockUserExperienceRepository.save(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () ->
            serviceWithMockRepo.createNewUserExperience(
                fooUserActivated.getId(),
                fooCompany.getId(),
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void createNewUserExperience_whenCalled_expectPersistedInDatabase() {
    setUpCompany();

    this.userService.createNewUserExperience(
        fooUserActivated.getId(),
        fooCompany.getId(),
        "Software Engineer",
        "Some description",
        START_DATE,
        END_DATE);

    List<UserExperienceModel> saved =
        this.userExperienceRepository.findAllByUserId(fooUserActivated.getId());
    assertThat(saved).hasSize(1);
    assertEquals(fooCompany.getId(), saved.get(0).getCompanyId());
  }

  @Test
  public void createNewUserExperience_whenCompanyIdIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.createNewUserExperience(
                fooUserActivated.getId(),
                null, // companyId null
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void createNewUserExperience_whenRoleTitleIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.createNewUserExperience(
                fooUserActivated.getId(),
                "someCompanyId",
                null, // roleTitle null
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void createNewUserExperience_whenRoleDescriptionIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.createNewUserExperience(
                fooUserActivated.getId(),
                "someCompanyId",
                "Software Engineer",
                null, // roleDescription null
                START_DATE,
                END_DATE));
  }

  @Test
  public void createNewUserExperience_whenStartDateIsNull_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.createNewUserExperience(
                fooUserActivated.getId(),
                "someCompanyId",
                "Software Engineer",
                "Some description",
                null, // startDate null
                END_DATE));
  }

  @Test
  public void
      createNewUserExperience_whenEndDateIsBeforeStartDate_expectUserServiceFailException() {
    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.createNewUserExperience(
                fooUserActivated.getId(),
                "someCompanyId",
                "Software Engineer",
                "Some description",
                START_DATE,
                START_DATE.minusDays(1))); // endDate before startDate
  }

  @Test
  public void createNewUserExperience_whenEndDateIsAfterStartDate_expectSuccess() {
    setUpCompany();

    UserExperienceModel result =
        this.userService.createNewUserExperience(
            fooUserActivated.getId(),
            fooCompany.getId(),
            "Software Engineer",
            "Some description",
            START_DATE,
            END_DATE); // endDate after startDate — valid

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
  }

  @Test
  public void createNewUserExperience_whenEndDateIsNull_expectSuccess() {
    setUpCompany();

    UserExperienceModel result =
        this.userService.createNewUserExperience(
            fooUserActivated.getId(),
            fooCompany.getId(),
            "Software Engineer",
            "Some description",
            START_DATE,
            null); // null endDate is valid — current job

    assertThat(result).isNotNull();
    assertThat(result.getEndDate()).isNull();
  }

  @Test
  public void deleteUserExperience_whenValidArgs_expectExperienceDeleted() {
    setUpExperiences();

    this.userService.deleteUserExperience(fooExperience1.getId(), fooUserActivated.getId());

    List<UserExperienceModel> remaining =
        this.userExperienceRepository.findAllByUserId(fooUserActivated.getId());
    assertThat(remaining).hasSize(1);
    assertEquals(fooExperience2.getId(), remaining.get(0).getId());
  }

  @Test
  public void deleteUserExperience_whenExperienceNotFound_expectExperienceNotFoundException() {
    assertThrows(
        ExperienceNotFoundException.class,
        () ->
            this.userService.deleteUserExperience(
                "nonExistentExperienceId", fooUserActivated.getId()));
  }

  @Test
  public void deleteUserExperience_whenExperienceNotOwnedByUser_expectExperienceNotOwnException() {
    setUpExperiences();

    assertThrows(
        ExperienceNotOwnedException.class,
        () ->
            this.userService.deleteUserExperience(
                fooExperience1.getId(), "anotherUserId")); // different user trying to delete
  }

  @Test
  public void deleteUserExperience_whenRepositoryThrows_expectUserServiceFailException() {
    setUpExperiences();

    UserService serviceWithMockRepo =
        new UserService(
            this.userRepository,
            this.passwordEncoder,
            this.mockUserExperienceRepository,
            this.companyRepository);

    when(mockUserExperienceRepository.findById(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () ->
            serviceWithMockRepo.deleteUserExperience(
                fooExperience1.getId(), fooUserActivated.getId()));
  }

  @Test
  public void deleteUserExperience_whenDeleted_expectNotFoundInDatabase() {
    setUpExperiences();

    this.userService.deleteUserExperience(fooExperience1.getId(), fooUserActivated.getId());

    UserExperienceModel deleted =
        this.userExperienceRepository.findUserExperienceModelById(fooExperience1.getId());
    assertNull(deleted);
  }

  @Test
  public void deleteUserExperience_whenDeleteOne_expectOtherExperiencesUnaffected() {
    setUpExperiences();

    this.userService.deleteUserExperience(fooExperience1.getId(), fooUserActivated.getId());

    UserExperienceModel remaining =
        this.userExperienceRepository.findUserExperienceModelById(fooExperience2.getId());
    assertNotNull(remaining);
    assertEquals(fooExperience2.getId(), remaining.getId());
  }

  @Test
  public void updateUserExperience_whenValidArgs_expectAllFieldsUpdated() {
    setUpExperiences();
    setUpCompany();
    LocalDate newStartDate = LocalDate.now().minusYears(2);
    LocalDate newEndDate = LocalDate.now().minusMonths(1);

    this.userService.updateUserExperience(
        fooExperience1.getId(),
        fooUserActivated.getId(),
        fooCompany.getId(),
        "Senior Software Engineer",
        "Updated description",
        newStartDate,
        newEndDate);

    UserExperienceModel updated =
        this.userExperienceRepository.findUserExperienceModelById(fooExperience1.getId());
    assertEquals(fooCompany.getId(), updated.getCompanyId());
    assertEquals("Senior Software Engineer", updated.getRoleTitle());
    assertEquals("Updated description", updated.getRoleDescription());
    assertEquals(newStartDate, updated.getStartDate());
    assertEquals(newEndDate, updated.getEndDate());
  }

  @Test
  public void updateUserExperience_whenEndDateIsNull_expectEndDateCleared() {
    setUpExperiences();
    setUpCompany();

    this.userService.updateUserExperience(
        fooExperience1.getId(),
        fooUserActivated.getId(),
        fooCompany.getId(),
        "Software Engineer",
        "Some description",
        START_DATE,
        null); // mark as current job

    UserExperienceModel updated =
        this.userExperienceRepository.findUserExperienceModelById(fooExperience1.getId());
    assertNull(updated.getEndDate());
  }

  @Test
  public void updateUserExperience_whenCompanyIdIsNull_expectUserServiceFailException() {
    setUpExperiences();

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.updateUserExperience(
                fooExperience1.getId(),
                fooUserActivated.getId(),
                null, // companyId null
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenRoleTitleIsNull_expectUserServiceFailException() {
    setUpExperiences();

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.updateUserExperience(
                fooExperience1.getId(),
                fooUserActivated.getId(),
                "someCompanyId",
                null, // roleTitle null
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenRoleDescriptionIsNull_expectUserServiceFailException() {
    setUpExperiences();

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.updateUserExperience(
                fooExperience1.getId(),
                fooUserActivated.getId(),
                "someCompanyId",
                "Software Engineer",
                null, // roleDescription null
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenStartDateIsNull_expectUserServiceFailException() {
    setUpExperiences();

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.updateUserExperience(
                fooExperience1.getId(),
                fooUserActivated.getId(),
                "someCompanyId",
                "Software Engineer",
                "Some description",
                null, // startDate null
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenStartDateAfterEndDate_expectUserServiceFailException() {
    setUpExperiences();

    assertThrows(
        UserServiceFailException.class,
        () ->
            this.userService.updateUserExperience(
                fooExperience1.getId(),
                fooUserActivated.getId(),
                "someCompanyId",
                "Software Engineer",
                "Some description",
                START_DATE,
                START_DATE.minusDays(1)));
  }

  @Test
  public void updateUserExperience_whenExperienceNotFound_expectExperienceNotFoundException() {
    assertThrows(
        ExperienceNotFoundException.class,
        () ->
            this.userService.updateUserExperience(
                "nonExistentExperienceId",
                fooUserActivated.getId(),
                "someCompanyId",
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenExperienceNotOwnedByUser_expectExperienceNotOwnException() {
    setUpExperiences();
    setUpCompany();

    assertThrows(
        ExperienceNotOwnedException.class,
        () ->
            this.userService.updateUserExperience(
                fooExperience1.getId(),
                "anotherUserId", // different user
                fooCompany.getId(),
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenCompanyNotFound_expectCompanyNotFoundException() {
    setUpExperiences();

    assertThrows(
        CompanyNotFoundException.class,
        () ->
            this.userService.updateUserExperience(
                fooExperience1.getId(),
                fooUserActivated.getId(),
                "nonExistentCompanyId", // company doesn't exist
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenRepositoryThrows_expectUserServiceFailException() {
    setUpExperiences();
    setUpCompany();

    UserService serviceWithMockRepo =
        new UserService(
            this.userRepository,
            this.passwordEncoder,
            this.mockUserExperienceRepository,
            this.companyRepository);

    when(mockUserExperienceRepository.findById(any())).thenThrow(new RuntimeException());

    assertThrows(
        UserServiceFailException.class,
        () ->
            serviceWithMockRepo.updateUserExperience(
                fooExperience1.getId(),
                fooUserActivated.getId(),
                fooCompany.getId(),
                "Software Engineer",
                "Some description",
                START_DATE,
                END_DATE));
  }

  @Test
  public void updateUserExperience_whenUpdated_expectPersistedInDatabase() {
    setUpExperiences();
    setUpCompany();

    this.userService.updateUserExperience(
        fooExperience1.getId(),
        fooUserActivated.getId(),
        fooCompany.getId(),
        "Senior Software Engineer",
        "Updated description",
        START_DATE,
        END_DATE);

    UserExperienceModel updated =
        this.userExperienceRepository.findUserExperienceModelById(fooExperience1.getId());
    assertThat(updated).isNotNull();
    assertThat(updated.getRoleTitle()).isEqualTo("Senior Software Engineer");
  }
}
