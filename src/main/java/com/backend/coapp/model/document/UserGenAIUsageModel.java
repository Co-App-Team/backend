package com.backend.coapp.model.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "userGenAIUsage")
public class UserGenAIUsageModel {
  // Required fields
  @Id private String id;

  @NotBlank(message = "User ID cannot be empty")
  @Indexed(unique = true)
  private String userId;

  @PositiveOrZero(message = "Request count cannot be negative")
  private int requestCount;

  @PositiveOrZero(message = "Monthly request limit cannot be negative")
  private int monthlyLimit;

  @NotNull(message = "Last reset timestamp cannot be null")
  private LocalDateTime lastReset;

  public UserGenAIUsageModel(
      @NotNull String userId, @PositiveOrZero int monthlyLimit, @NotNull LocalDateTime lastReset) {
    this.userId = userId;
    this.monthlyLimit = monthlyLimit;
    this.lastReset = lastReset;
    this.requestCount = 0;
  }
}
