package com.backend.coapp.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class PaginationResponseTest {

  @Test
  public void getterMethod_expectInitValues() {
    PaginationResponse response = new PaginationResponse(0, 5, 100, 20, true, false);
    assertEquals(0, response.getCurrentPage());
    assertEquals(5, response.getTotalPages());
    assertEquals(100L, response.getTotalItems());
    assertEquals(20, response.getItemsPerPage());
    assertTrue(response.isHasNext());
    assertFalse(response.isHasPrevious());
  }

  @Test
  public void toMap_expectMapWithInitValues() {
    PaginationResponse response = new PaginationResponse(0, 5, 100L, 20, true, false);
    Map<String, Object> expectedMap = Map.of(
      "currentPage", 0,
      "totalPages", 5,
      "totalItems", 100L,
      "itemsPerPage", 20,
      "hasNext", true,
      "hasPrevious", false
    );
    assertEquals(expectedMap, response.toMap());
  }

  @Test
  public void fromPage_expectCorrectMapping() {
    // Create a real Page object using PageImpl
    List<String> content = List.of("item1", "item2");
    PageRequest pageRequest = PageRequest.of(2, 20);
    Page<String> page = new PageImpl<>(content, pageRequest, 200);

    PaginationResponse response = PaginationResponse.fromPage(page);

    assertEquals(2, response.getCurrentPage());
    assertEquals(10, response.getTotalPages()); // 200 total / 20 per page = 10 pages
    assertEquals(200L, response.getTotalItems());
    assertEquals(20, response.getItemsPerPage());
    assertTrue(response.isHasNext()); // page 2 of 10, so has next
    assertTrue(response.isHasPrevious()); // page 2, so has previous
  }

  @Test
  public void fromPage_whenFirstPage_expectNoPrevious() {
    List<String> content = List.of("item1", "item2");
    PageRequest pageRequest = PageRequest.of(0, 20);
    Page<String> page = new PageImpl<>(content, pageRequest, 100);

    PaginationResponse response = PaginationResponse.fromPage(page);

    assertEquals(0, response.getCurrentPage());
    assertTrue(response.isHasNext());
    assertFalse(response.isHasPrevious());
  }

  @Test
  public void fromPage_whenLastPage_expectNoNext() {
    List<String> content = List.of("item1", "item2");
    PageRequest pageRequest = PageRequest.of(4, 20); // Last page (5th page, 0-indexed)
    Page<String> page = new PageImpl<>(content, pageRequest, 82); // 82 items = 5 pages

    PaginationResponse response = PaginationResponse.fromPage(page);

    assertEquals(4, response.getCurrentPage());
    assertFalse(response.isHasNext());
    assertTrue(response.isHasPrevious());
  }

  @Test
  public void fromPage_whenEmptyPage_expectCorrectValues() {
    Page<String> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

    PaginationResponse response = PaginationResponse.fromPage(page);

    assertEquals(0, response.getCurrentPage());
    assertEquals(0, response.getTotalPages());
    assertEquals(0L, response.getTotalItems());
    assertFalse(response.isHasNext());
    assertFalse(response.isHasPrevious());
  }
}
