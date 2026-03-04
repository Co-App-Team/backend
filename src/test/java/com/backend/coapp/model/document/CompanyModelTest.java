package com.backend.coapp.model.document;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for CompanyModel */
public class CompanyModelTest {

  private static ValidatorFactory validatorFactory;
  private static Validator validator;
  private CompanyModel validCompany;

  @BeforeAll
  public static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  public static void tearDownValidator() {
    if (validatorFactory != null) {
      validatorFactory.close();
    }
  }

  @BeforeEach
  public void setUp() {
    this.validCompany = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
  }

  /* test constructor */

  @Test
  public void constructor_whenAllValid_expectSuccess() {
    CompanyModel company = new CompanyModel("Priceline", "Winnipeg", "https://priceline.com");

    assertNotNull(company);
    assertEquals("Priceline", company.getCompanyName());
    assertEquals("priceline", company.getCompanyNameLower());
    assertEquals("Winnipeg", company.getLocation());
    assertEquals("https://priceline.com", company.getWebsite());
    assertEquals(0.0, company.getAvgRating());
  }

  @Test
  public void constructor_whenTrimsWhitespace_expectTrimmed() {
    CompanyModel company =
        new CompanyModel("  Priceline  ", "  Winnipeg  ", "  https://priceline.com  ");

    assertEquals("Priceline", company.getCompanyName());
    assertEquals("priceline", company.getCompanyNameLower());
    assertEquals("Winnipeg", company.getLocation());
    assertEquals("https://priceline.com", company.getWebsite());
  }

  @Test
  public void constructor_expectAvgRatingDefaultsToZero() {
    CompanyModel company = new CompanyModel("Test Co", "Location", "https://test.com");
    assertEquals(0.0, company.getAvgRating());
  }

  @Test
  public void noArgsConstructor_expectFieldsNull() {
    CompanyModel company = new CompanyModel();

    assertNull(company.getCompanyName());
    assertNull(company.getCompanyNameLower());
    assertNull(company.getLocation());
    assertNull(company.getWebsite());
    assertNull(company.getAvgRating());
  }

  @Test
  public void constructor_whenNullCompanyName_expectBothNull() {
    CompanyModel company = new CompanyModel(null, "Location", "https://test.com");

    assertNull(company.getCompanyName());
    assertNull(company.getCompanyNameLower());
    assertEquals("Location", company.getLocation());
    assertEquals("https://test.com", company.getWebsite());
    assertEquals(0.0, company.getAvgRating());
  }

  @Test
  public void constructor_whenNullLocation_expectNull() {
    CompanyModel company = new CompanyModel("Test", null, "https://test.com");

    assertEquals("Test", company.getCompanyName());
    assertNull(company.getLocation());
    assertEquals("https://test.com", company.getWebsite());
  }

  @Test
  public void constructor_whenNullWebsite_expectNull() {
    CompanyModel company = new CompanyModel("Test", "Location", null);

    assertEquals("Test", company.getCompanyName());
    assertEquals("Location", company.getLocation());
    assertNull(company.getWebsite());
  }

  @Test
  public void constructor_whenNullCompanyName_expectHandled() {
    CompanyModel company = new CompanyModel(null, "Location", "https://test.com");
    assertNull(company.getCompanyName());
    assertNull(company.getCompanyNameLower());
  }

  @Test
  public void constructor_whenNullLocation_expectHandled() {
    CompanyModel company = new CompanyModel("Test", null, "https://test.com");
    assertNull(company.getLocation());
  }

  @Test
  public void constructor_whenNullWebsite_expectHandled() {
    CompanyModel company = new CompanyModel("Test", "Location", null);
    assertNull(company.getWebsite());
  }

  /* test custom setters */

  @Test
  public void setCompanyName_expectUpdatesBothFields() {
    validCompany.setCompanyName("Priceline");

    assertEquals("Priceline", validCompany.getCompanyName());
    assertEquals("priceline", validCompany.getCompanyNameLower());
  }

  @Test
  public void setCompanyName_expectTrimsWhitespace() {
    validCompany.setCompanyName("  Priceline Inc  ");

    assertEquals("Priceline Inc", validCompany.getCompanyName());
    assertEquals("priceline inc", validCompany.getCompanyNameLower());
  }

  @Test
  public void setCompanyName_whenNull_expectBothNull() {
    validCompany.setCompanyName(null);

    assertNull(validCompany.getCompanyName());
    assertNull(validCompany.getCompanyNameLower());
  }

  @Test
  public void setCompanyName_whenCaseChanges_expectLowerUpdated() {
    validCompany.setCompanyName("PAYWORKS");
    assertEquals("PAYWORKS", validCompany.getCompanyName());
    assertEquals("payworks", validCompany.getCompanyNameLower());

    validCompany.setCompanyName("PayWorks");
    assertEquals("PayWorks", validCompany.getCompanyName());
    assertEquals("payworks", validCompany.getCompanyNameLower());
  }

  /* test jakarta validation annotations with a validator */

  @Test
  public void validate_whenAllFieldsValid_expectNoViolations() {
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void validate_whenCompanyNameNull_expectViolation() {
    validCompany.setCompanyName(null);
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenCompanyNameBlank_expectViolation() {
    validCompany.setCompanyName("");
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Company name cannot be empty")));
  }

  @Test
  public void validate_whenLocationNull_expectViolation() {
    validCompany.setLocation(null);
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenLocationBlank_expectViolation() {
    validCompany.setLocation("   ");
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenWebsiteNull_expectViolation() {
    validCompany.setWebsite(null);
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenWebsiteBlank_expectViolation() {
    validCompany.setWebsite("");
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void validate_whenInvalidUrl_expectViolation() {
    validCompany.setWebsite("not-a-url");
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Website must be a valid URL")));
  }

  @Test
  public void validate_whenUrlWithoutProtocol_expectViolation() { // Changed name
    validCompany.setWebsite("niche.com");
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Website must be a valid URL")));
  }

  @Test
  public void validate_whenValidUrlWithHttp_expectNoViolation() {
    validCompany.setWebsite("http://niche.com");
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertTrue(violations.isEmpty());
  }

  @Test
  public void validate_whenValidUrlWithHttps_expectNoViolation() {
    validCompany.setWebsite("https://niche.com");
    Set<ConstraintViolation<CompanyModel>> violations = validator.validate(validCompany);

    assertTrue(violations.isEmpty());
  }

  /* test lombok getters and setters */

  @Test
  public void lombokSetters_expectWork() {
    CompanyModel company = new CompanyModel();
    company.setLocation("Toronto");
    company.setWebsite("https://example.com");
    company.setAvgRating(4.5);

    assertEquals("Toronto", company.getLocation());
    assertEquals("https://example.com", company.getWebsite());
    assertEquals(4.5, company.getAvgRating());
  }

  @Test
  public void lombokGetters_expectWork() {
    assertNotNull(validCompany.getCompanyName());
    assertNotNull(validCompany.getLocation());
    assertNotNull(validCompany.getWebsite());
    assertNotNull(validCompany.getAvgRating());
  }
}
