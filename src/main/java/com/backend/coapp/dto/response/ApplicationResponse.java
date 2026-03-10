package com.backend.coapp.dto.response;

import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.enumeration.ApplicationStatus;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** DTO response for returning an application. */
@Getter
@AllArgsConstructor
public class ApplicationResponse implements IResponse {

  private String applicationId;
  private String companyId;
  private String jobTitle;
  private ApplicationStatus status;
  private LocalDate applicationDeadline;

  // optional fields
  private String jobDescription;
  private Integer numPositions;
  private String sourceLink;
  private LocalDate dateApplied;
  private String notes;
  private LocalDate interviewDate;

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("applicationId", this.applicationId);
    map.put("companyId", this.companyId);
    map.put("jobTitle", this.jobTitle);
    map.put("status", this.status);
    map.put("applicationDeadline", this.applicationDeadline);
    map.put("jobDescription", this.jobDescription);
    map.put("numPositions", this.numPositions);
    map.put("sourceLink", this.sourceLink);
    map.put("dateApplied", this.dateApplied);
    map.put("notes", this.notes);
    map.put("interviewDate", this.interviewDate);
    return map;
  }

  public static ApplicationResponse fromModel(ApplicationModel application) {
    return new ApplicationResponse(
        application.getId(),
        application.getCompanyId(),
        application.getJobTitle(),
        application.getStatus(),
        application.getApplicationDeadline(),
        application.getJobDescription(),
        application.getNumPositions(),
        application.getSourceLink(),
        application.getDateApplied(),
        application.getNotes(),
        application.getInterviewDate());
  }
}
