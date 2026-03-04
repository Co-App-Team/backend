package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.dto.request.CreateCompanyRequest;
import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.service.CompanyService;
import com.backend.coapp.service.JwtService;
import com.backend.coapp.service.ReviewService;
import com.backend.coapp.util.PaginationConstants;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CompanyControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private CompanyService companyService;
  @MockitoBean private ReviewService reviewService;
  @MockitoBean private JwtService jwtService;
  @Autowired private CompanyController companyController;

  private CompanyResponse nicheResponse;
  private CompanyResponse varianResponse;
  private CreateCompanyRequest createRequest;
  private ReviewModel review1;
  private ReviewModel review2;

  @BeforeEach
  public void setUp() {
    this.nicheResponse = new CompanyResponse("1", "Niche", "Winnipeg", "https://niche.com", 4.5);
    this.varianResponse = new CompanyResponse("2", "Varian", "Winnipeg", "https://varian.com", 3.5);
    this.createRequest = new CreateCompanyRequest("Amazon", "Seattle", "https://amazon.com");

    this.review1 =
        new ReviewModel(
            "1", "user1", "John Doe", 5, "Great company", "Software Developer", "Summer", 2024);
    this.review1.setId("review1");

    this.review2 =
        new ReviewModel(
            "1", "user2", "Jane Smith", 4, "Good experience", "QA Engineer", "Fall", 2023);
    this.review2.setId("review2");
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertEquals(this.companyController.getCompanyService(), this.companyService);
    assertEquals(this.companyController.getReviewService(), this.reviewService);
  }

  // test get all companies

  @Test
  public void getAllCompanies_withoutPagination_expect200AndCompaniesList() throws Exception {
    List<CompanyResponse> companies = List.of(this.nicheResponse, this.varianResponse);
    when(this.companyService.getAllCompanies(isNull())).thenReturn(companies);

    mockMvc
        .perform(get("/api/companies").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.companies").isArray())
        .andExpect(jsonPath("$.companies[0].companyId").value("1"))
        .andExpect(jsonPath("$.companies[0].companyName").value("Niche"))
        .andExpect(jsonPath("$.companies[1].companyId").value("2"))
        .andExpect(jsonPath("$.pagination").doesNotExist());

    verify(this.companyService, times(1)).getAllCompanies(isNull());
  }

  @Test
  public void getAllCompanies_withPagination_expect200AndPaginatedResponse() throws Exception {
    List<CompanyResponse> companies = List.of(this.nicheResponse);
    Page<CompanyResponse> page = new PageImpl<>(companies, PageRequest.of(0, 20), 1);

    when(this.companyService.getAllCompanies(isNull(), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/api/companies")
                .param("usePagination", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.companies").isArray())
        .andExpect(jsonPath("$.companies[0].companyName").value("Niche"))
        .andExpect(jsonPath("$.pagination").exists())
        .andExpect(jsonPath("$.pagination.currentPage").value(0))
        .andExpect(jsonPath("$.pagination.totalItems").value(1));

    verify(this.companyService, times(1)).getAllCompanies(isNull(), any());
  }

  @Test
  public void getAllCompanies_withSearch_expect200AndFilteredResults() throws Exception {
    List<CompanyResponse> companies = List.of(this.nicheResponse);
    when(this.companyService.getAllCompanies(eq("niche"))).thenReturn(companies);

    mockMvc
        .perform(
            get("/api/companies").param("search", "niche").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.companies").isArray())
        .andExpect(jsonPath("$.companies[0].companyName").value("Niche"));

    verify(this.companyService, times(1)).getAllCompanies(eq("niche"));
  }

  @Test
  public void getAllCompanies_withCustomPageSize_expectCappedSize() throws Exception {
    Page<CompanyResponse> page =
        new PageImpl<>(
            List.of(this.nicheResponse),
            PageRequest.of(0, PaginationConstants.COMPANY_MAX_SIZE),
            1);
    when(this.companyService.getAllCompanies(isNull(), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/api/companies")
                .param("usePagination", "true")
                .param("size", "150") // Above max
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.companyService, times(1))
        .getAllCompanies(
            isNull(), argThat(p -> p.getPageSize() == PaginationConstants.COMPANY_MAX_SIZE));
  }

  @Test
  public void getAllCompanies_withInvalidSize_expectDefaultSize() throws Exception {
    Page<CompanyResponse> page =
        new PageImpl<>(
            List.of(this.nicheResponse),
            PageRequest.of(0, PaginationConstants.COMPANY_DEFAULT_SIZE),
            1);
    when(this.companyService.getAllCompanies(isNull(), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/api/companies")
                .param("usePagination", "true")
                .param("size", "0") // Below min
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // Verify that size was set to default (20)
    verify(this.companyService, times(1))
        .getAllCompanies(
            isNull(), argThat(p -> p.getPageSize() == PaginationConstants.COMPANY_DEFAULT_SIZE));
  }

  @Test
  public void getAllCompanies_withNegativePage_expectDefaultPage() throws Exception {
    Page<CompanyResponse> page =
        new PageImpl<>(List.of(this.nicheResponse), PageRequest.of(0, 20), 1);
    when(this.companyService.getAllCompanies(isNull(), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/api/companies")
                .param("usePagination", "true")
                .param("page", "-1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.companyService, times(1))
        .getAllCompanies(isNull(), argThat(p -> p.getPageNumber() == 0));
  }

  // test get company by id

  @Test
  public void getCompanyById_whenExists_expect200WithCompanyAndReviews() throws Exception {
    when(this.companyService.getCompanyById("1")).thenReturn(this.nicheResponse);

    Page<ReviewModel> reviewsPage =
        new PageImpl<>(List.of(this.review1, this.review2), PageRequest.of(0, 10), 2);
    when(this.reviewService.getReviewsByCompanyId(eq("1"), any())).thenReturn(reviewsPage);

    mockMvc
        .perform(get("/api/companies/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.company.companyId").value("1"))
        .andExpect(jsonPath("$.company.companyName").value("Niche"))
        .andExpect(jsonPath("$.company.location").value("Winnipeg"))
        .andExpect(jsonPath("$.reviews").isArray())
        .andExpect(jsonPath("$.reviews[0].authorName").value("John Doe"))
        .andExpect(jsonPath("$.reviews[1].authorName").value("Jane Smith"))
        .andExpect(jsonPath("$.reviewsPagination").exists())
        .andExpect(jsonPath("$.reviewsPagination.currentPage").value(0))
        .andExpect(jsonPath("$.reviewsPagination.totalItems").value(2));

    verify(this.companyService, times(1)).getCompanyById("1");
    verify(this.reviewService, times(1)).getReviewsByCompanyId(eq("1"), any());
  }

  @Test
  public void getCompanyById_withNoReviews_expect200WithEmptyReviews() throws Exception {
    when(this.companyService.getCompanyById("1")).thenReturn(this.nicheResponse);

    Page<ReviewModel> emptyReviewsPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(this.reviewService.getReviewsByCompanyId(eq("1"), any())).thenReturn(emptyReviewsPage);

    mockMvc
        .perform(get("/api/companies/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.company.companyId").value("1"))
        .andExpect(jsonPath("$.reviews").isArray())
        .andExpect(jsonPath("$.reviews").isEmpty())
        .andExpect(jsonPath("$.reviewsPagination.totalItems").value(0));

    verify(this.companyService, times(1)).getCompanyById("1");
    verify(this.reviewService, times(1)).getReviewsByCompanyId(eq("1"), any());
  }

  @Test
  public void getCompanyById_withCustomPaginationParams_expectCorrectPageable() throws Exception {
    when(this.companyService.getCompanyById("1")).thenReturn(this.nicheResponse);

    Page<ReviewModel> reviewsPage = new PageImpl<>(List.of(this.review1), PageRequest.of(1, 5), 10);
    when(this.reviewService.getReviewsByCompanyId(eq("1"), any())).thenReturn(reviewsPage);

    mockMvc
        .perform(
            get("/api/companies/1")
                .param("page", "1")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviewsPagination.currentPage").value(1))
        .andExpect(jsonPath("$.reviewsPagination.itemsPerPage").value(5));

    verify(this.reviewService, times(1))
        .getReviewsByCompanyId(
            eq("1"), argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 5));
  }

  @Test
  public void getCompanyById_withInvalidPageSize_expectCappedToMax() throws Exception {
    when(this.companyService.getCompanyById("1")).thenReturn(this.nicheResponse);

    Page<ReviewModel> reviewsPage = new PageImpl<>(List.of(), PageRequest.of(0, 50), 0);
    when(this.reviewService.getReviewsByCompanyId(eq("1"), any())).thenReturn(reviewsPage);

    mockMvc
        .perform(
            get("/api/companies/1")
                .param("size", "100") // Above max of 50
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.reviewService, times(1))
        .getReviewsByCompanyId(
            eq("1"), argThat(p -> p.getPageSize() == PaginationConstants.REVIEW_MAX_SIZE));
  }

  @Test
  public void getCompanyById_whenNotFound_expect404() throws Exception {
    when(this.companyService.getCompanyById("999")).thenThrow(new CompanyNotFoundException());

    mockMvc
        .perform(get("/api/companies/999").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("COMPANY_NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists());

    verify(this.companyService, times(1)).getCompanyById("999");
  }

  @Test
  public void getCompanyById_whenServiceFails_expect500() throws Exception {
    when(this.companyService.getCompanyById("1"))
        .thenThrow(new CompanyServiceFailException("Database error"));

    mockMvc
        .perform(get("/api/companies/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());

    verify(this.companyService, times(1)).getCompanyById("1");
  }

  // test creation of companies

  @Test
  public void createCompany_whenValid_expect201AndCompany() throws Exception {
    CompanyResponse response =
        new CompanyResponse("3", "Amazon", "Seattle", "https://amazon.com", 0.0);
    when(this.companyService.createCompany(eq("Amazon"), eq("Seattle"), eq("https://amazon.com")))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.companyId").value("3"))
        .andExpect(jsonPath("$.companyName").value("Amazon"))
        .andExpect(jsonPath("$.location").value("Seattle"))
        .andExpect(jsonPath("$.website").value("https://amazon.com"))
        .andExpect(jsonPath("$.avgRating").value(0.0));

    verify(this.companyService, times(1))
        .createCompany(eq("Amazon"), eq("Seattle"), eq("https://amazon.com"));
  }

  @Test
  public void createCompany_whenAlreadyExists_expect409() throws Exception {
    when(this.companyService.createCompany(eq("Amazon"), eq("Seattle"), eq("https://amazon.com")))
        .thenThrow(new CompanyAlreadyExistsException("1"));

    mockMvc
        .perform(
            post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("COMPANY_ALREADY_EXISTS"))
        .andExpect(jsonPath("$.message").exists());

    verify(this.companyService, times(1))
        .createCompany(eq("Amazon"), eq("Seattle"), eq("https://amazon.com"));
  }

  @Test
  public void createCompany_whenInvalidWebsite_expect400() throws Exception {
    when(this.companyService.createCompany(eq("Amazon"), eq("Seattle"), eq("https://amazon.com")))
        .thenThrow(new InvalidWebsiteException());

    mockMvc
        .perform(
            post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("INVALID_WEBSITE"))
        .andExpect(jsonPath("$.message").exists());

    verify(this.companyService, times(1))
        .createCompany(eq("Amazon"), eq("Seattle"), eq("https://amazon.com"));
  }

  @Test
  public void createCompany_whenMissingFields_expect400() throws Exception {
    CreateCompanyRequest invalidRequest =
        new CreateCompanyRequest(null, "Seattle", "https://amazon.com");

    mockMvc
        .perform(
            post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verify(this.companyService, never()).createCompany(anyString(), anyString(), anyString());
  }

  @Test
  public void createCompany_whenServiceFails_expect500() throws Exception {
    when(this.companyService.createCompany(eq("Amazon"), eq("Seattle"), eq("https://amazon.com")))
        .thenThrow(new CompanyServiceFailException("Database error"));

    mockMvc
        .perform(
            post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.createRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").exists());

    verify(this.companyService, times(1))
        .createCompany(eq("Amazon"), eq("Seattle"), eq("https://amazon.com"));
  }

  @Test
  public void getAllCompanies_whenNoCompanies_expectEmptyList() throws Exception {
    when(this.companyService.getAllCompanies(isNull())).thenReturn(List.of());

    mockMvc
        .perform(get("/api/companies").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.companies").isArray())
        .andExpect(jsonPath("$.companies").isEmpty())
        .andExpect(jsonPath("$.pagination").doesNotExist());

    verify(this.companyService, times(1)).getAllCompanies(isNull());
  }

  @Test
  public void getAllCompanies_whenNoCompaniesWithPagination_expectEmptyWithPagination()
      throws Exception {
    Page<CompanyResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    when(this.companyService.getAllCompanies(isNull(), any())).thenReturn(emptyPage);

    mockMvc
        .perform(
            get("/api/companies")
                .param("usePagination", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.companies").isEmpty())
        .andExpect(jsonPath("$.pagination.totalItems").value(0))
        .andExpect(jsonPath("$.pagination.totalPages").value(0));
  }
}
