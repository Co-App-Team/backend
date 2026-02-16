# API Documentation - Feature #4: Company Wiki ("Rate My Co-op")

## Overview

This document outlines the API endpoints for the Company Wiki feature of CoApp. This feature allows co-op students and alumni to create company profiles, write reviews, and read reviews from other students about their co-op experiences. This helps students make informed decisions about where to apply.

---

## Data Models

### Company DTO

```json
{
  "companyId": "string",
  "companyName": "string",
  "location": "string",
  "website": "string",
  "avgRating": "float"
}
```

### Review DTO

```json
{
  "reviewId": "string",
  "companyId": "string",
  "userId": "string",
  "authorName": "string",
  "rating": "int",
  "comment": "string",
  "jobTitle": "string",
  "workTermSeason": "string (Fall, Winter, or Summer)",
  "workTermYear": "integer (e.g. 2025)"
}
```

### Rating Values

- Minimum: `1`
- Maximum: `5`
- Type: `Integer`

---

## Endpoints

### 1. Get All Companies

**Path:** `/api/companies`

**Method:** `GET`

**Description:** Retrieves a list of all companies in the wiki. Supports pagination and optional search by company name.

**Authentication:** Required (JWT token in cookie)

**Request Headers:**

- `Cookie`: Contains JWT authentication token (set during login)

**Query Parameters:**

| Parameter       | Type    | Required                        | Description                                                                 | Example              |
|-----------------|---------|---------------------------------|-----------------------------------------------------------------------------|----------------------|
| `search`        | string  | No                              | Search term to match against company name (case insensitive, partial match) | `search=Niche`       |
| `page`          | integer | No                              | Page number for pagination (0-indexed). Defaults to `0`                     | `page=0`             |
| `size`          | integer | No                              | Number of results per page. Defaults to `20`. Max: `100`                    | `size=50`            |
| `usePagination` | boolean | not required (default to false) | enables or disables pagination in the API response                          | `usePagination=true` |

**Request Body:** None

**Response 200 OK (with `usePagination=false` or `usePagination` not present):**

```json
{
  "companies": [
    {
      "companyId": "1",
      "companyName": "Niche",
      "location": "wpg",
      "website": "linkHere",
      "avgRating": "4.5"
    },
    {
      "companyId": "2",
      "companyName": "Varian",
      "location": "wpg",
      "website": "linkHere",
      "avgRating": "4.5"
    },
    {
      "companyId": "3",
      "companyName": "Amazon",
      "location": "wpg",
      "website": "linkHere",
      "avgRating": "4.5"
    },
    {
      "companyId": "4",
      "companyName": "NML",
      "location": "wpg",
      "website": "linkHere",
      "avgRating": "4.5"
    }
  ]
}
```

Note: This would return a list of all companies

**Response 200 OK (with `usePagination=true`):**

