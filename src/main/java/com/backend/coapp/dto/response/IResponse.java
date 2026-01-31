package com.backend.coapp.dto.response;

import java.util.Map;

/** An interface for every DTO responses of all REST API. */
public interface IResponse {
  /**
   * Create a map, where keys are object attributes name and values are attributes' values
   *
   * @return Map
   */
  public Map<String, Object> toMap();
}
