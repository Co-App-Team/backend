package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import com.backend.coapp.service.CompanyService;
import com.backend.coapp.service.ReviewService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewServiceUnitTest {

  private ReviewService reviewService;
  private ReviewRepository mockReviewRepository;
  private CompanyRepository mockCompanyRepository;
  private CompanyService mockCompanyService;

  private ReviewModel testReview;

  @BeforeEach
  void setUp() {
    mockReviewRepository = Mockito.mock(ReviewRepository.class);
    mockCompanyRepository = Mockito.mock(CompanyRepository.class);
    mockCompanyService = Mockito.mock(CompanyService.class);

    CompanyModel nicheCompany = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
    ReflectionTestUtils.setField(nicheCompany, "id", "company_001");

    testReview =
        new ReviewModel(
            "company_001",
            "user1",
            "John Doe",
            5,
            "Great company",
            "Software Developer",
            "Summer",
            2024);
    ReflectionTestUtils.setField(testReview, "id", "review_001");

    reviewService =
        new ReviewService(mockReviewRepository, mockCompanyRepository, mockCompanyService);
  }

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  @Test
  void constructor_expectSameInitInstance() {
    assertSame(mockReviewRepository, reviewService.getReviewRepository());
    assertSame(mockCompanyRepository, reviewService.getCompanyRepository());
    assertSame(mockCompanyService, reviewService.getCompanyService());
  }

  // -------------------------------------------------------------------------
  // createReview
  // -------------------------------------------------------------------------

  @Test
  void createReview_whenValidData_expectSuccess() {
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockReviewRepository.existsByUserIdAndCompanyId("user2", "company_001")).thenReturn(false);
    when(mockReviewRepository.save(any(ReviewModel.class))).thenAnswer(i -> i.getArgument(0));

    ReviewModel review =
        reviewService.createReview(
            "company_001",
            "user2",
            "Jane Smith",
            4,
            "Good experience",
            "QA Engineer",
            "Fall",
            2023);

    assertNotNull(review);
    assertEquals("company_001", review.getCompanyId());
    assertEquals("user2", review.getUserId());
    assertEquals("Jane Smith", review.getAuthorName());
    assertEquals(4, review.getRating());
    assertEquals("Good experience", review.getComment());
    assertEquals("QA Engineer", review.getJobTitle());
    assertEquals("Fall", review.getWorkTermSeason());
    assertEquals(2023, review.getWorkTermYear());
    verify(mockCompanyService, times(1)).updateAvgRating("company_001");
  }

  @Test
  void createReview_whenCompanyNotFound_expectException() {
    when(mockCompanyRepository.existsById("nonexistent")).thenReturn(false);

    assertThrows(
        CompanyNotFoundException.class,
        () ->
            reviewService.createReview(
                "nonexistent", "user2", "Jane", 4, "Good", "Engineer", "Winter", 2023));

    verify(mockReviewRepository, never()).save(any());
  }

  @Test
  void createReview_whenReviewAlreadyExists_expectException() {
    when(mockCompanyRepository.existsById("company_001")).thenReturn(true);
    when(mockReviewRepository.existsByUserIdAndCompanyId("user1", "company_001")).thenReturn(true);

    Exception ex =
        assertThrows(
            ReviewAlreadyExistsException.class,
            () ->
                reviewService.createReview(
                    "company_001",
                    "user1",
                    "John Doe",
                    3,
                    "Another review",
                    "Developer",
                    "Fall",
                    2024));

    assertTrue(ex.getMessage().contains("already submitted a review"));
    verify(mockReviewRepository, never()).save(any());
  }

  @Test
  void createReview_whenDuplicateKeyExceptionAndReviewExists_expectException() {
    when(mockCompanyRepository.existsById(anyString())).thenReturn(true);
    when(mockReviewRepository.existsByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(false)
        .thenReturn(true);
    when(mockReviewRepository.save(any(ReviewModel.class)))
        .thenThrow(new DuplicateKeyException("Duplicate key"));

    assertThrows(
        ReviewAlreadyExistsException.class,
        () ->
            reviewService.createReview(
                "company_001", "user1", "Name", 5, "Comment", "Job", "Summer", 2024));
  }

  @Test
  void createReview_whenDuplicateKeyExceptionButNoReview_expectServiceFailException() {
    when(mockCompanyRepository.existsById(anyString())).thenReturn(true);
    when(mockReviewRepository.existsByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(false);
    when(mockReviewRepository.save(any(ReviewModel.class)))
        .thenThrow(new DuplicateKeyException("Duplicate key"));

    assertThrows(
        ReviewServiceFailException.class,
        () ->
            reviewService.createReview(
                "company_001", "user1", "Name", 5, "Comment", "Job", "Summer", 2024));
  }

  @Test
  void createReview_whenDatabaseSaveFails_expectException() {
    when(mockCompanyRepository.existsById(anyString())).thenReturn(true);
    when(mockReviewRepository.existsByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(false);
    when(mockReviewRepository.save(any(ReviewModel.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        ReviewServiceFailException.class,
        () ->
            reviewService.createReview(
                "company_001", "user1", "Name", 5, "Comment", "Job", "Summer", 2024));

    verify(mockReviewRepository, times(1)).save(any(ReviewModel.class));
  }

  // -------------------------------------------------------------------------
  // updateReview
  // -------------------------------------------------------------------------

  @Test
  void updateReview_whenValidDataWithRatingChange_expectSuccess() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "company_001"))
        .thenReturn(Optional.of(testReview));
    when(mockReviewRepository.save(any(ReviewModel.class))).thenAnswer(i -> i.getArgument(0));

    ReviewModel updated =
        reviewService.updateReview("company_001", "user1", 3, "Updated comment", null, null, null);

    assertNotNull(updated);
    assertEquals(3, updated.getRating());
    assertEquals("Updated comment", updated.getComment());
    assertEquals("Software Developer", updated.getJobTitle());
    assertEquals("Summer", updated.getWorkTermSeason());
    verify(mockCompanyService, times(1)).updateAvgRating("company_001");
  }

  @Test
  void updateReview_whenAllFieldsUpdated_expectSuccess() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "company_001"))
        .thenReturn(Optional.of(testReview));
    when(mockReviewRepository.save(any(ReviewModel.class))).thenAnswer(i -> i.getArgument(0));

    ReviewModel updated =
        reviewService.updateReview(
            "company_001", "user1", 4, "New comment", "Senior Developer", "Winter", 2025);

    assertNotNull(updated);
    assertEquals(4, updated.getRating());
    assertEquals("New comment", updated.getComment());
    assertEquals("Senior Developer", updated.getJobTitle());
    assertEquals("Winter", updated.getWorkTermSeason());
    assertEquals(2025, updated.getWorkTermYear());
  }

  @Test
  void updateReview_whenNoRatingChange_expectNoAvgRatingUpdate() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "company_001"))
        .thenReturn(Optional.of(testReview));
    when(mockReviewRepository.save(any(ReviewModel.class))).thenAnswer(i -> i.getArgument(0));

    ReviewModel updated =
        reviewService.updateReview(
            "company_001", "user1", 5, "New comment", "New Job Title", null, null);

    assertNotNull(updated);
    assertEquals(5, updated.getRating());
    assertEquals("New comment", updated.getComment());
    assertEquals("New Job Title", updated.getJobTitle());
    verify(mockCompanyService, never()).updateAvgRating(anyString());
  }

  @Test
  void updateReview_whenOnlyCommentUpdated_expectSuccess() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "company_001"))
        .thenReturn(Optional.of(testReview));
    when(mockReviewRepository.save(any(ReviewModel.class))).thenAnswer(i -> i.getArgument(0));

    ReviewModel updated =
        reviewService.updateReview(
            "company_001", "user1", null, "Only comment changed", null, null, null);

    assertNotNull(updated);
    assertEquals(5, updated.getRating());
    assertEquals("Only comment changed", updated.getComment());
    verify(mockCompanyService, never()).updateAvgRating(anyString());
  }

  @Test
  void updateReview_whenCommentHasWhitespace_expectTrimmed() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "company_001"))
        .thenReturn(Optional.of(testReview));
    when(mockReviewRepository.save(any(ReviewModel.class))).thenAnswer(i -> i.getArgument(0));

    ReviewModel updated =
        reviewService.updateReview(
            "company_001",
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
  void updateReview_whenCommentIsNull_expectNoCommentUpdate() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "company_001"))
        .thenReturn(Optional.of(testReview));
    when(mockReviewRepository.save(any(ReviewModel.class))).thenAnswer(i -> i.getArgument(0));

    String originalComment = testReview.getComment();
    ReviewModel updated =
        reviewService.updateReview("company_001", "user1", 4, null, "New Job Title", null, null);

    assertNotNull(updated);
    assertEquals(4, updated.getRating());
    assertEquals(originalComment, updated.getComment());
    assertEquals("New Job Title", updated.getJobTitle());
  }

  @Test
  void updateReview_whenReviewNotFound_expectException() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "nonexistent"))
        .thenReturn(Optional.empty());

    assertThrows(
        ReviewNotFoundException.class,
        () -> reviewService.updateReview("nonexistent", "user1", 4, "Comment", null, null, null));
  }

  @Test
  void updateReview_whenUserNotOwner_expectException() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user2", "company_001"))
        .thenReturn(Optional.empty());

    assertThrows(
        ReviewNotFoundException.class,
        () -> reviewService.updateReview("company_001", "user2", 4, "Comment", null, null, null));
  }

  @Test
  void updateReview_whenDatabaseSaveFails_expectException() {
    when(mockReviewRepository.findByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(Optional.of(testReview));
    when(mockReviewRepository.save(any(ReviewModel.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        ReviewServiceFailException.class,
        () -> reviewService.updateReview("company_001", "user1", 4, "Comment", null, null, null));

    verify(mockReviewRepository, times(1)).save(any(ReviewModel.class));
  }

  // -------------------------------------------------------------------------
  // deleteReview
  // -------------------------------------------------------------------------

  @Test
  void deleteReview_whenValidData_expectSuccess() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "company_001"))
        .thenReturn(Optional.of(testReview));

    assertDoesNotThrow(() -> reviewService.deleteReview("company_001", "user1"));

    verify(mockReviewRepository, times(1)).deleteById("review_001");
    verify(mockCompanyService, times(1)).updateAvgRating("company_001");
  }

  @Test
  void deleteReview_whenReviewNotFound_expectException() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user1", "nonexistent"))
        .thenReturn(Optional.empty());

    assertThrows(
        ReviewNotFoundException.class, () -> reviewService.deleteReview("nonexistent", "user1"));

    verify(mockReviewRepository, never()).deleteById(anyString());
  }

  @Test
  void deleteReview_whenUserNotOwner_expectException() {
    when(mockReviewRepository.findByUserIdAndCompanyId("user2", "company_001"))
        .thenReturn(Optional.empty());

    assertThrows(
        ReviewNotFoundException.class, () -> reviewService.deleteReview("company_001", "user2"));

    verify(mockReviewRepository, never()).deleteById(anyString());
  }

  @Test
  void deleteReview_whenDatabaseDeleteFails_expectException() {
    when(mockReviewRepository.findByUserIdAndCompanyId(anyString(), anyString()))
        .thenReturn(Optional.of(testReview));
    doThrow(new RuntimeException("Database error"))
        .when(mockReviewRepository)
        .deleteById(anyString());

    assertThrows(
        ReviewServiceFailException.class, () -> reviewService.deleteReview("company_001", "user1"));

    verify(mockReviewRepository, times(1)).deleteById(anyString());
  }

  // -------------------------------------------------------------------------
  // getReviewsByCompanyId
  // -------------------------------------------------------------------------

  @Test
  void getReviewsByCompanyId_whenReviewsExist_expectPageOfReviews() {
    ReviewModel review2 =
        new ReviewModel(
            "company_001",
            "user2",
            "Jane Doe",
            4,
            "Good experience",
            "QA Engineer",
            "Winter",
            2024);
    Pageable pageable = PageRequest.of(0, 10);
    Page<ReviewModel> mockPage = new PageImpl<>(List.of(testReview, review2), pageable, 2);

    when(mockReviewRepository.findByCompanyId("company_001", pageable)).thenReturn(mockPage);

    Page<ReviewModel> result = reviewService.getReviewsByCompanyId("company_001", pageable);

    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    assertEquals(2, result.getContent().size());
  }

  @Test
  void getReviewsByCompanyId_whenNoReviews_expectEmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<ReviewModel> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(mockReviewRepository.findByCompanyId("company_002", pageable)).thenReturn(emptyPage);

    Page<ReviewModel> result = reviewService.getReviewsByCompanyId("company_002", pageable);

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  void getReviewsByCompanyId_withPagination_expectCorrectPage() {
    Pageable pageable = PageRequest.of(0, 2);
    Page<ReviewModel> mockPage = new PageImpl<>(List.of(testReview), pageable, 5);

    when(mockReviewRepository.findByCompanyId("company_001", pageable)).thenReturn(mockPage);

    Page<ReviewModel> result = reviewService.getReviewsByCompanyId("company_001", pageable);

    assertNotNull(result);
    assertEquals(5, result.getTotalElements());
    assertEquals(3, result.getTotalPages());
  }

  @Test
  void getReviewsByCompanyId_whenDatabaseFails_expectException() {
    Pageable pageable = PageRequest.of(0, 10);
    when(mockReviewRepository.findByCompanyId(anyString(), any()))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        ReviewServiceFailException.class,
        () -> reviewService.getReviewsByCompanyId("company_001", pageable));
  }

  // -------------------------------------------------------------------------
  // verifyReviewBelongsToCompany
  // -------------------------------------------------------------------------

  @Test
  void verifyReviewBelongsToCompany_withNullReviewId_expectException() {
    assertThrows(
        Exception.class, () -> reviewService.verifyReviewBelongsToCompany(null, "company_001"));
  }

  @Test
  void verifyReviewBelongsToCompany_whenReviewBelongsToCompany_expectNoException() {
    when(mockReviewRepository.findById("review_001")).thenReturn(Optional.of(testReview));

    assertDoesNotThrow(
        () -> reviewService.verifyReviewBelongsToCompany("review_001", "company_001"));
  }

  @Test
  void verifyReviewBelongsToCompany_whenReviewNotFound_expectReviewNotFoundException() {
    when(mockReviewRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ReviewNotFoundException.class,
        () -> reviewService.verifyReviewBelongsToCompany("nonexistent", "company_001"));
  }

  @Test
  void
      verifyReviewBelongsToCompany_whenReviewBelongsToDifferentCompany_expectInvalidRequestException() {
    when(mockReviewRepository.findById("review_001")).thenReturn(Optional.of(testReview));

    InvalidRequestException ex =
        assertThrows(
            InvalidRequestException.class,
            () -> reviewService.verifyReviewBelongsToCompany("review_001", "company_999"));

    assertTrue(ex.getMessage().contains("does not belong to the specified company"));
  }

  @Test
  void verifyReviewBelongsToCompany_whenMultipleReviewsForCompany_expectNoException() {
    ReviewModel review2 =
        new ReviewModel(
            "company_001", "user2", "Jane Smith", 4, "Good place", "Developer", "Fall", 2023);
    ReflectionTestUtils.setField(review2, "id", "review_002");

    when(mockReviewRepository.findById("review_001")).thenReturn(Optional.of(testReview));
    when(mockReviewRepository.findById("review_002")).thenReturn(Optional.of(review2));

    assertDoesNotThrow(
        () -> reviewService.verifyReviewBelongsToCompany("review_001", "company_001"));
    assertDoesNotThrow(
        () -> reviewService.verifyReviewBelongsToCompany("review_002", "company_001"));
  }
}