```json
{
  "companies": [
    {
      "companyId": "1",
      "companyName": "Niche",
      "location": "wpg",
      "website": "linkHere",
      "avgRating": "4.5"
    },
    {
      "companyId": "2",
      "companyName": "Varian",
      "location": "wpg",
      "website": "linkHere",
      "avgRating": "4.5"
    }
  ],
  "pagination": {
    "currentPage": 0,
    "totalPages": 2,
    "totalItems": 4,
    "itemsPerPage": 2,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

**Response 200 OK:** (when there are no companies yet created)

```json
{
  "companies": [],
  "pagination": {
    "currentPage": 0,
    "totalPages": 1,
    "totalItems": 0,
    "itemsPerPage": 10,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

### 2. Get Company Profile by ID

**Path:** `/api/companies/{companyId}`

**Method:** `GET`

**Description:** Retrieves detailed information about a specific company including all reviews. Supports pagination for reviews.

**Authentication:** Required (JWT token in cookie)

**Path Parameters:**

- `companyId` (string, required): The unique identifier of the company

**Query Parameters:**

| Parameter | Type    | Required | Description                                                     | Example   |
| --------- | ------- | -------- | --------------------------------------------------------------- | --------- |
| `page`    | integer | No       | Page number for reviews pagination (0-indexed). Defaults to `0` | `page=0`  |
| `size`    | integer | No       | Number of reviews per page. Defaults to `10`. Max: `50`         | `size=20` |

**Request Headers:**

- `Cookie`: Contains JWT authentication token (set during login)

**Request Body:** None

**Response 200 OK:**

```json
{
  "company": {
    "companyId": "1",
    "companyName": "Niche",
    "location": "wpg",
    "website": "linkHere",
    "avgRating": 4.5
  },
  "reviews": [
    {
      "reviewId": "1",
      "authorName": "aidan",
      "rating": 5,
      "comment": "Great work life balance and mentorship opportunities.",
      "workTermSeason": "Summer",
      "workTermYear": 2025,
      "jobTitle": "Software developer"
    },
    {
      "reviewId": "2",
      "authorName": "bao",
      "rating": 3,
      "comment": "mid work life balance and mentorship opportunities.",
      "workTermSeason": "Summer",
      "workTermYear": 2025,
      "jobTitle": "Software developer"
    }
  ],
  "reviewsPagination": {
    "currentPage": 0,
    "totalPages": 1,
    "totalItems": 2,
    "itemsPerPage": 10,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

**Response 200 OK:** (company with no reviews)

```json
{
  "company": {
    "companyId": "1",
    "companyName": "Niche",
    "location": "wpg",
    "website": "linkHere",
    "avgRating": 4.5
  },
  "reviews": [],
  "reviewsPagination": {
    "currentPage": 0,
    "totalPages": 1,
    "totalItems": 0,
    "itemsPerPage": 10,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid companyId format. (must be an integer)"
}
```

**Response 404 Not Found:**

```json
{
  "error": "COMPANY_NOT_FOUND",
  "message": "Company with this companyId does not exist."
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

### 3. Create Company Profile

**Path:** `/api/companies`

**Method:** `POST`

**Description:** Creates a new company profile in the wiki. Only creates the company if it doesn't already exist.

**Authentication:** Required (JWT token in cookie)

**Request Headers:**

- `Cookie`: Contains JWT authentication token (set during login)

**Request Body:**

```json
{
  "companyName": "Niche",
  "location": "wpg",
  "website": "linkHere"
}
```

**Response 201 Created:**

```json
{
  "companyId": "1",
  "companyName": "Niche",
  "location": "wpg",
  "website": "linkHere",
  "avgRating": 0.0
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "Fields (companyName, location, website) cannot be null or empty."
}
```

**Response 400 Bad Request:**

```json
{
  "error": "INVALID_WEBSITE",
  "message": "The provided website URL is not valid."
}
```

**Response 409 Conflict:**

```json
{
  "error": "COMPANY_ALREADY_EXISTS",
  "message": "A company with this name already exists."
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

### 4. Create Review

**Path:** `/api/companies/{companyId}/reviews`

**Method:** `POST`

**Description:** Creates a new review for a specific company. Users can only have one review per company.

**Authentication:** Required (JWT token in cookie)

**Path Parameters:**

- `companyId` (string, required): The unique identifier of the company

**Request Headers:**

- `Cookie`: Contains JWT authentication token (set during login)

**Request Body:**

```json
{
  "rating": 5,
  "comment": "Great work life balance and excellent mentorship opportunities.",
  "workTermSeason": "Summer",
  "workTermYear": 2025,
  "jobTitle": "Software developer"
}
```

**Field Requirements:**

- `rating`: Integer between 1 and 5
- `comment` (optional): String with max length of 2000 characters
- `workTermSeason`: String (must be one of: "Fall", "Winter", "Summer")
- `workTermYear`: Integer (must be between 1950 and current year inclusive)
- `jobTitle`: String (e.g. "Software developer ")

**Response 201 Created:**

```json
{
  "reviewId": "1",
  "companyId": "1",
  "userId": "1",
  "authorName": "aidan",
  "rating": 5,
  "comment": "Great work life balance and excellent mentorship opportunities.",
  "workTermSeason": "Summer",
  "workTermYear": 2025,
  "jobTitle": "Software developer"
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "Rating is required and must be between 1 and 5."
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "comment exceeds maximum length of 2000 characters."
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "workTermSeason must be one of Fall, Winter, or Summer."
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "Year must be a non-negative integer between 1950 and the current year"
}
```

**Response 404 Not Found:**

```json
{
  "error": "REVIEW_NOT_FOUND",
  "message": "Company with the provided companyId does not exist."
}
```

**Response 409 Conflict:**

```json
{
  "error": "REVIEW_ALREADY_EXISTS",
  "message": "You have already submitted a review for this company."
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

### 5. Update Review

**Path:** `/api/companies/{companyId}/reviews/{reviewId}`

**Method:** `PUT`

**Description:** Updates an existing review. Users can only update their own reviews.

**Authentication:** Required (JWT token in cookie)

**Path Parameters:**

- `companyId` (string, required): The unique identifier of the company
- `reviewId` (string, required): The unique identifier of the review

**Request Headers:**

- `Cookie`: Contains JWT authentication token (set during login)

**Request Body:**

```json
{
  "rating": 4,
  "comment": "Updated comment stuff",
  "workTermSeason": "Summer",
  "workTermYear": 2025,
  "jobTitle": "Software developer"
}
```

**Field Requirements:**

- `rating` (optional): Integer between 1 and 5
- `comment` (optional): String with max length of 2000 characters
- `workTermSeason` (optional): String
- `workTermYear` (optional): String
- `jobTitle` (optional): String

_Note: At least one field must be provided to update_

**Response 200 OK:**

```json
{
  "reviewId": "1",
  "companyId": "1",
  "userId": "1",
  "authorName": "aidan",
  "rating": 5,
  "comment": "Updated comment stuff",
  "workTermSeason": "Summer",
  "workTermYear": 2025,
  "jobTitle": "Software developer"
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "At least one field must be provided to update the review."
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "Rating must be between 1 and 5."
}
```

**Response 403 Forbidden:**

```json
{
  "error": "REVIEW_NOT_OWNED",
  "message": "You can only update your own reviews."
}
```

**Response 404 Not Found:**

```json
{
  "error": "REVIEW_NOT_FOUND",
  "message": "Review with the provided id does not exist for this company."
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

### 6. Delete Review

**Path:** `/api/companies/{companyId}/reviews/{reviewId}`

**Method:** `DELETE`

**Description:** Deletes a review. Users can only delete their own reviews.

**Authentication:** Required (JWT token in cookie)

**Path Parameters:**

- `companyId` (string, required): The unique identifier of the company
- `reviewId` (string, required): The unique identifier of the review

**Request Headers:**

- `Cookie`: Contains JWT authentication token (set during login)

**Request Body:** None

**Response 200 OK:**

```json
{
  "message": "Review deleted successfully."
}
```

**Response 403 Forbidden:**

```json
{
  "error": "REVIEW_NOT_OWNED",
  "message": "You can only delete your own reviews."
}
```

**Response 404 Not Found:**

```json
{
  "error": "REVIEW_NOT_FOUND",
  "message": "Review with the provided ix does not exist for this company."
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

## Implementation Notes for Backend

### Company Name Uniqueness

- Company names should be case-insensitive unique (e.g. "niche" and "Niche" are the same)
- Normalize company names by trimming whitespace before checking uniqueness

### Review Ownership

- Always verify ownership before allowing updates or deletes
- Never expose raw user IDs in public-facing responses
- Use display names (e.g. "aidan") to protect user privacy

### Rating Calculation

- Recalculate company average rating whenever a review is created, updated, or deleted
- Store the calculated average in the company document for efficient retrieval

### Search Implementation

- Search should be case-insensitive
- Support partial matching for company names

### Validation

- Validate all required fields before processing
- Sanitize comment in case of injection (IDK how much of a concern this is)
- Validate URL format for company websites
- Enforce rating range (1-5)
- Enforce maximum comment length (2000 characters)
- Validate workTermSeason is one of: Fall, Winter, Summer (case-sensitive)
- Validate workTermYear is between 1950 and current year (get current year from server, never trust client)

### Pagination

- Default page size: 20 for companies, 10 for reviews
- Maximum page size: 100 for companies, 50 for reviews
- Always return accurate pagination data

### Security

- Always extract userId from JWT token, never trust client-provided userId
- Validate all path parameters and query parameters

### Data Integrity

- Handle edge cases (e.g. deleting the only review should set avgRating to 0)
- Ensure cascade behavior is defined (e.g. what happens to reviews if a company is deleted)

---

## Testing Checklist

### Company Tests

- [ ] Create company with valid data succeeds
- [ ] Create duplicate company returns 409 conflict
- [ ] Create company with missing required fields returns 400
- [ ] Create company with invalid website URL returns 400
- [ ] Get all companies returns paginated results
- [ ] Search companies by name (case-insensitive, partial match)
- [ ] Get company profile by valid ID returns company with reviews
- [ ] Get company profile by invalid ID returns 404

### Review Tests

- [ ] Create review with valid rating succeeds
- [ ] Create review with only rating (no text) succeeds
- [ ] Create review with rating outside 1-5 range returns 400
- [ ] Create review with text exceeding max length returns 400
- [ ] Create review for non-existent company returns 404
- [ ] Create duplicate review (same user, same company) returns 409
- [ ] Update own review succeeds
- [ ] Update another user's review returns 403
- [ ] Update review with no fields returns 400
- [ ] Delete own review succeeds
- [ ] Delete another user's review returns 403
- [ ] Delete non-existent review returns 404

### Rating Tests

- [ ] Average rating updates correctly when review is created
- [ ] Average rating updates correctly when review is updated
- [ ] Average rating updates correctly when review is deleted
- [ ] Deleting the only review sets avgRating to 0

### Authorization Tests

- [ ] Unauthenticated requests to all endpoints return 401
- [ ] JWT token is correctly extracted from cookie
- [ ] Expired JWT token returns 401

### Edge Cases

- [ ] Pagination with page beyond total pages returns empty
- [ ] Search with no results returns empty
- [ ] Company with no reviews displays correctly
