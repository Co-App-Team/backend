package com.backend.coapp.dto.request;

import com.backend.coapp.exception.global.InvalidRequestException;

/** An interface for every REST API requests. */
public interface IRequest {
  /**
   * Validate all fields (if applicable) of the REST API request. This function will throw
   * InvalidRequestException if the request has at least one invalid field.
   */
  void validateRequest() throws InvalidRequestException;
}
