package com.backend.coapp.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.coapp.exception.InvalidRequestException;
import com.backend.coapp.exception.InvalidWebsiteException;
import org.junit.jupiter.api.Test;

public class CreateCompanyRequestTest {

  @Test
  public void getMethods_expectInitValues() {
    CreateCompanyRequest request =
        new CreateCompanyRequest("Niche", "Winnipeg", "https://niche.com");
    assertEquals("Niche", request.getCompanyName());
    assertEquals("Winnipeg", request.getLocation());
    assertEquals("https://niche.com", request.getWebsite());
  }

  @Test
  public void validateRequest_whenValidRequest_expectNoException() {
    CreateCompanyRequest request =
        new CreateCompanyRequest("Niche", "Winnipeg", "https://niche.com");
    assertDoesNotThrow(request::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidCompanyName_expectException() {
    CreateCompanyRequest requestNameNull =
        new CreateCompanyRequest(null, "Winnipeg", "https://niche.com");
    assertThrows(InvalidRequestException.class, requestNameNull::validateRequest);

    CreateCompanyRequest requestNameBlank =
        new CreateCompanyRequest("", "Winnipeg", "https://niche.com");
    assertThrows(InvalidRequestException.class, requestNameBlank::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidLocation_expectException() {
    CreateCompanyRequest requestLocationNull =
        new CreateCompanyRequest("Niche", null, "https://niche.com");
    assertThrows(InvalidRequestException.class, requestLocationNull::validateRequest);

    CreateCompanyRequest requestLocationBlank =
        new CreateCompanyRequest("Niche", "", "https://niche.com");
    assertThrows(InvalidRequestException.class, requestLocationBlank::validateRequest);
  }

  @Test
  public void validateRequest_whenInvalidWebsite_expectException() {
    CreateCompanyRequest requestWebsiteNull = new CreateCompanyRequest("Niche", "Winnipeg", null);
    assertThrows(InvalidRequestException.class, requestWebsiteNull::validateRequest);

    CreateCompanyRequest requestWebsiteBlank = new CreateCompanyRequest("Niche", "Winnipeg", "");
    assertThrows(InvalidRequestException.class, requestWebsiteBlank::validateRequest);

    CreateCompanyRequest requestWebsiteInvalid =
        new CreateCompanyRequest("Niche", "Winnipeg", "niche.com");
    assertThrows(InvalidWebsiteException.class, requestWebsiteInvalid::validateRequest);
  }
}
