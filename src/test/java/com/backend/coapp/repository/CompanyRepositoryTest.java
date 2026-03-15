package com.backend.coapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.backend.coapp.model.document.CompanyModel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Unit tests for CompanyModel and its repository. */
@SpringBootTest
@Testcontainers
class CompanyRepositoryTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired CompanyRepository repository;

  CompanyModel niche;
  CompanyModel varian;
  CompanyModel payworks;

  /** Runs before each test to reset the data. */
  @BeforeEach
  void setUp() {
    repository.deleteAll();
    niche = repository.save(new CompanyModel("Niche", "Winnipeg", "https://niche.com"));
    varian = repository.save(new CompanyModel("Varian", "Winnipeg", "https://varian.com"));
    payworks = repository.save(new CompanyModel("Payworks", "Winnipeg", "https://payworks.com"));
  }

  @Test
  void saveNew_whenNoIdDefined_expectSetsIdOnSave() {
    CompanyModel company =
        repository.save(new CompanyModel("Priceline", "Winnipeg", "https://priceline.com"));
    assertThat(company.getId()).isNotNull();
  }

  @Test
  void saveNew_expectAvgRatingDefaultsToZero() {
    CompanyModel company =
        repository.save(new CompanyModel("Priceline", "Winnipeg", "https://priceline.com"));
    assertThat(company.getAvgRating()).isEqualTo(0.0);
  }

  @Test
  void findById_expectReturnNiche() {
    Optional<CompanyModel> found = repository.findById(niche.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getCompanyName()).isEqualTo("Niche");
    assertThat(found.get().getCompanyNameLower()).isEqualTo("niche");
    assertThat(found.get().getLocation()).isEqualTo("Winnipeg");
    assertThat(found.get().getWebsite()).isEqualTo("https://niche.com");
    assertThat(found.get().getAvgRating()).isEqualTo(0.0);
  }

  @Test
  void findByCompanyNameLower_whenFindNiche_expectNicheCompanyModel() {
    Optional<CompanyModel> found = repository.findByCompanyNameLower("niche");
    assertThat(found).isPresent();
    assertThat(found.get().getCompanyName()).isEqualTo("Niche");
    assertThat(found.get().getLocation()).isEqualTo("Winnipeg");
    assertThat(found.get().getWebsite()).isEqualTo("https://niche.com");
  }

  @Test
  void findByCompanyNameLower_caseInsensitive_expectFindsCompany() {
    Optional<CompanyModel> found = repository.findByCompanyNameLower("niche");
    assertThat(found).isPresent();
    assertThat(found.get().getCompanyName()).isEqualTo("Niche");
  }

  @Test
  void findByCompanyNameLower_whenNonExistent_expectEmpty() {
    Optional<CompanyModel> found = repository.findByCompanyNameLower("nonexistent");
    assertThat(found).isEmpty();
  }

  @Test
  void existsByCompanyNameLower_whenExists_expectTrue() {
    boolean exists = repository.existsByCompanyNameLower("niche");
    assertTrue(exists);
  }

  @Test
  void existsByCompanyNameLower_whenNotExists_expectFalse() {
    boolean exists = repository.existsByCompanyNameLower("nonexistent");
    assertThat(exists).isFalse();
  }

  @Test
  void findsAllCompanies_expectReturn3Companies() {
    List<CompanyModel> companies = repository.findAll();
    assertThat(companies)
        .hasSize(3)
        .extracting("companyName")
        .contains("Niche", "Varian", "Payworks");
    assertThat(companies)
        .hasSize(3)
        .extracting("companyNameLower")
        .contains("niche", "varian", "payworks");
    assertThat(companies)
        .hasSize(3)
        .extracting("location")
        .contains("Winnipeg", "Winnipeg", "Winnipeg");
  }

  @Test
  void deleteById_whenDeletePayworks_expectDeletePayworks() {
    repository.deleteById(payworks.getId());
    List<CompanyModel> companies = repository.findAll();
    assertThat(companies).hasSize(2);
    Optional<CompanyModel> deleted = repository.findById(payworks.getId());
    assertThat(deleted).isEmpty();
  }

  @Test
  void updatesCompany_whenUpdateNicheLocation_expectLocationUpdate() {
    niche.setLocation("Toronto");
    repository.save(niche);
    Optional<CompanyModel> updated = repository.findById(niche.getId());
    assertThat(updated).isPresent();
    assertThat(updated.get().getLocation()).isEqualTo("Toronto");
    assertThat(updated.get().getCompanyName()).isEqualTo("Niche");
  }

  @Test
  void updatesCompany_whenUpdateAvgRating_expectRatingUpdate() {
    niche.setAvgRating(4.5);
    repository.save(niche);
    Optional<CompanyModel> updated = repository.findById(niche.getId());
    assertThat(updated).isPresent();
    assertThat(updated.get().getAvgRating()).isEqualTo(4.5);
  }

  @Test
  void findById_whenCompanyNotExist_expectEmpty() {
    Optional<CompanyModel> notFound = repository.findById("fakeId");
    assertThat(notFound).isEmpty();
  }

  @Test
  void save_whenDuplicateCompanyNameLower_expectUniqueConstraintViolation() {
    // verifies that the unique index on companyNameLower works
    assertThat(repository.findAll()).hasSize(3);

    // Try to save a company with duplicate name
    Exception exception =
        assertThrows(
            Exception.class,
            () ->
                repository.save(
                    new CompanyModel("NICHE", "Toronto", "https://niche-different.com")));

    assertThat(exception.getMessage()).containsAnyOf("duplicate", "unique");
  }

  @Test
  void save_whenCompanyNameWithWhitespace_expectTrimmedAndSaved() {
    CompanyModel company =
        repository.save(new CompanyModel("  Priceline Inc  ", "Winnipeg", "https://priceline.com"));

    assertThat(company.getId()).isNotNull();
    assertThat(company.getCompanyName()).isEqualTo("Priceline Inc");
    assertThat(company.getCompanyNameLower()).isEqualTo("priceline inc");
  }

  @Test
  void save_whenDuplicateNameDifferentCase_expectConstraintViolation() {
    repository.save(new CompanyModel("TestCompany", "Winnipeg", "https://testcompany.com"));

    // Try to save duplicate with different case
    Exception exception =
        assertThrows(
            Exception.class,
            () ->
                repository.save(
                    new CompanyModel(
                        "TESTCOMPANY", "Toronto", "https://testcompany-different.com")));

    assertThat(exception.getMessage()).containsAnyOf("duplicate", "unique");
  }

  @Test
  void save_whenSameCompanyNameWithSpaces_expectDuplicateDetection() {
    repository.save(new CompanyModel("Priceline Inc", "Winnipeg", "https://bold.com"));

    // Try to save duplicate with whitespace and different case
    Exception exception =
        assertThrows(
            Exception.class,
            () ->
                repository.save(
                    new CompanyModel("  PRICELINE INC  ", "Toronto", "https://bold-toronto.com")));

    assertThat(exception.getMessage()).containsAnyOf("duplicate", "unique");
  }

  @Test
  void updateCompanyName_expectBothFieldsUpdate() {
    niche.setCompanyName("Niche Updated");
    CompanyModel updated = repository.save(niche);

    assertThat(updated.getCompanyName()).isEqualTo("Niche Updated");
    assertThat(updated.getCompanyNameLower()).isEqualTo("niche updated");
  }

  @Test
  void updateCompanyName_whenChangingCase_expectLowerCaseUpdates() {
    niche.setCompanyName("NICHE");
    CompanyModel updated = repository.save(niche);

    assertThat(updated.getCompanyName()).isEqualTo("NICHE");
    assertThat(updated.getCompanyNameLower()).isEqualTo("niche");
  }

  @Test
  void count_expectReturnsCorrectCount() {
    long count = repository.count();
    assertThat(count).isEqualTo(3);
  }

  @Test
  void count_whenEmpty_expectReturnsZero() {
    repository.deleteAll();
    long count = repository.count();
    assertThat(count).isEqualTo(0);
  }

  @Test
  void deleteAll_expectEmptyRepository() {
    repository.deleteAll();
    List<CompanyModel> companies = repository.findAll();
    assertThat(companies).isEmpty();
    assertThat(repository.count()).isEqualTo(0);
  }

  @Test
  void findByCompanyNameLower_whenMixedCase_expectFindsRegardless() {
    repository.save(new CompanyModel("MixedCaseCompany", "Winnipeg", "https://test.com"));

    Optional<CompanyModel> found = repository.findByCompanyNameLower("mixedcasecompany");
    assertThat(found).isPresent();
    assertThat(found.get().getCompanyName()).isEqualTo("MixedCaseCompany");
  }

  @Test
  void existsByCompanyNameLower_afterDelete_expectFalse() {
    repository.delete(niche);
    boolean exists = repository.existsByCompanyNameLower("niche");
    assertThat(exists).isFalse();
  }

  @Test
  void findAll_whenEmpty_expectEmptyList() {
    repository.deleteAll();
    List<CompanyModel> companies = repository.findAll();
    assertThat(companies).isEmpty();
  }

  @Test
  void save_expectAllFieldsPersist() {
    CompanyModel company = new CompanyModel("Test Test", "Winnipeg", "https://test.com");
    company.setAvgRating(3.75);

    CompanyModel saved = repository.save(company);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCompanyName()).isEqualTo("Test Test");
    assertThat(saved.getCompanyNameLower()).isEqualTo("test test");
    assertThat(saved.getLocation()).isEqualTo("Winnipeg");
    assertThat(saved.getWebsite()).isEqualTo("https://test.com");
    assertThat(saved.getAvgRating()).isEqualTo(3.75);
  }

  @Test
  void updateMultipleFields_expectAllFieldsUpdate() {
    niche.setCompanyName("Niche Inc");
    niche.setLocation("Vancouver");
    niche.setWebsite("https://niche-new.com");
    niche.setAvgRating(4.2);

    CompanyModel updated = repository.save(niche);

    assertThat(updated.getCompanyName()).isEqualTo("Niche Inc");
    assertThat(updated.getCompanyNameLower()).isEqualTo("niche inc");
    assertThat(updated.getLocation()).isEqualTo("Vancouver");
    assertThat(updated.getWebsite()).isEqualTo("https://niche-new.com");
    assertThat(updated.getAvgRating()).isEqualTo(4.2);
  }
}
