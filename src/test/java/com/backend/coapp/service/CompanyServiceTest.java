package com.backend.coapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.backend.coapp.dto.response.CompanyResponse;
import com.backend.coapp.exception.company.CompanyAlreadyExistsException;
import com.backend.coapp.exception.company.CompanyNotFoundException;
import com.backend.coapp.exception.company.CompanyServiceFailException;
import com.backend.coapp.exception.company.InvalidWebsiteException;
import com.backend.coapp.model.document.CompanyModel;
import com.backend.coapp.model.document.ReviewModel;
import com.backend.coapp.repository.CompanyRepository;
import com.backend.coapp.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
@SpringBootTest
@Testcontainers
public class CompanyServiceTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired private CompanyRepository companyRepository;
  @Autowired private ReviewRepository reviewRepository;

  private CompanyRepository mockCompanyRepository;
  private ReviewRepository mockReviewRepository;
  private CompanyService companyService;

  private CompanyModel nicheCompany;

  @BeforeEach
  public void setUp() {
    this.companyRepository.deleteAll();
    this.reviewRepository.deleteAll();

    this.nicheCompany = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
    this.companyRepository.save(this.nicheCompany);

    // use for pagination related tests (e.g. see if count == 2 in some cases below)
    CompanyModel varianCompany = new CompanyModel("Varian", "Winnipeg", "https://varian.com");
    this.companyRepository.save(varianCompany);

    this.companyService = new CompanyService(this.companyRepository, this.reviewRepository);
    this.mockCompanyRepository = Mockito.mock(CompanyRepository.class);
    this.mockReviewRepository = Mockito.mock(ReviewRepository.class);
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertSame(this.companyRepository, this.companyService.getCompanyRepository());
    assertSame(this.reviewRepository, this.companyService.getReviewRepository());
  }

  // test creating companies

  @Test
  public void createCompany_whenValidData_expectSuccess() {
    CompanyResponse response =
        this.companyService.createCompany("Amazon", "Seattle", "https://amazon.com");
    assertNotNull(response);
    assertEquals("Amazon", response.getCompanyName());
    assertEquals("Seattle", response.getLocation());
    assertEquals("https://amazon.com", response.getWebsite());
    assertEquals(0.0, response.getAvgRating());
  }

  @Test
  public void createCompany_whenCompanyAlreadyExists_expectException() {
    Exception exception =
        assertThrows(
            CompanyAlreadyExistsException.class,
            () -> this.companyService.createCompany("Niche", "Winnipeg", "https://niche.com"));
    assertTrue(exception.getMessage().contains("A company with this name already exists"));
  }

  @Test
  public void createCompany_whenCompanyExistsCaseInsensitive_expectException() {
    Exception exception =
        assertThrows(
            CompanyAlreadyExistsException.class,
            () -> this.companyService.createCompany("NICHE", "Winnipeg", "https://niche.com"));
    assertTrue(exception.getMessage().contains("A company with this name already exists"));
  }

  @Test
  public void createCompany_whenInvalidWebsite_expectException() {
    assertThrows(
        InvalidWebsiteException.class,
        () -> this.companyService.createCompany("Test", "City", "invalidurl.com"));
  }

  @Test
  public void createCompany_whenDatabaseSaveFails_expectException() {
    this.companyService = new CompanyService(this.mockCompanyRepository, this.mockReviewRepository);
    when(this.mockCompanyRepository.findByCompanyNameLower(anyString()))
        .thenReturn(Optional.empty());
    when(this.mockCompanyRepository.save(any(CompanyModel.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        CompanyServiceFailException.class,
        () -> this.companyService.createCompany("Test", "City", "https://test.com"));
    verify(this.mockCompanyRepository, times(1)).save(any(CompanyModel.class));
  }

  // test get all companies

  @Test
  public void getAllCompanies_withPagination_expectSuccess() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<CompanyResponse> page = this.companyService.getAllCompanies(null, pageable);
    assertNotNull(page);
    assertEquals(2, page.getTotalElements());
    assertTrue(page.getContent().size() >= 2);
  }

  @Test
  public void getAllCompanies_withSearch_expectFilteredResults() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<CompanyResponse> page = this.companyService.getAllCompanies("niche", pageable);
    assertNotNull(page);
    assertEquals(1, page.getTotalElements());
    assertEquals("Niche", page.getContent().getFirst().getCompanyName());
  }

  @Test
  public void getAllCompanies_withSearchNoResults_expectEmpty() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<CompanyResponse> page = this.companyService.getAllCompanies("nonexistent", pageable);
    assertNotNull(page);
    assertEquals(0, page.getTotalElements());
  }

  @Test
  public void getAllCompanies_withoutPagination_expectAllResults() {
    List<CompanyResponse> companies = this.companyService.getAllCompanies(null);
    assertNotNull(companies);
    assertTrue(companies.size() >= 2);
  }

  @Test
  public void getAllCompanies_whenDatabaseFails_expectException() {
    this.companyService = new CompanyService(this.mockCompanyRepository, this.mockReviewRepository);
    when(this.mockCompanyRepository.findAll(any(Pageable.class)))
        .thenThrow(new RuntimeException("Database error"));

    Pageable pageable = PageRequest.of(0, 10);
    assertThrows(
        CompanyServiceFailException.class,
        () -> this.companyService.getAllCompanies(null, pageable));
  }

  // test get company by id

  @Test
  public void getCompanyById_whenExists_expectSuccess() {
    CompanyResponse response = this.companyService.getCompanyById(this.nicheCompany.getId());
    assertNotNull(response);
    assertEquals("Niche", response.getCompanyName());
    assertEquals(this.nicheCompany.getId(), response.getCompanyId());
  }

  @Test
  public void getCompanyById_whenNotExists_expectException() {
    assertThrows(
        CompanyNotFoundException.class, () -> this.companyService.getCompanyById("nonexistentid"));
  }

  @Test
  public void getCompanyById_whenDatabaseFails_expectException() {
    this.companyService = new CompanyService(this.mockCompanyRepository, this.mockReviewRepository);
    when(this.mockCompanyRepository.findById(anyString()))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        CompanyServiceFailException.class, () -> this.companyService.getCompanyById("someid"));
  }

  // test updating average rating

  @Test
  public void updateAvgRating_whenNoReviews_expectZeroRating() {
    assertDoesNotThrow(() -> this.companyService.updateAvgRating(this.nicheCompany.getId()));

    CompanyModel updated = this.companyRepository.findById(this.nicheCompany.getId()).get();
    assertEquals(0.0, updated.getAvgRating());
  }

  @Test
  public void updateAvgRating_whenOneReview_expectCorrectRating() {
    ReviewModel review =
        new ReviewModel(
            this.nicheCompany.getId(),
            "user1",
            "John Doe",
            5,
            "Great company",
            "Software Developer",
            "Summer",
            2024);
    this.reviewRepository.save(review);

    assertDoesNotThrow(() -> this.companyService.updateAvgRating(this.nicheCompany.getId()));

    CompanyModel updated = this.companyRepository.findById(this.nicheCompany.getId()).get();
    assertEquals(5.0, updated.getAvgRating());
  }

  @Test
  public void updateAvgRating_whenMultipleReviews_expectCorrectAverage() {
    ReviewModel review1 =
        new ReviewModel(
            this.nicheCompany.getId(),
            "user1",
            "John Doe",
            5,
            "Great",
            "Developer",
            "Summer",
            2024);
    ReviewModel review2 =
        new ReviewModel(
            this.nicheCompany.getId(), "user2", "Jane Smith", 3, "Good", "Engineer", "Fall", 2024);

    this.reviewRepository.save(review1);
    assertDoesNotThrow(() -> this.companyService.updateAvgRating(this.nicheCompany.getId()));
    CompanyModel updated = this.companyRepository.findById(this.nicheCompany.getId()).get();
    assertEquals(5.0, updated.getAvgRating());

    this.reviewRepository.save(review2);
    assertDoesNotThrow(() -> this.companyService.updateAvgRating(this.nicheCompany.getId()));
    updated = this.companyRepository.findById(this.nicheCompany.getId()).get();
    assertEquals(4.0, updated.getAvgRating());
  }

  @Test
  public void updateAvgRating_whenCompanyNotFound_expectException() {
    assertThrows(
        CompanyNotFoundException.class, () -> this.companyService.updateAvgRating("nonexistentid"));
  }

  @Test
  public void updateAvgRating_whenDatabaseFindFails_expectException() {
    this.companyService = new CompanyService(this.mockCompanyRepository, this.mockReviewRepository);
    when(this.mockCompanyRepository.findById(anyString()))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        CompanyServiceFailException.class, () -> this.companyService.updateAvgRating("someid"));
  }

  @Test
  public void updateAvgRating_whenDatabaseSaveFails_expectException() {
    this.companyService = new CompanyService(this.mockCompanyRepository, this.mockReviewRepository);
    when(this.mockCompanyRepository.findById(anyString()))
        .thenReturn(Optional.of(this.nicheCompany));
    when(this.mockReviewRepository.findByCompanyId(anyString())).thenReturn(new ArrayList<>());
    when(this.mockCompanyRepository.save(any(CompanyModel.class)))
        .thenThrow(new RuntimeException("Database error"));
    String companyId = this.nicheCompany.getId();
    assertThrows(
        CompanyServiceFailException.class, () -> this.companyService.updateAvgRating(companyId));
    verify(this.mockCompanyRepository, times(1)).save(any(CompanyModel.class));
  }

  @Test
  public void getAllCompanies_withBlankSearch_expectAllResults() {
    Pageable pageable = PageRequest.of(0, 10);

    Page<CompanyResponse> page1 = this.companyService.getAllCompanies("", pageable);
    assertNotNull(page1);
    assertEquals(2, page1.getTotalElements());

    Page<CompanyResponse> page2 = this.companyService.getAllCompanies("   ", pageable);
    assertNotNull(page2);
    assertEquals(2, page2.getTotalElements());
  }
}
