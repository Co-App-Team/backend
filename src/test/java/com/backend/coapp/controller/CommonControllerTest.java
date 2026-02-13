package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.service.JwtService;
import com.backend.coapp.util.WorkTermValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CommonController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommonControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private JwtService jwtService;
  @Autowired private CommonController commonController;

  @Test
  public void constructor_expectSameInitInstance() {
    assertEquals(CommonController.class, this.commonController.getClass());
  }

  // Test term seasons endpoint

  @Test
  public void getTermSeasons_expectArrayOfSeasons() throws Exception {
    mockMvc
        .perform(get("/api/common/termSeasons").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").isString())
        .andExpect(jsonPath("$[1]").isString())
        .andExpect(jsonPath("$[2]").isString());
  }

  @Test
  public void getTermSeasons_expectNonEmptyArray() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/common/termSeasons").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    String[] seasons = this.objectMapper.readValue(responseBody, String[].class);

    assert seasons.length > 0;

    for (String season : seasons) {
      assert season != null;
    }
  }

  @Test
  public void getTermSeasons_expectValidSeasonNames() throws Exception {
    mockMvc
        .perform(get("/api/common/termSeasons").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3));
  }

  // Test term year range endpoint

  @Test
  public void getTermYearRange_expectLowerAndUpperBoundFields() throws Exception {
    mockMvc
        .perform(get("/api/common/termYearRange").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lowerBound").exists())
        .andExpect(jsonPath("$.upperBound").exists());
  }

  @Test
  public void getTermYearRange_expectValidYearValues() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/common/termYearRange").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    java.util.Map<String, String> yearRange =
        this.objectMapper.readValue(responseBody, java.util.Map.class);

    String lowerBound = yearRange.get("lowerBound");
    String upperBound = yearRange.get("upperBound");

    long lowerBoundLong = Long.parseLong(lowerBound);
    long upperBoundLong = Long.parseLong(upperBound);

    assert lowerBoundLong < upperBoundLong;
  }

  @Test
  public void getTermYearRange_expectCorrectBoundValues() throws Exception {
    int minYear = WorkTermValidator.getMinYear();
    int maxYear = WorkTermValidator.getMaxYear();

    mockMvc
        .perform(get("/api/common/termYearRange").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lowerBound").value(String.valueOf(minYear)))
        .andExpect(jsonPath("$.upperBound").value(String.valueOf(maxYear)));
  }

  @Test
  public void getTermYearRange_expectUpperBoundEqualsCurrentYear() throws Exception {
    int currentYear = WorkTermValidator.getMaxYear();

    MvcResult result =
        mockMvc
            .perform(get("/api/common/termYearRange").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    java.util.Map<String, String> yearRange =
        this.objectMapper.readValue(responseBody, java.util.Map.class);

    String upperBound = yearRange.get("upperBound");
    long upperBoundLong = Long.parseLong(upperBound);

    assertEquals(currentYear, upperBoundLong);
  }

  @Test
  public void getTermYearRange_expectLowerBoundLessThanUpperBound() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/common/termYearRange").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    java.util.Map<String, String> yearRange =
        this.objectMapper.readValue(responseBody, java.util.Map.class);

    long lowerBound = Long.parseLong(yearRange.get("lowerBound"));
    long upperBound = Long.parseLong(yearRange.get("upperBound"));

    assert lowerBound < upperBound;
  }
}
