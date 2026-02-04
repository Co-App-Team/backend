package com.backend.coapp.model.document;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** This test is for CompanyModel without MongoDB. */
public class CompanyModelTest {

  private CompanyModel testCompanyModel;

  @BeforeEach
  public void setUp() {
    this.testCompanyModel =
      new CompanyModel("Niche", "Winnipeg", "https://niche.com");
  }

  @Test
  public void getterMethods_expectInitValues() {
    assertEquals("Niche", this.testCompanyModel.getCompanyName());
    assertEquals("niche", this.testCompanyModel.getCompanyNameLower());
    assertEquals("Winnipeg", this.testCompanyModel.getLocation());
    assertEquals("https://niche.com", this.testCompanyModel.getWebsite());
    assertEquals(0.0, this.testCompanyModel.getAvgRating());
  }

  @Test
  public void constructor_expectTrimsCompanyName() {
    CompanyModel company = new CompanyModel("  Niche  ", "Winnipeg", "https://niche.com");
    assertEquals("Niche", company.getCompanyName());
    assertEquals("niche", company.getCompanyNameLower());
  }

  @Test
  public void constructor_expectLowerCaseCompanyNameLower() {
    CompanyModel company = new CompanyModel("NICHE", "Winnipeg", "https://niche.com");
    assertEquals("NICHE", company.getCompanyName());
    assertEquals("niche", company.getCompanyNameLower());
  }

  @Test
  public void constructor_whenNullCompanyName_expectThrowsException() {
    Exception exception =
      assertThrows(
        IllegalArgumentException.class,
        () -> new CompanyModel(null, "Winnipeg", "https://company.com"));
    assertEquals("Company name cannot be null", exception.getMessage());
  }

  @Test
  public void constructor_expectAvgRatingDefaultsToZero() {
    CompanyModel company = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
    assertEquals(0.0, company.getAvgRating());
  }

  @Test
  public void defaultConstructor_expectAvgRatingDefaultsToZero() {
    CompanyModel company = new CompanyModel();
    assertEquals(0.0, company.getAvgRating());
  }

  @Test
  public void setCompanyName_expectUpdatesBothFields() {
    this.testCompanyModel.setCompanyName("Priceline");
    assertEquals("Priceline", this.testCompanyModel.getCompanyName());
    assertEquals("priceline", this.testCompanyModel.getCompanyNameLower());
  }

  @Test
  public void setCompanyName_expectTrimsWhitespace() {
    this.testCompanyModel.setCompanyName("  Priceline Inc  ");
    assertEquals("Priceline Inc", this.testCompanyModel.getCompanyName());
    assertEquals("priceline inc", this.testCompanyModel.getCompanyNameLower());
  }

  @Test
  public void setCompanyName_whenNull_expectThrowsException() {
    Exception exception =
      assertThrows(
        IllegalArgumentException.class,
        () -> this.testCompanyModel.setCompanyName(null));
    assertEquals("Company name cannot be null", exception.getMessage());
  }

  @Test
  public void setLocation_expectOnlyLocationChange() {
    this.testCompanyModel.setLocation("Toronto");
    assertEquals("Niche", this.testCompanyModel.getCompanyName());
    assertEquals("niche", this.testCompanyModel.getCompanyNameLower());
    assertEquals("Toronto", this.testCompanyModel.getLocation());
    assertEquals("https://niche.com", this.testCompanyModel.getWebsite());
    assertEquals(0.0, this.testCompanyModel.getAvgRating());
  }

  @Test
  public void setWebsite_expectOnlyWebsiteChange() {
    this.testCompanyModel.setWebsite("https://newsite.com");
    assertEquals("Niche", this.testCompanyModel.getCompanyName());
    assertEquals("niche", this.testCompanyModel.getCompanyNameLower());
    assertEquals("Winnipeg", this.testCompanyModel.getLocation());
    assertEquals("https://newsite.com", this.testCompanyModel.getWebsite());
    assertEquals(0.0, this.testCompanyModel.getAvgRating());
  }

  @Test
  public void setAvgRating_expectOnlyAvgRatingChange() {
    this.testCompanyModel.setAvgRating(4.5);
    assertEquals("Niche", this.testCompanyModel.getCompanyName());
    assertEquals("niche", this.testCompanyModel.getCompanyNameLower());
    assertEquals("Winnipeg", this.testCompanyModel.getLocation());
    assertEquals("https://niche.com", this.testCompanyModel.getWebsite());
    assertEquals(4.5, this.testCompanyModel.getAvgRating());
  }

  @Test
  public void setId_expectIdChange() {
    this.testCompanyModel.setId("company123");
    assertEquals("company123", this.testCompanyModel.getId());
  }

  @Test
  public void caseInsensitivity_expectSameLowerCase() {
    CompanyModel company1 = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
    CompanyModel company2 = new CompanyModel("NICHE", "Winnipeg", "https://niche.com");
    CompanyModel company3 = new CompanyModel("NiChE", "Winnipeg", "https://niche.com");

    assertEquals(
      company1.getCompanyNameLower(),
      company2.getCompanyNameLower());
    assertEquals(
      company1.getCompanyNameLower(),
      company3.getCompanyNameLower());
  }

  @Test
  public void getId_whenNoIdSet_expectNull() {
    assertNull(this.testCompanyModel.getId());
  }

  @Test
  public void constructor_whenNameWithInternalSpaces_expectSpacesPreserved() {
    CompanyModel company = new CompanyModel("Priceline  Inc", "Winnipeg", "https://bold.com");
    assertEquals("Priceline  Inc", company.getCompanyName());
    assertEquals("priceline  inc", company.getCompanyNameLower());
  }
}
