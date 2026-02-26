package com.backend.coapp.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.model.document.CompanyModel;
import java.util.Map;
import org.junit.jupiter.api.Test;

/* these tests were written with the help of Claude Sonnet 4.5 and revised by Eric Hodgson */
public class CompanyResponseTest {

  @Test
  public void getterMethod_expectInitValues() {
    CompanyResponse response =
        new CompanyResponse("1", "Niche", "Winnipeg", "https://niche.com", 4.5);
    assertEquals("1", response.getCompanyId());
    assertEquals("Niche", response.getCompanyName());
    assertEquals("Winnipeg", response.getLocation());
    assertEquals("https://niche.com", response.getWebsite());
    assertEquals(4.5, response.getAvgRating());
  }

  @Test
  public void toMap_expectMapWithInitValues() {
    CompanyResponse response =
        new CompanyResponse("1", "Niche", "Winnipeg", "https://niche.com", 4.5);
    Map<String, Object> expectedMap =
        Map.of(
            "companyId", "1",
            "companyName", "Niche",
            "location", "Winnipeg",
            "website", "https://niche.com",
            "avgRating", 4.5);
    assertEquals(expectedMap, response.toMap());
  }

  @Test
  public void fromModel_expectCorrectMapping() {
    CompanyModel company = new CompanyModel("Niche", "Winnipeg", "https://niche.com");
    // Mock the ID since it would normally be set by the database
    company.setAvgRating(3.5);

    CompanyResponse response = CompanyResponse.fromModel(company);

    assertEquals(company.getId(), response.getCompanyId());
    assertEquals(company.getCompanyName(), response.getCompanyName());
    assertEquals(company.getLocation(), response.getLocation());
    assertEquals(company.getWebsite(), response.getWebsite());
    assertEquals(company.getAvgRating(), response.getAvgRating());
  }
}
