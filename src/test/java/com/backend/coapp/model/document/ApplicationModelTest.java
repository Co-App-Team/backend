package com.backend.coapp.model.document;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.enumeration.ApplicationStatus;
import com.backend.coapp.util.ApplicationConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ApplicationModel; since we're using lombok in this model, we use the java
 * validator
 */
public class ApplicationModelTest {

  private Validator validator;
  private ApplicationModel validApplication;

  @BeforeEach
  public void setUp() {
    // Java Validator since we're using lombok
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    this.validator = factory.getValidator();

    // Valid Application baseline
    this.validApplication = new ApplicationModel();
    validApplication.setUserId("user123");
    validApplication.setCompanyId("company456");
    validApplication.setJobTitle("Software Engineer");
    validApplication.setStatus(ApplicationStatus.APPLIED);
    validApplication.setApplicationDeadline(LocalDate.now().plusDays(7));
    validApplication.setSourceLink("https://company.com/careers");
    validApplication.setJobDescription("Free Pizza");
    validApplication.setNumPositions(3);
    validApplication.setDateApplied(LocalDate.now());
    validApplication.setNotes("Referral from John Doe");
  }

  @Test
  public void builder_expectCorrectValues() {
    LocalDate deadline = LocalDate.now().plusMonths(1);

    ApplicationModel app =
        ApplicationModel.builder()
            .userId("builder-user")
            .companyId("builder-comp")
            .jobTitle("Architect")
            .status(ApplicationStatus.INTERVIEWING)
            .applicationDeadline(deadline)
            .numPositions(2)
            .build();

    assertNotNull(app);
    assertEquals("builder-user", app.getUserId());
    assertEquals("builder-comp", app.getCompanyId());
    assertEquals("Architect", app.getJobTitle());
    assertEquals(ApplicationStatus.INTERVIEWING, app.getStatus());
    assertEquals(deadline, app.getApplicationDeadline());
    assertEquals(2, app.getNumPositions());
  }

  @Test
  public void validate_whenAllFieldsValid_expectNoViolations() {
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertTrue(violations.isEmpty(), "Expected no validation errors");
  }

  @Test
  public void validate_whenUserIdNull_expectViolation() {
    validApplication.setUserId(null);
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertEquals("User ID cannot be empty", violations.iterator().next().getMessage());
  }

  @Test
  public void validate_whenCompanyIdBlank_expectViolation() {
    validApplication.setCompanyId("   ");
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertEquals("Company ID cannot be empty", violations.iterator().next().getMessage());
  }

  @Test
  public void validate_whenJobTitleEmpty_expectViolation() {
    validApplication.setJobTitle("");
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertEquals("Job title cannot be empty", violations.iterator().next().getMessage());
  }

  @Test
  public void validate_whenStatusNull_expectViolation() {
    validApplication.setStatus(null);
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertEquals("Status cannot be null", violations.iterator().next().getMessage());
  }

  @Test
  public void validate_whenDeadlineNull_expectViolation() {
    validApplication.setApplicationDeadline(null);
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertEquals("Application Deadline cannot be null", violations.iterator().next().getMessage());
  }

  @Test
  public void validate_whenInvalidUrl_expectViolation() {
    validApplication.setSourceLink("invalid-url");
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertEquals("Website must be a valid URL", violations.iterator().next().getMessage());
  }

  @Test
  public void validate_whenDescriptionTooLong_expectViolation() {
    String longDescription = "a".repeat(ApplicationConstants.MAX_JOB_DESCRIPTION_LENGTH + 1);
    validApplication.setJobDescription(longDescription);
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertTrue(violations.iterator().next().getMessage().contains("Description cannot exceed"));
  }

  @Test
  public void validate_whenNumPositionsIsZero_expectViolation() {
    validApplication.setNumPositions(0);
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Number of positions must be at least 1", violations.iterator().next().getMessage());
  }

  @Test
  public void validate_whenNotesTooLong_expectViolation() {
    String longNotes = "a".repeat(2001);
    validApplication.setNotes(longNotes);
    Set<ConstraintViolation<ApplicationModel>> violations = validator.validate(validApplication);
    assertFalse(violations.isEmpty());
    assertTrue(violations.iterator().next().getMessage().contains("Notes cannot exceed"));
  }

  @Test
  public void lombokMethods_expectGettersAndSettersWork() {
    ApplicationModel app = new ApplicationModel();
    app.setNumPositions(5);
    app.setNotes("Some notes");
    app.setDateApplied(LocalDate.now());

    assertEquals(5, app.getNumPositions());
    assertEquals("Some notes", app.getNotes());
    assertNotNull(app.getDateApplied());
  }
}
