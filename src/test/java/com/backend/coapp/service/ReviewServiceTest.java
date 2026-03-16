package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.exception.company.CompanyNotFoundException;
import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.exception.review.ReviewAlreadyExistsException;
import com.backend.coapp.exception.review.ReviewNotFoundException;
import com.backend.coapp.exception.review.ReviewServiceFailException;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
@SpringBootTest
@Testcontainers
public class ReviewServiceTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private CompanyRepository companyRepository;
  @Autowired private ReviewRepository reviewRepository;
  @Autowired private CompanyService companyService;

  private CompanyRepository mockCompanyRepository;
  private ReviewRepository mockReviewRepository;
  private CompanyService mockCompanyService;
  private ReviewService reviewService;

  private CompanyModel nicheCompany;
  private ReviewModel testReview;

  @BeforeEach
  public void setUp() {
    this.companyRepository.deleteAll();
    this.reviewRepository.deleteAll();

    this.nicheCompany = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
    this.companyRepository.save(this.nicheCompany);

    this.testReview =
        new ReviewModel(
            this.nicheCompany.getId(),
            "user1",
            "John Doe",
            5,
            "Great company",
            "Software Developer",
            "Summer",
            2024);
    this.reviewRepository.save(this.testReview);

    this.reviewService =
        new ReviewService(this.reviewRepository, this.companyRepository, this.companyService);
    this.mockCompanyRepository = Mockito.mock(CompanyRepository.class);
    this.mockReviewRepository = Mockito.mock(ReviewRepository.class);
    this.mockCompanyService = Mockito.mock(CompanyService.class);
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertSame(this.reviewRepository, this.reviewService.getReviewRepository());
    assertSame(this.companyRepository, this.reviewService.getCompanyRepository());
    assertSame(this.companyService, this.reviewService.getCompanyService());
  }

  // test creating reviews

  @Test
  public void createReview_whenValidData_expectSuccess() {
    ReviewModel review =
        this.reviewService.createReview(
            this.nicheCompany.getId(),
            "user2",
            "Jane Smith",
            4,
            "Good experience",
            "QA Engineer",
            "Fall",
            2023);

    assertNotNull(review);
    assertNotNull(review.getId());
    assertEquals(this.nicheCompany.getId(), review.getCompanyId());
    assertEquals("user2", review.getUserId());
    assertEquals("Jane Smith", review.getAuthorName());
    assertEquals(4, review.getRating());
    assertEquals("Good experience", review.getComment());
    assertEquals("QA Engineer", review.getJobTitle());
    assertEquals("Fall", review.getWorkTermSeason());
    assertEquals(2023, review.getWorkTermYear());

    // verify company avg rating was updated
    CompanyModel updated = this.companyRepository.findById(this.nicheCompany.getId()).orElseThrow();
    assertEquals(4.5, updated.getAvgRating());
  }

  @Test
  public void createReview_whenCompanyNotFound_expectException() {
    assertThrows(
        CompanyNotFoundException.class,
        () ->
            this.reviewService.createReview(
                "nonexistent", "user2", "Jane", 4, "Good", "Engineer", "Winter", 2023));
  }

  @Test
  public void createReview_whenReviewAlreadyExists_expectException() {
    String companyId = this.nicheCompany.getId();
    Exception exception =
        assertThrows(
            ReviewAlreadyExistsException.class,
            () ->
                this.reviewService.createReview(
                    companyId,
                    "user1",
                    "John Doe",
                    3,
                    "Another review",
                    "Developer",
                    "Fall",
                    2024));
    assertTrue(exception.getMessage().contains("already submitted a review"));
  }

  @Test
  public void createReview_whenDuplicateKeyExceptionAndReviewExists_expectException() {
    this.reviewService =
        new ReviewService(
            this.mockReviewRepository, this.mockCompanyRepository, this.mockCompanyService);
    when(this.mockCompanyRepository.existsById(anyString())).thenReturn(true);
    when(this.mockReviewRepository.existsByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(false)
        .thenReturn(true); // first check passes, but race condition occurs
    when(this.mockReviewRepository.save(any(ReviewModel.class)))
        .thenThrow(new DuplicateKeyException("Duplicate key"));

    assertThrows(
        ReviewAlreadyExistsException.class,
        () ->
            this.reviewService.createReview(
                "companyId", "userId", "Name", 5, "Comment", "Job", "Summer", 2024));
  }

  @Test
  public void createReview_whenDuplicateKeyExceptionButNoReview_expectServiceFailException() {
    this.reviewService =
        new ReviewService(
            this.mockReviewRepository, this.mockCompanyRepository, this.mockCompanyService);
    when(this.mockCompanyRepository.existsById(anyString())).thenReturn(true);
    when(this.mockReviewRepository.existsByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(false);
    when(this.mockReviewRepository.save(any(ReviewModel.class)))
        .thenThrow(new DuplicateKeyException("Duplicate key"));

    assertThrows(
        ReviewServiceFailException.class,
        () ->
            this.reviewService.createReview(
                "companyId", "userId", "Name", 5, "Comment", "Job", "Summer", 2024));
  }

  @Test
  public void createReview_whenDatabaseSaveFails_expectException() {
    this.reviewService =
        new ReviewService(
            this.mockReviewRepository, this.mockCompanyRepository, this.mockCompanyService);
    when(this.mockCompanyRepository.existsById(anyString())).thenReturn(true);
    when(this.mockReviewRepository.existsByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(false);
    when(this.mockReviewRepository.save(any(ReviewModel.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        ReviewServiceFailException.class,
        () ->
            this.reviewService.createReview(
                "companyId", "userId", "Name", 5, "Comment", "Job", "Summer", 2024));
    verify(this.mockReviewRepository, times(1)).save(any(ReviewModel.class));
  }

  // test updating reviews

  @Test
  public void updateReview_whenValidDataWithRatingChange_expectSuccess() {
    ReviewModel updated =
        this.reviewService.updateReview(
            this.testReview.getCompanyId(), "user1", 3, "Updated comment", null, null, null);

    assertNotNull(updated);
    assertEquals(3, updated.getRating());
    assertEquals("Updated comment", updated.getComment());
    assertEquals("Software Developer", updated.getJobTitle()); // unchanged
    assertEquals("Summer", updated.getWorkTermSeason()); // unchanged

    // verify company avg rating was updated
    CompanyModel company = this.companyRepository.findById(this.nicheCompany.getId()).orElseThrow();
    assertEquals(3.0, company.getAvgRating());
  }

  @Test
  public void updateReview_whenAllFieldsUpdated_expectSuccess() {
    ReviewModel updated =
        this.reviewService.updateReview(
            this.testReview.getCompanyId(),
            "user1",
            4,
            "New comment",
            "Senior Developer",
            "Winter",
            2025);

    assertNotNull(updated);
    assertEquals(4, updated.getRating());
    assertEquals("New comment", updated.getComment());
    assertEquals("Senior Developer", updated.getJobTitle());
    assertEquals("Winter", updated.getWorkTermSeason());
    assertEquals(2025, updated.getWorkTermYear());
  }

  @Test
  public void updateReview_whenNoRatingChange_expectNoAvgRatingUpdate() {
    this.reviewService =
        new ReviewService(this.reviewRepository, this.companyRepository, this.mockCompanyService);

    ReviewModel updated =
        this.reviewService.updateReview(
            this.testReview.getCompanyId(), "user1", 5, "New comment", "New Job Title", null, null);

    assertNotNull(updated);
    assertEquals(5, updated.getRating());
    assertEquals("New comment", updated.getComment());
    assertEquals("New Job Title", updated.getJobTitle());

    verify(this.mockCompanyService, never()).updateAvgRating(anyString());
  }

  @Test
  public void updateReview_whenReviewNotFound_expectException() {
    assertThrows(
        ReviewNotFoundException.class,
        () ->
            this.reviewService.updateReview(
                "nonexistent", "user1", 4, "Comment", null, null, null));
  }

  @Test
  public void updateReview_whenUserNotOwner_expectException() {
    String companyId = this.testReview.getCompanyId();
    assertThrows(
        ReviewNotFoundException.class,
        () -> this.reviewService.updateReview(companyId, "user2", 4, "Comment", null, null, null));
  }

  @Test
  public void updateReview_whenDatabaseSaveFails_expectException() {
    this.reviewService =
        new ReviewService(
            this.mockReviewRepository, this.mockCompanyRepository, this.mockCompanyService);
    when(this.mockReviewRepository.findByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(Optional.of(this.testReview));
    when(this.mockReviewRepository.save(any(ReviewModel.class)))
        .thenThrow(new RuntimeException("Database error"));

    String companyId = this.testReview.getCompanyId();
    assertThrows(
        ReviewServiceFailException.class,
        () -> this.reviewService.updateReview(companyId, "user1", 4, "Comment", null, null, null));
    verify(this.mockReviewRepository, times(1)).save(any(ReviewModel.class));
  }

  // test deleting reviews

  @Test
  public void deleteReview_whenValidData_expectSuccess() {
    String reviewCompanyId = this.testReview.getCompanyId();

    assertDoesNotThrow(() -> this.reviewService.deleteReview(reviewCompanyId, "user1"));

    // verify review is deleted
    assertFalse(this.reviewRepository.findById(reviewCompanyId).isPresent());

    // verify company avg rating was updated
    CompanyModel company = this.companyRepository.findById(this.nicheCompany.getId()).orElseThrow();
    assertEquals(0.0, company.getAvgRating()); // no reviews left
  }

  @Test
  public void deleteReview_whenReviewNotFound_expectException() {
    assertThrows(
        ReviewNotFoundException.class,
        () -> this.reviewService.deleteReview("nonexistent", "user1"));
  }

  @Test
  public void deleteReview_whenUserNotOwner_expectException() {
    String companyId = this.nicheCompany.getId();
    assertThrows(
        ReviewNotFoundException.class, () -> this.reviewService.deleteReview(companyId, "user2"));
  }

  @Test
  public void deleteReview_whenDatabaseDeleteFails_expectException() {
    this.reviewService =
        new ReviewService(
            this.mockReviewRepository, this.mockCompanyRepository, this.mockCompanyService);
    when(this.mockReviewRepository.findByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(Optional.of(this.testReview));
    doThrow(new RuntimeException("Database error"))
        .when(this.mockReviewRepository)
        .deleteById(anyString());

    String companyId = this.nicheCompany.getId();
    assertThrows(
        ReviewServiceFailException.class,
        () -> this.reviewService.deleteReview(companyId, "user1"));
    verify(this.mockReviewRepository, times(1)).deleteById(anyString());
  }

  @Test
  public void updateReview_whenOnlyCommentUpdated_expectSuccess() {
    ReviewModel updated =
        this.reviewService.updateReview(
            this.nicheCompany.getId(), "user1", null, "Only comment changed", null, null, null);

    assertNotNull(updated);
    assertEquals(5, updated.getRating()); // unchanged
    assertEquals("Only comment changed", updated.getComment());
  }

  @Test
  public void updateReview_whenCommentHasWhitespace_expectTrimmed() {
    ReviewModel updated =
        this.reviewService.updateReview(
            this.nicheCompany.getId(),
            "user1",
            null,
            "  Comment with spaces  ",
            "  Job with spaces  ",
            null,
            null);

    assertEquals("Comment with spaces", updated.getComment());
    assertEquals("Job with spaces", updated.getJobTitle());
  }

  @Test
  public void updateReview_whenCommentIsNull_expectNoCommentUpdate() {
    String originalComment = this.testReview.getComment();

    ReviewModel updated =
        this.reviewService.updateReview(
            this.nicheCompany.getId(), "user1", 4, null, "New Job Title", null, null);

    assertNotNull(updated);
    assertEquals(4, updated.getRating());
    assertEquals(originalComment, updated.getComment()); // comment unchanged
    assertEquals("New Job Title", updated.getJobTitle());
  }

  @Test
  public void getReviewsByCompanyId_whenReviewsExist_expectPageOfReviews() {
    ReviewModel review2 =
        new ReviewModel(
            this.nicheCompany.getId(),
            "user2",
            "Jane Doe",
            4,
            "Good experience",
            "QA Engineer",
            "Winter",
            2024);
    this.reviewRepository.save(review2);

    Pageable pageable = PageRequest.of(0, 10);
    Page<ReviewModel> reviewsPage =
        this.reviewService.getReviewsByCompanyId(this.nicheCompany.getId(), pageable);

    assertNotNull(reviewsPage);
    assertEquals(2, reviewsPage.getTotalElements());
    assertEquals(2, reviewsPage.getContent().size());
  }

  @Test
  public void getReviewsByCompanyId_whenNoReviews_expectEmptyPage() {
    CompanyModel emptyCompany = new CompanyModel("Empty Co", "Toronto", "https://empty.com");
    this.companyRepository.save(emptyCompany);

    Pageable pageable = PageRequest.of(0, 10);
    Page<ReviewModel> reviewsPage =
        this.reviewService.getReviewsByCompanyId(emptyCompany.getId(), pageable);

    assertNotNull(reviewsPage);
    assertEquals(0, reviewsPage.getTotalElements());
    assertTrue(reviewsPage.getContent().isEmpty());
  }

  @Test
  public void getReviewsByCompanyId_whenDatabaseFails_expectException() {
    this.reviewService =
        new ReviewService(
            this.mockReviewRepository, this.mockCompanyRepository, this.mockCompanyService);

    when(this.mockReviewRepository.findByCompanyId(anyString(), any()))
        .thenThrow(new RuntimeException("Database error"));

    Pageable pageable = PageRequest.of(0, 10);
    assertThrows(
        ReviewServiceFailException.class,
        () -> this.reviewService.getReviewsByCompanyId("companyId", pageable));
  }

  @Test
  public void getReviewsByCompanyId_withPagination_expectCorrectPage() {
    for (int i = 2; i <= 5; i++) {
      ReviewModel review =
          new ReviewModel(
              this.nicheCompany.getId(),
              "user" + i,
              "User " + i,
              4,
              "Comment " + i,
              "Job " + i,
              "Summer",
              2024);
      this.reviewRepository.save(review);
    }

    Pageable pageable = PageRequest.of(0, 2);
    Page<ReviewModel> reviewsPage =
        this.reviewService.getReviewsByCompanyId(this.nicheCompany.getId(), pageable);

    assertNotNull(reviewsPage);
    assertEquals(5, reviewsPage.getTotalElements());
    assertEquals(2, reviewsPage.getContent().size());
    assertEquals(3, reviewsPage.getTotalPages());
  }

  @Test
  public void verifyReviewBelongsToCompany_withNullReviewId_expectException() {
    assertThrows(
        Exception.class, () -> this.reviewService.verifyReviewBelongsToCompany(null, "companyId"));
  }

  @Test
  public void verifyReviewBelongsToCompany_whenReviewBelongsToCompany_expectNoException() {
    String reviewId = this.testReview.getId();
    String companyId = this.nicheCompany.getId();
    assertDoesNotThrow(() -> this.reviewService.verifyReviewBelongsToCompany(reviewId, companyId));
  }

  @Test
  public void verifyReviewBelongsToCompany_whenReviewNotFound_expectReviewNotFoundException() {
    assertThrows(
        ReviewNotFoundException.class,
        () -> this.reviewService.verifyReviewBelongsToCompany("nonexistent-id", "company1"));
  }

  @Test
  public void
      verifyReviewBelongsToCompany_whenReviewBelongsToDifferentCompany_expectInvalidRequestException() {
    // Create a second company
    CompanyModel otherCompany = new CompanyModel("Other Company", "Toronto", "https://other.com");
    this.companyRepository.save(otherCompany);

    // testReview belongs to nicheCompany, not otherCompany
    String reviewId = this.testReview.getId();
    String otherCompanyId = otherCompany.getId();
    InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class,
            () -> this.reviewService.verifyReviewBelongsToCompany(reviewId, otherCompanyId));

    assertTrue(exception.getMessage().contains("does not belong to the specified company"));
  }

  @Test
  public void verifyReviewBelongsToCompany_whenMultipleReviewsForCompany_expectNoException() {
    // Create another review for the same company
    ReviewModel review2 =
        new ReviewModel(
            this.nicheCompany.getId(),
            "user2",
            "Jane Smith",
            4,
            "Good place",
            "Developer",
            "Fall",
            2023);
    this.reviewRepository.save(review2);

    // Verify both reviews belong to nicheCompany
    String testReviewId = this.testReview.getId();
    String nicheCompanyId = this.nicheCompany.getId();
    String review2Id = review2.getId();

    assertDoesNotThrow(
        () -> this.reviewService.verifyReviewBelongsToCompany(testReviewId, nicheCompanyId));

    assertDoesNotThrow(
        () -> this.reviewService.verifyReviewBelongsToCompany(review2Id, nicheCompanyId));
  }
}
