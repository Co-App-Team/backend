package com.backend.coapp.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.document.UserExperienceModel;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

/* These tests were written with the help of Claude Sonnet 4.5 and revised by Bao Ngo */

public class UserExperienceResponseTest {
  private final String EXPERIENCE_ID = "fooExperienceId";
  private final String COMPANY_ID = "fooCompanyId";
  private final String ROLE_TITLE = "Foo Software Engineer";
  private final String ROLE_DESCRIPTION = "Built foo microservices";
  private final LocalDate START_DATE = LocalDate.of(2023, 1, 1);
  private final LocalDate END_DATE = LocalDate.of(2024, 1, 1);

  private UserExperienceResponse buildResponse() {
    return new UserExperienceResponse(
        EXPERIENCE_ID, COMPANY_ID, ROLE_TITLE, ROLE_DESCRIPTION, START_DATE, END_DATE);
  }

  private UserExperienceModel buildModel() {
    UserExperienceModel model =
        new UserExperienceModel(
            "someUserId", COMPANY_ID, ROLE_TITLE, ROLE_DESCRIPTION, START_DATE, END_DATE);
    model.setId(EXPERIENCE_ID);
    return model;
  }

  @Test
  public void getMethod_expectInitValues() {
    UserExperienceResponse response = buildResponse();
    assertEquals(EXPERIENCE_ID, response.getExperienceId());
    assertEquals(COMPANY_ID, response.getCompanyId());
    assertEquals(ROLE_TITLE, response.getRoleTitle());
    assertEquals(ROLE_DESCRIPTION, response.getRoleDescription());
    assertEquals(START_DATE, response.getStartDate());
    assertEquals(END_DATE, response.getEndDate());
  }

  @Test
  public void toMap_expectCorrectKeys() {
    Map<String, Object> map = buildResponse().toMap();
    assertTrue(map.containsKey("experienceId"));
    assertTrue(map.containsKey("companyId"));
    assertTrue(map.containsKey("roleTitle"));
    assertTrue(map.containsKey("roleDescription"));
    assertTrue(map.containsKey("startDate"));
    assertTrue(map.containsKey("endDate"));
  }

  @Test
  public void toMap_expectCorrectValues() {
    Map<String, Object> map = buildResponse().toMap();
    assertEquals(EXPERIENCE_ID, map.get("experienceId"));
    assertEquals(COMPANY_ID, map.get("companyId"));
    assertEquals(ROLE_TITLE, map.get("roleTitle"));
    assertEquals(ROLE_DESCRIPTION, map.get("roleDescription"));
    assertEquals(START_DATE, map.get("startDate"));
    assertEquals(END_DATE, map.get("endDate"));
  }

  @Test
  public void fromModel_expectCorrectMapping() {
    UserExperienceResponse response = UserExperienceResponse.fromModel(buildModel());
    assertEquals(EXPERIENCE_ID, response.getExperienceId());
    assertEquals(COMPANY_ID, response.getCompanyId());
    assertEquals(ROLE_TITLE, response.getRoleTitle());
    assertEquals(ROLE_DESCRIPTION, response.getRoleDescription());
    assertEquals(START_DATE, response.getStartDate());
    assertEquals(END_DATE, response.getEndDate());
  }

  @Test
  public void fromModel_whenEndDateIsNull_expectNullEndDate() {
    UserExperienceModel model =
        new UserExperienceModel(
            "someUserId", COMPANY_ID, ROLE_TITLE, ROLE_DESCRIPTION, START_DATE, null);
    model.setId(EXPERIENCE_ID);

    UserExperienceResponse response = UserExperienceResponse.fromModel(model);
    assertNull(response.getEndDate());
  }
}
