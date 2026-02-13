package com.backend.coapp.dto.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/** DTO response for pagination data */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse implements IResponse {

  private int currentPage;
  private int totalPages;
  private long totalItems; // has to be a long to work with Page class totalItems call
  private int itemsPerPage;
  private boolean hasNext;
  private boolean hasPrevious;

  /**
   * Create a map, where keys are object attributes name and values are attributes' values
   *
   * @return Map<String, Object>
   */
  @Override
  public Map<String, Object> toMap() {
    return Map.of(
        "currentPage", this.currentPage,
        "totalPages", this.totalPages,
        "totalItems", this.totalItems,
        "itemsPerPage", this.itemsPerPage,
        "hasNext", this.hasNext,
        "hasPrevious", this.hasPrevious);
  }

  /**
   * maps spring data page object to PaginationResponse DTO
   *
   * @param page Spring Data Page object
   * @return PaginationResponse DTO
   */
  public static PaginationResponse fromPage(Page<?> page) {
    return new PaginationResponse(
        page.getNumber(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.getSize(),
        page.hasNext(),
        page.hasPrevious());
  }
}
