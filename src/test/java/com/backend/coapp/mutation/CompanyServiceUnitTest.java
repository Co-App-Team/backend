package com.backend.coapp.mutation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import com.backend.coapp.service.CompanyService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class CompanyServiceUnitTest {

  private CompanyService companyService;
  private CompanyRepository mockCompanyRepository;
  private ReviewRepository mockReviewRepository;

  private CompanyModel nicheCompany;
  private CompanyModel varianCompany;

  @BeforeEach
  void setUp() {
    mockCompanyRepository = Mockito.mock(CompanyRepository.class);
    mockReviewRepository = Mockito.mock(ReviewRepository.class);

    nicheCompany = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
    ReflectionTestUtils.setField(nicheCompany, "id", "company_001");

    varianCompany = new CompanyModel("Varian", "Winnipeg", "https://varian.com");
    ReflectionTestUtils.setField(varianCompany, "id", "company_002");

    companyService = new CompanyService(mockCompanyRepository, mockReviewRepository);
  }

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  @Test
  void constructor_expectSameInitInstance() {
    assertSame(mockCompanyRepository, companyService.getCompanyRepository());
    assertSame(mockReviewRepository, companyService.getReviewRepository());
  }

  // -------------------------------------------------------------------------
  // createCompany
  // -------------------------------------------------------------------------

  @Test
  void createCompany_whenValidData_expectSuccess() {
    when(mockCompanyRepository.findByCompanyNameLower("amazon")).thenReturn(Optional.empty());
    when(mockCompanyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    CompanyResponse response =
        companyService.createCompany("Amazon", "Seattle", "https://amazon.com");

    assertNotNull(response);
    assertEquals("Amazon", response.getCompanyName());
    assertEquals("Seattle", response.getLocation());
    assertEquals("https://amazon.com", response.getWebsite());
    assertEquals(0.0, response.getAvgRating());
  }

  @Test
  void createCompany_whenCompanyAlreadyExists_expectException() {
    when(mockCompanyRepository.findByCompanyNameLower("niche"))
        .thenReturn(Optional.of(nicheCompany));

    Exception exception =
        assertThrows(
            CompanyAlreadyExistsException.class,
            () -> companyService.createCompany("Niche", "Winnipeg", "https://niche.com"));
    assertTrue(exception.getMessage().contains("A company with this name already exists"));
  }

  @Test
  void createCompany_whenCompanyExistsCaseInsensitive_expectException() {
    when(mockCompanyRepository.findByCompanyNameLower("niche"))
        .thenReturn(Optional.of(nicheCompany));

    Exception exception =
        assertThrows(
            CompanyAlreadyExistsException.class,
            () -> companyService.createCompany("NICHE", "Winnipeg", "https://niche.com"));
    assertTrue(exception.getMessage().contains("A company with this name already exists"));
  }

  @Test
  void createCompany_whenInvalidWebsite_expectException() {
    assertThrows(
        InvalidWebsiteException.class,
        () -> companyService.createCompany("Test", "City", "invalidurl.com"));
  }

  @Test
  void createCompany_whenDatabaseSaveFails_expectException() {
    when(mockCompanyRepository.findByCompanyNameLower(anyString())).thenReturn(Optional.empty());
    when(mockCompanyRepository.save(any(CompanyModel.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        CompanyServiceFailException.class,
        () -> companyService.createCompany("Test", "City", "https://test.com"));
    verify(mockCompanyRepository, times(1)).save(any(CompanyModel.class));
  }

  // -------------------------------------------------------------------------
  // getAllCompanies (paginated)
  // -------------------------------------------------------------------------

  @Test
  void getAllCompanies_withPagination_expectSuccess() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<CompanyModel> mockPage = new PageImpl<>(List.of(nicheCompany, varianCompany), pageable, 2);
    when(mockCompanyRepository.findAll(pageable)).thenReturn(mockPage);

    Page<CompanyResponse> page = companyService.getAllCompanies(null, pageable);

    assertNotNull(page);
    assertEquals(2, page.getTotalElements());
    assertEquals(2, page.getContent().size());
  }

  @Test
  void getAllCompanies_withSearch_expectFilteredResults() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<CompanyModel> mockPage = new PageImpl<>(List.of(nicheCompany), pageable, 1);
    when(mockCompanyRepository.findByCompanyNameLowerContaining("niche", pageable))
        .thenReturn(mockPage);

    Page<CompanyResponse> page = companyService.getAllCompanies("niche", pageable);

    assertNotNull(page);
    assertEquals(1, page.getTotalElements());
    assertEquals("Niche", page.getContent().getFirst().getCompanyName());
  }

  @Test
  void getAllCompanies_withSearchNoResults_expectEmpty() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<CompanyModel> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    when(mockCompanyRepository.findByCompanyNameLowerContaining("nonexistent", pageable))
        .thenReturn(emptyPage);

    Page<CompanyResponse> page = companyService.getAllCompanies("nonexistent", pageable);

    assertNotNull(page);
    assertEquals(0, page.getTotalElements());
  }

  @Test
  void getAllCompanies_withBlankSearch_expectAllResults() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<CompanyModel> mockPage = new PageImpl<>(List.of(nicheCompany, varianCompany), pageable, 2);
    when(mockCompanyRepository.findAll(pageable)).thenReturn(mockPage);

    Page<CompanyResponse> page1 = companyService.getAllCompanies("", pageable);
    assertNotNull(page1);
    assertEquals(2, page1.getTotalElements());

    Page<CompanyResponse> page2 = companyService.getAllCompanies("   ", pageable);
    assertNotNull(page2);
    assertEquals(2, page2.getTotalElements());
  }

  @Test
  void getAllCompanies_whenDatabaseFails_expectException() {
    Pageable pageable = PageRequest.of(0, 10);
    when(mockCompanyRepository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

    assertThrows(
        CompanyServiceFailException.class, () -> companyService.getAllCompanies(null, pageable));
  }

  // -------------------------------------------------------------------------
  // getAllCompanies (unpaginated)
  // -------------------------------------------------------------------------

  @Test
  void getAllCompanies_withoutPagination_expectAllResults() {
    Page<CompanyModel> mockPage = new PageImpl<>(List.of(nicheCompany, varianCompany));
    when(mockCompanyRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

    List<CompanyResponse> companies = companyService.getAllCompanies(null);

    assertNotNull(companies);
    assertEquals(2, companies.size());
  }

  // -------------------------------------------------------------------------
  // getCompanyById
  // -------------------------------------------------------------------------

  @Test
  void getCompanyById_whenExists_expectSuccess() {
    when(mockCompanyRepository.findById("company_001")).thenReturn(Optional.of(nicheCompany));

    CompanyResponse response = companyService.getCompanyById("company_001");

    assertNotNull(response);
    assertEquals("Niche", response.getCompanyName());
    assertEquals("company_001", response.getCompanyId());
  }

  @Test
  void getCompanyById_whenNotExists_expectException() {
    when(mockCompanyRepository.findById("nonexistentid")).thenReturn(Optional.empty());

    assertThrows(
        CompanyNotFoundException.class, () -> companyService.getCompanyById("nonexistentid"));
  }

  @Test
  void getCompanyById_whenDatabaseFails_expectException() {
    when(mockCompanyRepository.findById(anyString()))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(CompanyServiceFailException.class, () -> companyService.getCompanyById("someid"));
  }

  // -------------------------------------------------------------------------
  // updateAvgRating
  // -------------------------------------------------------------------------

  @Test
  void updateAvgRating_whenNoReviews_expectZeroRating() {
    when(mockCompanyRepository.findById("company_001")).thenReturn(Optional.of(nicheCompany));
    when(mockReviewRepository.getAverageRatingByCompanyId("company_001")).thenReturn(null);
    when(mockCompanyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> companyService.updateAvgRating("company_001"));

    ArgumentCaptor<CompanyModel> captor = ArgumentCaptor.forClass(CompanyModel.class);
    verify(mockCompanyRepository).save(captor.capture());
    assertEquals(0.0, captor.getValue().getAvgRating());
  }

  @Test
  void updateAvgRating_whenOneReview_expectCorrectRating() {
    when(mockCompanyRepository.findById("company_001")).thenReturn(Optional.of(nicheCompany));
    when(mockReviewRepository.getAverageRatingByCompanyId("company_001")).thenReturn(5.0);
    when(mockCompanyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> companyService.updateAvgRating("company_001"));

    ArgumentCaptor<CompanyModel> captor = ArgumentCaptor.forClass(CompanyModel.class);
    verify(mockCompanyRepository).save(captor.capture());
    assertEquals(5.0, captor.getValue().getAvgRating());
  }

  @Test
  void updateAvgRating_whenMultipleReviews_expectCorrectAverage() {
    when(mockCompanyRepository.findById("company_001")).thenReturn(Optional.of(nicheCompany));
    when(mockReviewRepository.getAverageRatingByCompanyId("company_001")).thenReturn(4.0);
    when(mockCompanyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> companyService.updateAvgRating("company_001"));

    ArgumentCaptor<CompanyModel> captor = ArgumentCaptor.forClass(CompanyModel.class);
    verify(mockCompanyRepository).save(captor.capture());
    assertEquals(4.0, captor.getValue().getAvgRating());
  }

  @Test
  void updateAvgRating_whenCompanyNotFound_expectException() {
    when(mockCompanyRepository.findById("nonexistentid")).thenReturn(Optional.empty());

    assertThrows(
        CompanyNotFoundException.class, () -> companyService.updateAvgRating("nonexistentid"));
  }

  @Test
  void updateAvgRating_whenDatabaseFindFails_expectException() {
    when(mockCompanyRepository.findById(anyString()))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(CompanyServiceFailException.class, () -> companyService.updateAvgRating("someid"));
  }

  @Test
  void updateAvgRating_whenDatabaseSaveFails_expectException() {
    ReviewModel review = mock(ReviewModel.class);
    when(review.getRating()).thenReturn(5);

    when(mockCompanyRepository.findById(anyString())).thenReturn(Optional.of(nicheCompany));
    when(mockReviewRepository.findByCompanyId(anyString())).thenReturn(List.of(review));
    when(mockCompanyRepository.save(any(CompanyModel.class)))
      .thenThrow(new RuntimeException("Database error"));

    String companyId = nicheCompany.getId();

    assertThrows(
      CompanyServiceFailException.class,
      () -> companyService.updateAvgRating(companyId));
    verify(mockCompanyRepository, times(1)).save(any(CompanyModel.class));
  }
}
