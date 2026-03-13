package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backend.coapp.exception.global.InvalidRequestException;
import com.backend.coapp.util.ExperienceConstants;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/* These tests were written with the help of Claude Sonnet 4.5 and revised by Bao Ngo */

class UserExperienceRequestTest {
  private final String VALID_COMPANY_ID = "someCompanyId";
  private final String VALID_ROLE_TITLE = "Software Engineer";
  private final String VALID_ROLE_DESCRIPTION = "Built microservices";
  private final LocalDate VALID_START_DATE = LocalDate.of(2023, 1, 1);
  private final LocalDate VALID_END_DATE = LocalDate.of(2024, 1, 1);

  private UserExperienceRequest buildValidRequest() {
    return UserExperienceRequest.builder()
        .companyId(VALID_COMPANY_ID)
        .roleTitle(VALID_ROLE_TITLE)
        .roleDescription(VALID_ROLE_DESCRIPTION)
        .startDate(VALID_START_DATE)
        .endDate(VALID_END_DATE)
        .build();
  }

  @Test
  void getMethod_expectInitValues() {
    UserExperienceRequest request = buildValidRequest();
    assertEquals(VALID_COMPANY_ID, request.getCompanyId());
    assertEquals(VALID_ROLE_TITLE, request.getRoleTitle());
    assertEquals(VALID_ROLE_DESCRIPTION, request.getRoleDescription());
    assertEquals(VALID_START_DATE, request.getStartDate());
    assertEquals(VALID_END_DATE, request.getEndDate());
  }

  @Test
  void validateRequest_whenValidRequest_expectNoException() {
    assertDoesNotThrow(() -> buildValidRequest().validateRequest());
  }

  @Test
  void validateRequest_whenEndDateIsNull_expectNoException() {
    UserExperienceRequest request =
        UserExperienceRequest.builder()
            .companyId(VALID_COMPANY_ID)
            .roleTitle(VALID_ROLE_TITLE)
            .roleDescription(VALID_ROLE_DESCRIPTION)
            .startDate(VALID_START_DATE)
            .endDate(null) // current job
            .build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenCompanyIdIsNull_expectInvalidRequestException() {
    UserExperienceRequest request = buildValidRequest().toBuilder().companyId(null).build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenCompanyIdIsBlank_expectInvalidRequestException() {
    UserExperienceRequest request = buildValidRequest().toBuilder().companyId("   ").build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenCompanyIdIsEmpty_expectInvalidRequestException() {
    UserExperienceRequest request = buildValidRequest().toBuilder().companyId("").build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRoleTitleIsNull_expectInvalidRequestException() {
    UserExperienceRequest request = buildValidRequest().toBuilder().roleTitle(null).build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRoleTitleIsBlank_expectInvalidRequestException() {
    UserExperienceRequest request = buildValidRequest().toBuilder().roleTitle("   ").build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRoleTitleExceedsMaxLength_expectInvalidRequestException() {
    UserExperienceRequest request =
        buildValidRequest().toBuilder()
            .roleTitle("a".repeat(ExperienceConstants.MAX_JOB_TITLE_LENGTH + 1))
            .build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRoleTitleExactlyAtMaxLength_expectNoException() {
    UserExperienceRequest request =
        buildValidRequest().toBuilder()
            .roleTitle("a".repeat(ExperienceConstants.MAX_JOB_TITLE_LENGTH))
            .build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenRoleDescriptionIsNull_expectInvalidRequestException() {
    UserExperienceRequest request = buildValidRequest().toBuilder().roleDescription(null).build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRoleDescriptionIsBlank_expectInvalidRequestException() {
    UserExperienceRequest request = buildValidRequest().toBuilder().roleDescription("   ").build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRoleDescriptionExceedsMaxLength_expectInvalidRequestException() {
    UserExperienceRequest request =
        buildValidRequest().toBuilder()
            .roleDescription("a".repeat(ExperienceConstants.MAX_EXPERIENCE_DESCRIPTION_LENGTH + 1))
            .build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenRoleDescriptionExactlyAtMaxLength_expectNoException() {
    UserExperienceRequest request =
        buildValidRequest().toBuilder()
            .roleDescription("a".repeat(ExperienceConstants.MAX_EXPERIENCE_DESCRIPTION_LENGTH))
            .build();
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  void validateRequest_whenStartDateIsNull_expectInvalidRequestException() {
    UserExperienceRequest request = buildValidRequest().toBuilder().startDate(null).build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenEndDateIsBeforeStartDate_expectInvalidRequestException() {
    UserExperienceRequest request =
        buildValidRequest().toBuilder().endDate(VALID_START_DATE.minusDays(1)).build();
    assertThrows(InvalidRequestException.class, request::validateRequest);
  }

  @Test
  void validateRequest_whenEndDateIsEqualToStartDate_expectInvalidRequestException() {
    UserExperienceRequest request =
        buildValidRequest().toBuilder()
            .endDate(VALID_START_DATE)
            .build(); // same day — isBefore returns false so no exception
    assertDoesNotThrow(
        request::validateRequest); // endDate.isBefore(startDate) is false for equal dates
  }

  @Test
  void validateRequest_whenEndDateIsAfterStartDate_expectNoException() {
    UserExperienceRequest request =
        buildValidRequest().toBuilder().endDate(VALID_START_DATE.plusDays(1)).build();
    assertDoesNotThrow(request::validateRequest);
  }
}
