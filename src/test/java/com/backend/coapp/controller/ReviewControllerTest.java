package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.dto.request.CreateReviewRequest;
import com.backend.coapp.dto.request.UpdateReviewRequest;
import com.backend.coapp.exception.company.CompanyNotFoundException;
import com.backend.coapp.exception.review.ReviewAlreadyExistsException;
import com.backend.coapp.exception.review.ReviewNotFoundException;
import com.backend.coapp.exception.review.ReviewServiceFailException;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.service.JwtService;
import com.backend.coapp.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private ReviewService reviewService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private Authentication authentication;
  @Autowired private ReviewController reviewController;

  private ReviewModel mockReview;
  private CreateReviewRequest createRequest;
  private UpdateReviewRequest updateRequest;

  @BeforeEach
  public void setUp() {
    UserModel mockUser = mock(UserModel.class);
    when(mockUser.getId()).thenReturn("user1");
    when(mockUser.getFirstName()).thenReturn("John");
    when(mockUser.getLastName()).thenReturn("Doe");

    this.mockReview =
        new ReviewModel(
            "company1",
            "user1",
            "John Doe",
            5,
            "Great company",
            "Software Developer",
            "Summer",
            2024);
    this.mockReview.setId("review1");

    this.createRequest =
        new CreateReviewRequest(5, "Great company", "Software Developer", "Summer", 2024);

    this.updateRequest = new UpdateReviewRequest(4, "Updated comment", null, null, null);

    when(this.authentication.getPrincipal()).thenReturn(mockUser);
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertEquals(this.reviewController.getReviewService(), this.reviewService);
  }

  // test create review

  @Test
  public void createReview_whenValid_expect201AndReview() throws Exception {
    when(this.reviewService.createReview(
            eq("company1"),
            eq("user1"),
            eq("John Doe"),
            eq(5),
            eq("Great company"),
            eq("Software Developer"),
            eq("Summer"),
            eq(2024)))
        .thenReturn(this.mockReview);

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest))
                .principal(this.authentication))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reviewId").value("review1"))
        .andExpect(jsonPath("$.companyId").value("company1"))
        .andExpect(jsonPath("$.userId").value("user1"))
        .andExpect(jsonPath("$.authorName").value("John Doe"))
        .andExpect(jsonPath("$.rating").value(5))
        .andExpect(jsonPath("$.comment").value("Great company"))
        .andExpect(jsonPath("$.jobTitle").value("Software Developer"))
        .andExpect(jsonPath("$.workTermSeason").value("Summer"))
        .andExpect(jsonPath("$.workTermYear").value(2024));

    verify(this.reviewService, times(1))
        .createReview(
            eq("company1"),
            eq("user1"),
            eq("John Doe"),
            eq(5),
            eq("Great company"),
            eq("Software Developer"),
            eq("Summer"),
            eq(2024));
  }

  @Test
  public void createReview_whenCompanyNotFound_expect404() throws Exception {
    when(this.reviewService.createReview(
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            anyString(),
            anyInt()))
        .thenThrow(new CompanyNotFoundException());

    mockMvc
        .perform(
            post("/api/companies/nonexistent/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest))
                .principal(this.authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("COMPANY_NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void createReview_whenReviewAlreadyExists_expect409() throws Exception {
    when(this.reviewService.createReview(
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            anyString(),
            anyInt()))
        .thenThrow(new ReviewAlreadyExistsException());

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest))
                .principal(this.authentication))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("REVIEW_ALREADY_EXISTS"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void createReview_whenMissingRequiredFields_expect400() throws Exception {
    CreateReviewRequest invalidRequest =
        new CreateReviewRequest(null, "Comment", "Developer", "Summer", 2024);

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());

    verify(this.reviewService, never())
        .createReview(
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            anyString(),
            anyInt());
  }

  @Test
  public void createReview_whenServiceFails_expect500() throws Exception {
    when(this.reviewService.createReview(
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            anyString(),
            anyInt()))
        .thenThrow(new ReviewServiceFailException("Database error"));

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest))
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());
  }

  // test update review

  @Test
  public void updateReview_whenValid_expect200AndUpdatedReview() throws Exception {
    ReviewModel updatedReview =
        new ReviewModel(
            "company1",
            "user1",
            "John Doe",
            4,
            "Updated comment",
            "Software Developer",
            "Summer",
            2024);
    updatedReview.setId("review1");

    when(this.reviewService.updateReview(
            eq("company1"),
            eq("user1"),
            eq(4),
            eq("Updated comment"),
            isNull(),
            isNull(),
            isNull()))
        .thenReturn(updatedReview);

    mockMvc
        .perform(
            put("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.updateRequest))
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviewId").value("review1"))
        .andExpect(jsonPath("$.rating").value(4))
        .andExpect(jsonPath("$.comment").value("Updated comment"));

    verify(this.reviewService, times(1))
        .updateReview(
            eq("company1"),
            eq("user1"),
            eq(4),
            eq("Updated comment"),
            isNull(),
            isNull(),
            isNull());
  }

  @Test
  public void updateReview_whenReviewNotFound_expect404() throws Exception {
    when(this.reviewService.updateReview(
            anyString(), anyString(), any(), any(), any(), any(), any()))
        .thenThrow(new ReviewNotFoundException());

    mockMvc
        .perform(
            put("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.updateRequest))
                .principal(this.authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("REVIEW_NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void updateReview_whenNoFieldsProvided_expect400() throws Exception {
    UpdateReviewRequest emptyRequest = new UpdateReviewRequest(null, null, null, null, null);

    mockMvc
        .perform(
            put("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(emptyRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());

    verify(this.reviewService, never())
        .updateReview(anyString(), anyString(), any(), any(), any(), any(), any());
  }

  @Test
  public void updateReview_whenServiceFails_expect500() throws Exception {
    when(this.reviewService.updateReview(
            anyString(), anyString(), any(), any(), any(), any(), any()))
        .thenThrow(new ReviewServiceFailException("Database error"));

    mockMvc
        .perform(
            put("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.updateRequest))
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());
  }

  // test delete review

  @Test
  public void deleteReview_whenValid_expect200WithSuccessMessage() throws Exception {
    doNothing().when(this.reviewService).deleteReview(eq("company1"), eq("user1"));

    mockMvc
        .perform(
            delete("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Review deleted successfully."));

    verify(this.reviewService, times(1)).deleteReview(eq("company1"), eq("user1"));
  }

  @Test
  public void deleteReview_whenReviewNotFound_expect404() throws Exception {
    doThrow(new ReviewNotFoundException())
        .when(this.reviewService)
        .deleteReview(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("REVIEW_NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void deleteReview_whenServiceFails_expect500() throws Exception {
    doThrow(new ReviewServiceFailException("Database error"))
        .when(this.reviewService)
        .deleteReview(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  public void createReview_withoutComment_expect201() throws Exception {
    CreateReviewRequest requestWithoutComment =
        new CreateReviewRequest(5, null, "Software Developer", "Summer", 2024);

    ReviewModel reviewWithoutComment =
        new ReviewModel(
            "company1", "user1", "John Doe", 5, null, "Software Developer", "Summer", 2024);
    reviewWithoutComment.setId("review1");

    when(this.reviewService.createReview(
            eq("company1"),
            eq("user1"),
            eq("John Doe"),
            eq(5),
            any(),
            eq("Software Developer"),
            eq("Summer"),
            eq(2024)))
        .thenReturn(reviewWithoutComment);

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(requestWithoutComment))
                .principal(this.authentication))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reviewId").value("review1"))
        .andExpect(jsonPath("$.rating").value(5))
        .andExpect(jsonPath("$.jobTitle").value("Software Developer"));

    verify(this.reviewService, times(1))
        .createReview(
            eq("company1"),
            eq("user1"),
            eq("John Doe"),
            eq(5),
            isNull(),
            eq("Software Developer"),
            eq("Summer"),
            eq(2024));
  }

  @Test
  public void createReview_withInvalidRating_expect400() throws Exception {
    CreateReviewRequest invalidRequest =
        new CreateReviewRequest(6, "Comment", "Developer", "Summer", 2024); // rating > 5

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createReview_withCommentTooLong_expect400() throws Exception {
    String longComment = "a".repeat(2001); // exceeds 2000 char limit
    CreateReviewRequest invalidRequest =
        new CreateReviewRequest(5, longComment, "Developer", "Summer", 2024);

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createReview_withInvalidWorkTermSeason_expect400() throws Exception {
    CreateReviewRequest invalidRequest =
        new CreateReviewRequest(5, "Comment", "Developer", "Spring", 2024); // invalid season

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createReview_withYearOutOfRange_expect400() throws Exception {
    CreateReviewRequest invalidRequest =
        new CreateReviewRequest(5, "Comment", "Developer", "Summer", 1949); // year < 1950

    mockMvc
        .perform(
            post("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateReview_withOnlyRating_expect200() throws Exception {
    UpdateReviewRequest partialUpdate = new UpdateReviewRequest(3, null, null, null, null);

    ReviewModel updatedReview =
        new ReviewModel(
            "company1",
            "user1",
            "John Doe",
            3,
            "Great company",
            "Software Developer",
            "Summer",
            2024);
    updatedReview.setId("review1");

    when(this.reviewService.updateReview(
            eq("company1"), eq("user1"), eq(3), isNull(), isNull(), isNull(), isNull()))
        .thenReturn(updatedReview);

    mockMvc
        .perform(
            put("/api/companies/company1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(partialUpdate))
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rating").value(3))
        .andExpect(jsonPath("$.comment").value("Great company")); // Original value kept
  }
}
