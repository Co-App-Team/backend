package com.backend.coapp.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.document.UserExperienceModel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class UserExperienceRepositoryTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired UserExperienceRepository repository;

  private final LocalDate START_DATE = LocalDate.now().minusYears(1);
  private final LocalDate END_DATE = LocalDate.now();

  UserExperienceModel fooExp1;
  UserExperienceModel fooExp2;
  UserExperienceModel woofExp1;

  @BeforeEach
  public void setUp() {
    repository.deleteAll();
    fooExp1 =
        repository.save(
            new UserExperienceModel(
                "fooId",
                "companyA",
                "Software Engineer",
                "Building co-app application",
                START_DATE,
                END_DATE));
    fooExp2 =
        repository.save(
            new UserExperienceModel(
                "fooId",
                "companyB",
                "Tech Lead",
                "leading development of co-app application",
                START_DATE,
                null));
    woofExp1 =
        repository.save(
            new UserExperienceModel(
                "woofId",
                "companyC",
                "Product Manager",
                "Manage Co-App application team",
                START_DATE,
                END_DATE));
  }

  @Test
  public void save_whenNoIdDefined_expectSetsIdOnSave() {
    UserExperienceModel model =
        repository.save(
            new UserExperienceModel(
                null,
                "newUserId",
                "companyD",
                "Designer",
                "design something cool",
                START_DATE,
                END_DATE));

    assertNotNull(model.getId());
  }

  @Test
  public void findAllByUserId_whenUserHasMultipleExperiences_expectReturnAll() {
    List<UserExperienceModel> results = repository.findAllByUserId(fooExp1.getUserId());

    assertThat(results)
        .hasSize(2)
        .extracting("companyId")
        .contains(fooExp1.getCompanyId(), fooExp2.getCompanyId());
  }

  @Test
  public void findAllByUserId_whenUserHasOneExperience_expectReturnOne() {
    List<UserExperienceModel> results = repository.findAllByUserId(woofExp1.getUserId());

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getCompanyId()).isEqualTo(woofExp1.getCompanyId());
    assertThat(results.get(0).getRoleDescription()).isEqualTo(woofExp1.getRoleDescription());
  }

  @Test
  public void findAllByUserId_whenUserDoesNotExist_expectReturnEmptyList() {
    List<UserExperienceModel> results = repository.findAllByUserId("nonExistentId");

    assertThat(results).isEmpty();
  }

  @Test
  public void findAllByUserId_whenEndDateIsNull_expectReturnCorrectly() {
    List<UserExperienceModel> results = repository.findAllByUserId(fooExp1.getUserId());

    assertThat(results)
        .filteredOn(exp -> exp.getEndDate() == null)
        .hasSize(1)
        .extracting("companyId")
        .contains(fooExp2.getCompanyId());
  }

  @Test
  public void findAll_expectReturnAllExperiences() {
    List<UserExperienceModel> all = repository.findAll();

    assertThat(all)
        .hasSize(3)
        .extracting("userId")
        .contains(fooExp1.getUserId(), fooExp2.getUserId(), woofExp1.getUserId());
  }

  @Test
  public void save_whenUpdateRoleDescription_expectRoleDescriptionUpdated() {
    fooExp1.setRoleDescription("Developing new application");
    repository.save(fooExp1);

    UserExperienceModel updated = repository.findById(fooExp1.getId()).orElse(null);
    assertNotNull(updated);
    assertEquals("Developing new application", updated.getRoleDescription());
  }

  @Test
  public void save_whenUpdateStartDate_expectStartDateUpdated() {
    LocalDate newStartDate = LocalDate.now().minusYears(2);
    fooExp1.setStartDate(newStartDate);
    repository.save(fooExp1);

    UserExperienceModel updated = repository.findById(fooExp1.getId()).orElse(null);
    assertNotNull(updated);
    assertEquals(newStartDate, updated.getStartDate());
  }

  @Test
  public void save_whenUpdateEndDate_expectEndDateUpdated() {
    LocalDate newEndDate = LocalDate.now().plusMonths(6);
    fooExp1.setEndDate(newEndDate);
    repository.save(fooExp1);

    UserExperienceModel updated = repository.findById(fooExp1.getId()).orElse(null);
    assertNotNull(updated);
    assertEquals(newEndDate, updated.getEndDate());
  }

  @Test
  public void save_whenClearEndDate_expectEndDateIsNull() {
    fooExp1.setEndDate(null);
    repository.save(fooExp1);

    UserExperienceModel updated = repository.findById(fooExp1.getId()).orElse(null);
    assertNotNull(updated);
    assertNull(updated.getEndDate());
  }

  @Test
  public void findUserExperienceModelById_whenExists_expectReturnCorrectModel() {
    UserExperienceModel found = repository.findUserExperienceModelById(fooExp1.getId());

    assertNotNull(found);
    assertEquals(fooExp1.getId(), found.getId());
    assertEquals(fooExp1.getUserId(), found.getUserId());
    assertEquals(fooExp1.getCompanyId(), found.getCompanyId());
    assertEquals(fooExp1.getRoleDescription(), found.getRoleDescription());
  }

  @Test
  public void findUserExperienceModelById_whenNotExists_expectReturnNull() {
    UserExperienceModel found = repository.findUserExperienceModelById("nonExistentId");

    assertThat(found).isNull();
  }

  @Test
  public void findUserExperienceModelById_whenMultipleExperiencesExist_expectReturnCorrectOne() {
    UserExperienceModel found = repository.findUserExperienceModelById(fooExp2.getId());

    assertNotNull(found);
    assertEquals(fooExp2.getId(), found.getId());
    assertNotEquals(fooExp1.getId(), found.getId());
  }

  @Test
  public void deleteUserExperienceModelById_whenExists_expectDeleted() {
    repository.deleteUserExperienceModelById(fooExp1.getId());

    UserExperienceModel deleted = repository.findUserExperienceModelById(fooExp1.getId());
    assertThat(deleted).isNull();
  }

  @Test
  public void deleteUserExperienceModelById_whenNotExists_expectNoException() {
    assertDoesNotThrow(() -> repository.deleteUserExperienceModelById("nonExistentId"));
  }

  @Test
  public void deleteUserExperienceModelById_whenDeleteOne_expectOthersUnaffected() {
    repository.deleteUserExperienceModelById(fooExp1.getId());

    List<UserExperienceModel> fooRemaining = repository.findAllByUserId(fooExp1.getUserId());
    assertThat(fooRemaining).hasSize(1);
    assertThat(fooRemaining.get(0).getId()).isEqualTo(fooExp2.getId());
  }

  @Test
  public void save_whenUpdateRoleTitle_expectRoleTitleUpdated() {
    fooExp1.setRoleTitle("Senior Software Engineer");
    repository.save(fooExp1);

    UserExperienceModel updated = repository.findUserExperienceModelById(fooExp1.getId());
    assertThat(updated).isNotNull();
    assertThat(updated.getRoleTitle()).isEqualTo("Senior Software Engineer");
  }

  @Test
  public void findAllByUserId_whenUserHasMultipleExperiences_expectCorrectRoleTitles() {
    List<UserExperienceModel> results = repository.findAllByUserId(fooExp1.getUserId());

    assertThat(results)
        .extracting("roleTitle")
        .contains(fooExp1.getRoleTitle(), fooExp2.getRoleTitle());
  }
}
