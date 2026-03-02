package com.backend.coapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import java.time.LocalDate;
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

/** Unit tests for ApplicationModel and its repository. */
@SpringBootTest
@Testcontainers
public class ApplicationRepositoryTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired ApplicationRepository repository;

  ApplicationModel app1;
  ApplicationModel app2;
  ApplicationModel app3;

  @BeforeEach
  public void setUp() {
    repository.deleteAll();

    app1 = new ApplicationModel();
    app1.setUserId("user1");
    app1.setCompanyId("companyA");
    app1.setJobTitle("Software Engineer");
    app1.setStatus(ApplicationStatus.APPLIED);
    app1.setApplicationDeadline(LocalDate.now().plusDays(10));
    app1.setNumPositions(1);

    app2 = new ApplicationModel();
    app2.setUserId("user1"); // same user as app1
    app2.setCompanyId("companyB");
    app2.setJobTitle("Product Manager");
    app2.setStatus(ApplicationStatus.INTERVIEWING);
    app2.setApplicationDeadline(LocalDate.now().plusDays(5));
    app2.setNumPositions(1);

    app3 = new ApplicationModel();
    app3.setUserId("user2"); // different user
    app3.setCompanyId("companyA"); // same company as app1
    app3.setJobTitle("Data Scientist");
    app3.setStatus(ApplicationStatus.APPLIED);
    app3.setApplicationDeadline(LocalDate.now().plusDays(20));
    app3.setNumPositions(1);

    app1 = repository.save(app1);
    app2 = repository.save(app2);
    app3 = repository.save(app3);
  }

  @Test
  public void saveNew_expectIdGenerated() {
    assertThat(app1.getId()).isNotNull();
    assertThat(app2.getId()).isNotNull();
  }

  @Test
  public void findByUserId_whenUserHasTwoApps_expectReturnTwo() {
    List<ApplicationModel> found = repository.findByUserId("user1");

    assertThat(found).hasSize(2);
    assertThat(found)
        .extracting("jobTitle")
        .containsExactlyInAnyOrder("Software Engineer", "Product Manager");
  }

  @Test
  public void findByUserId_whenUserHasNoApps_expectEmpty() {
    List<ApplicationModel> found = repository.findByUserId("nonexistentUser");
    assertThat(found).isEmpty();
  }

  @Test
  public void findByCompanyId_whenCompanyHasTwoApps_expectReturnTwo() {
    List<ApplicationModel> found = repository.findByCompanyId("companyA");

    assertThat(found).hasSize(2); // app1 and app3
    assertThat(found).extracting("userId").containsExactlyInAnyOrder("user1", "user2");
  }

  @Test
  public void findByUserIdAndStatus_whenMatchExists_expectReturnOne() {
    List<ApplicationModel> found =
        repository.findByUserIdAndStatus("user1", ApplicationStatus.INTERVIEWING);

    assertThat(found).hasSize(1);
    assertThat(found.get(0).getJobTitle()).isEqualTo("Product Manager");
  }

  @Test
  public void findByUserIdAndStatus_whenNoMatch_expectEmpty() {
    List<ApplicationModel> found =
        repository.findByUserIdAndStatus("user1", ApplicationStatus.REJECTED);

    assertThat(found).isEmpty();
  }

  @Test
  public void deleteById_expectApplicationDeleted() {
    repository.deleteById(app1.getId());

    Optional<ApplicationModel> deleted = repository.findById(app1.getId());
    assertThat(deleted).isEmpty();

    // make sure others still exist
    assertThat(repository.count()).isEqualTo(2);
  }

  @Test
  public void updateStatus_expectStatusChanged() {
    app1.setStatus(ApplicationStatus.ACCEPTED);
    repository.save(app1);

    Optional<ApplicationModel> updated = repository.findById(app1.getId());
    assertThat(updated).isPresent();
    assertThat(updated.get().getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
  }

  @Test
  public void existsByUserIdAndCompanyIdAndJobTitle_whenMatchExists_expectTrue() {
    boolean exists =
        repository.existsByUserIdAndCompanyIdAndJobTitle("user1", "companyA", "Software Engineer");

    assertThat(exists).isTrue();
  }

  @Test
  public void existsByUserIdAndCompanyIdAndJobTitle_whenPartialMatch_expectFalse() {
    boolean wrongTitle =
        repository.existsByUserIdAndCompanyIdAndJobTitle("user1", "companyA", "Backend Developer");

    boolean wrongUser =
        repository.existsByUserIdAndCompanyIdAndJobTitle(
            "user999", "companyA", "Software Engineer");

    boolean wrongCompany =
        repository.existsByUserIdAndCompanyIdAndJobTitle("user1", "companyZ", "Software Engineer");

    assertThat(wrongTitle).isFalse();
    assertThat(wrongUser).isFalse();
    assertThat(wrongCompany).isFalse();
  }

  @Test
  public void existsByUserIdAndCompanyIdAndJobTitle_whenCaseDoesNotMatch_expectFalse() {
    boolean exists =
        repository.existsByUserIdAndCompanyIdAndJobTitle("USER1", "companyA", "Software Engineer");

    assertThat(exists).isFalse();
  }

  @Test
  public void existsByUserIdAndCompanyIdAndJobTitle_afterDelete_expectFalse() {
    repository.delete(app1);

    boolean exists =
        repository.existsByUserIdAndCompanyIdAndJobTitle("user1", "companyA", "Software Engineer");

    assertThat(exists).isFalse();
  }
}
