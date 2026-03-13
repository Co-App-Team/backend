package com.backend.coapp.dto.request;

import com.backend.coapp.exception.global.InvalidRequestException;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetInterviewApplicationsRequest implements IRequest {

  // optional, however, there are validation rules if provided
  private LocalDate startDate;
  private LocalDate endDate;

  @Override
  public void validateRequest() throws InvalidRequestException {
    if (this.startDate != null && this.endDate == null) {
      throw new InvalidRequestException(
          "If start date is provided, end date must be provided as well.");
    }

    if (this.startDate == null && this.endDate != null) {
      throw new InvalidRequestException(
          "If end date is provided, start date must be provided as well.");
    }

    if (this.startDate != null && this.startDate.isAfter(this.endDate)) {
      throw new InvalidRequestException("Start date must be before end date.");
    }
  }
}
