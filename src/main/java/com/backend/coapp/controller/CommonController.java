package com.backend.coapp.controller;

import com.backend.coapp.model.enumeration.WorkTermSeasonEnum;
import com.backend.coapp.util.WorkTermValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Getter
@RequestMapping("/api/common")
public class CommonController {

  /**
   * Get all term seasons
   *
   * @return ResponseEntity with list of term seasons
   */
  @GetMapping("/termSeasons")
  public ResponseEntity<List<String>> getTermSeasons() {
    List<String> seasons = new ArrayList<>();
    for (WorkTermSeasonEnum season : WorkTermSeasonEnum.values()) {
      seasons.add(season.name());
    }

    return ResponseEntity.ok(seasons);
  }

  /**
   * Get term year range (minimum and maximum allowed years)
   *
   * @return ResponseEntity with year range bounds
   */
  @GetMapping("/termYearRange")
  public ResponseEntity<Map<String, String>> getTermYearRange() {

    Map<String, String> yearRange =
        Map.of(
            "lowerBound", String.valueOf(WorkTermValidator.getMinYear()),
            "upperBound", String.valueOf(WorkTermValidator.getMaxYear()));

    return ResponseEntity.ok(yearRange);
  }
}
