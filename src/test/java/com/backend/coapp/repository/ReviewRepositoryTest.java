package com.backend.coapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.util.ReviewConstants;
import com.backend.coapp.util.WorkTermValidator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Unit tests for ReviewModel repository */
@SpringBootTest
@Testcontainers
class ReviewRepositoryTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired ReviewRepository repository;

  ReviewModel review1;
  ReviewModel review2;
  ReviewModel review3;

  /** reset data before each test */
  @BeforeEach
  void setUp() {
    repository.deleteAll();

    review1 =
        repository.save(
            new ReviewModel(
                "company1",
                "user1",
                "Eric Hodgson",
                ReviewConstants.MAX_RATING,
                "Great experience!",
                "Software Developer",
                "Summer",
                WorkTermValidator.getMaxYear()));

    review2 =
        repository.save(
            new ReviewModel(
                "company1",
                "user2",
                "Not Eric",
                ReviewConstants.MAX_RATING - 1,
                "Good company",
                "Engineer",
                "Fall",
                WorkTermValidator.getMaxYear() - 2));

    review3 =
        repository.save(
            new ReviewModel(
                "company2",
                "user1",
                "Eric Hodgson",
                ReviewConstants.MAX_RATING - 2,
                "Average",
                "Developer",
                "Winter",
                WorkTermValidator.getMaxYear() - 2));
  }

  @Test
  void saveNew_whenNoIdDefined_expectSetsIdOnSave() {
    ReviewModel review =
        repository.save(
            new ReviewModel(
                "company3",
                "user3",
                "Evil Eric",
                ReviewConstants.MAX_RATING,
                "Excellent!",
                "Developer",
                "Summer",
                WorkTermValidator.getMaxYear() - 2));
    assertThat(review.getId()).isNotNull();
  }

  @Test
  void findById_expectReturnReview() {
    Optional<ReviewModel> found = repository.findById(review1.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getCompanyId()).isEqualTo("company1");
    assertThat(found.get().getUserId()).isEqualTo("user1");
    assertThat(found.get().getAuthorName()).isEqualTo("Eric Hodgson");
    assertThat(found.get().getRating()).isEqualTo(ReviewConstants.MAX_RATING);
    assertThat(found.get().getComment()).isEqualTo("Great experience!");
    assertThat(found.get().getJobTitle()).isEqualTo("Software Developer");
    assertThat(found.get().getWorkTermSeason()).isEqualTo("Summer");
    assertThat(found.get().getWorkTermYear()).isEqualTo(WorkTermValidator.getMaxYear());
  }

  @Test
  void findByCompanyId_expectReturnReviewsForCompany() {
    List<ReviewModel> reviews = repository.findByCompanyId("company1");
    assertThat(reviews).hasSize(2);
    assertThat(reviews).extracting("companyId").containsOnly("company1");
    assertThat(reviews).extracting("authorName").contains("Eric Hodgson", "Not Eric");
  }

  @Test
  void findByCompanyId_withPagination_expectPagedResults() {
    Page<ReviewModel> page = repository.findByCompanyId("company1", PageRequest.of(0, 1));
    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  @Test
  void findByCompanyId_whenNoReviews_expectEmptyList() {
    List<ReviewModel> reviews = repository.findByCompanyId("fakeId");
    assertThat(reviews).isEmpty();
  }

  @Test
  void findByUserIdAndCompanyId_expectReturnReview() {
    Optional<ReviewModel> found = repository.findByUserIdAndCompanyId("user1", "company1");
    assertThat(found).isPresent();
    assertThat(found.get().getUserId()).isEqualTo("user1");
    assertThat(found.get().getCompanyId()).isEqualTo("company1");
    assertThat(found.get().getAuthorName()).isEqualTo("Eric Hodgson");
  }

  @Test
  void findByUserIdAndCompanyId_whenNotExists_expectEmpty() {
    Optional<ReviewModel> found = repository.findByUserIdAndCompanyId("user3", "company1");
    assertThat(found).isEmpty();
  }

  @Test
  void existsByUserIdAndCompanyId_whenExists_expectTrue() {
    boolean exists = repository.existsByUserIdAndCompanyId("user1", "company1");
    assertThat(exists).isTrue();
  }

  @Test
  void existsByUserIdAndCompanyId_whenNotExists_expectFalse() {
    boolean exists = repository.existsByUserIdAndCompanyId("user3", "company1");
    assertThat(exists).isFalse();
  }

  @Test
  void findByUserId_expectReturnAllReviewsByUser() {
    List<ReviewModel> reviews = repository.findByUserId("user1");
    assertThat(reviews).hasSize(2);
    assertThat(reviews).extracting("userId").containsOnly("user1");
    assertThat(reviews).extracting("companyId").contains("company1", "company2");
  }

  @Test
  void findByUserId_whenNoReviews_expectEmptyList() {
    List<ReviewModel> reviews = repository.findByUserId("userFake");
    assertThat(reviews).isEmpty();
  }

  @Test
  void countByCompanyId_expectCorrectCount() {
    long count = repository.countByCompanyId("company1");
    assertThat(count).isEqualTo(2);
  }

  @Test
  void countByCompanyId_whenNoReviews_expectZero() {
    long count = repository.countByCompanyId("companyFake");
    assertThat(count).isEqualTo(0);
  }

  @Test
  void findsAllReviews_expectReturn3Reviews() {
    List<ReviewModel> reviews = repository.findAll();
    assertThat(reviews).hasSize(3);
    assertThat(reviews).extracting("authorName").contains("Eric Hodgson", "Not Eric");
  }

  @Test
  void deleteById_expectDeleteReview() {
    repository.deleteById(review1.getId());
    List<ReviewModel> reviews = repository.findAll();
    assertThat(reviews).hasSize(2);
    Optional<ReviewModel> deleted = repository.findById(review1.getId());
    assertThat(deleted).isEmpty();
  }

  @Test
  void updatesReview_whenUpdateRating_expectRatingUpdate() {
    review1.setRating(ReviewConstants.MAX_RATING - 2);
    repository.save(review1);
    Optional<ReviewModel> updated = repository.findById(review1.getId());
    assertThat(updated).isPresent();
    assertThat(updated.get().getRating()).isEqualTo(ReviewConstants.MAX_RATING - 2);
    assertThat(updated.get().getComment()).isEqualTo("Great experience!");
  }

  @Test
  void updatesReview_whenUpdateComment_expectCommentUpdate() {
    review1.setComment("Updated comment");
    repository.save(review1);
    Optional<ReviewModel> updated = repository.findById(review1.getId());
    assertThat(updated).isPresent();
    assertThat(updated.get().getComment()).isEqualTo("Updated comment");
  }

  @Test
  void updatesReview_whenUpdateMultipleFields_expectAllFieldsUpdate() {
    review1.setRating(ReviewConstants.MAX_RATING - 1);
    review1.setComment("Updated!!!");
    review1.setJobTitle("Senior Developer");
    review1.setWorkTermSeason("Fall");
    review1.setWorkTermYear(WorkTermValidator.getMaxYear() - 2);

    repository.save(review1);
    Optional<ReviewModel> updated = repository.findById(review1.getId());

    assertThat(updated).isPresent();
    assertThat(updated.get().getRating()).isEqualTo(ReviewConstants.MAX_RATING - 1);
    assertThat(updated.get().getComment()).isEqualTo("Updated!!!");
    assertThat(updated.get().getJobTitle()).isEqualTo("Senior Developer");
    assertThat(updated.get().getWorkTermSeason()).isEqualTo("Fall");
    assertThat(updated.get().getWorkTermYear()).isEqualTo(WorkTermValidator.getMaxYear() - 2);
  }

  @Test
  void findById_whenReviewNotExist_expectEmpty() {
    Optional<ReviewModel> notFound = repository.findById("fakeId");
    assertThat(notFound).isEmpty();
  }

  @Test
  void save_whenDuplicateUserAndCompany_expectUniqueConstraintViolation() {
    assertThrows(
        DuplicateKeyException.class,
        () ->
            repository.save(
                new ReviewModel(
                    "company1",
                    "user1",
                    "Eric Hodgson",
                    ReviewConstants.MAX_RATING - 2,
                    "Another review",
                    "Developer",
                    "Summer",
                    WorkTermValidator.getMaxYear())));
  }

  @Test
  void save_whenSameUserDifferentCompany_expectSuccess() {
    ReviewModel review =
        repository.save(
            new ReviewModel(
                "company3",
                "user1",
                "Eric Hodgson",
                ReviewConstants.MAX_RATING - 1,
                "Good",
                "Developer",
                "Summer",
                WorkTermValidator.getMaxYear()));
    assertThat(review.getId()).isNotNull();
  }

  @Test
  void save_whenDifferentUserSameCompany_expectSuccess() {
    ReviewModel review =
        repository.save(
            new ReviewModel(
                "company1",
                "user3",
                "Evil Eric",
                ReviewConstants.MAX_RATING - 1,
                "Good",
                "Developer",
                "Summer",
                WorkTermValidator.getMaxYear()));
    assertThat(review.getId()).isNotNull();
  }

  @Test
  void count_expectReturnsCorrectCount() {
    long count = repository.count();
    assertThat(count).isEqualTo(3);
  }

  @Test
  void deleteAll_expectEmptyRepository() {
    repository.deleteAll();
    List<ReviewModel> reviews = repository.findAll();
    assertThat(reviews).isEmpty();
    assertThat(repository.count()).isEqualTo(0);
  }

  @Test
  void existsByUserIdAndCompanyId_afterDelete_expectFalse() {
    repository.delete(review1);
    boolean exists = repository.existsByUserIdAndCompanyId("user1", "company1");
    assertThat(exists).isFalse();
  }

  @Test
  void findAll_whenEmpty_expectEmptyList() {
    repository.deleteAll();
    List<ReviewModel> reviews = repository.findAll();
    assertThat(reviews).isEmpty();
  }
}
