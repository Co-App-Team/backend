# API Documentation - Feature #3: Application Filtering/Search

## Overview
This document outlines the API endpoints for the Application Filtering/Search feature of CoApp. This feature allows users to search, filter, and sort their job applications efficiently.

The first endpoint is the most important and provides the minimum requirements for this feature. The second and third are "nice to haves" that may make the frontend UI a bit nicer in the long run, but we can omit these if preferred.

---

## Data Models

**Note:** This will also require a User DTO, which should be outlined in the documentation for Feature 1. There may also be a Job Application DTO outlined in feature 2, so this outlines the minimum requirements of that DTO for this feature.

```json
{
  "id": "string",
  "userId": "string",
  "companyName": "string",
  "jobTitle": "string",
  "postingLink": "string",
  "status": "string",
  "dateApplied": "datetime string",
  "notes": "string (optional)",
  "address": "string (optional)"
}
```

**Note:** We could also optionally add `dateCreated` and `dateModified` for other sorting options, if we so desired. We could also sort by company names alphabetically or other similar options.

### Status Enum Values

- `"NOT_APPLIED"`
- `"APPLIED"`
- `"INTERVIEW_SCHEDULED"`
- `"INTERVIEWING"`
- `"OFFER_RECEIVED"`
- `"REJECTED"`
- `"WITHDRAWN"`
- `"ACCEPTED"`

---

## Endpoints

### 1. Get Filtered/Searched Applications

**Path:** `/api/applications`

**Method:** `GET`

**Description:** Retrieves a list of applications for the authenticated user with optional search, filter, and sort capabilities. All query parameters are optional and can be combined.

**Authentication:** Required (JWT token in cookie)

**Request Headers:**

- `Cookie`: Contains JWT authentication token (set during login)

**Query Parameters:**

| Parameter | Type | Required | Description | Example |
| :-- | :-- | :-- | :-- | :-- |
| `search` | string | No | Search term to match against company name (case-insensitive, partial match) | `search=Niche` |
| `status` | string | No | Filter by application status. Can be comma-separated for multiple filters | `status=APPLIED,INTERVIEWING` |
| `sortBy` | string | No | Field to sort by. Currently only 1 option: `dateApplied` | `sortBy=dateApplied` |
| `sortOrder` | string | No | Sort direction. Options: `asc`, `desc`. Defaults to `desc` | `sortOrder=asc` |
| `page` | integer | No | Page number for pagination (0-indexed). Defaults to `0` | `page=0` |
| `size` | integer | No | Number of results per page. Defaults to `20`. Max: `100` | `size=50` |

**Request Body:** None

**Response 200 OK:**

```json
{
  "applications": [
    {
      "id": "x",
      "userId": "y",
      "companyName": "Niche",
      "jobTitle": "Software Engineering Intern",
      "postingLink": "link",
      "status": "APPLIED",
      "dateApplied": "date string",
      "notes": "Applied through university portal",
      "address": "address here"
    },
    {
      "id": "x",
      "userId": "y",
      "companyName": "Varian",
      "jobTitle": "Software Engineering Intern",
      "postingLink": "link",
      "status": "APPLIED",
      "dateApplied": "date string",
      "notes": "Applied through university portal",
      "address": "address here"
    }
  ],
  "pagination": {
    "currentPage": 0,
    "totalPages": 3,
    "totalItems": 45,
    "itemsPerPage": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid query parameter",
  "details": {
    "parameter": "status",
    "invalidValue": "INVALID_STATUS_VALUE_HERE",
    "validValues": ["NOT_APPLIED", "APPLIED", "INTERVIEW_SCHEDULED", "INTERVIEWING", "OFFER_RECEIVED", "REJECTED", "WITHDRAWN", "ACCEPTED"]
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

### 2. Get Application Count by Filter

**Path:** `/api/applications/count`

**Method:** `GET`

**Description:** Returns the count of applications matching the specified filters. Useful for UI indicators showing result counts before loading full data.

**Authentication:** Required (JWT token in cookie)

**Request Headers:**

- `Cookie`: Contains JWT authentication token

**Query Parameters:**


| Parameter | Type | Required | Description | Example |
| :-- | :-- | :-- | :-- | :-- |
| `search` | string | No | Search term to match against company name and job title | `search=Niche` |
| `status` | string | No | Filter by application status. Can be comma-separated | `status=APPLIED,INTERVIEWING` |

**Request Body:** None

**Response 200 OK:**

```json
{
  "count": 12
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```


---

### 3. Get Distinct Status Counts

**Path:** `/api/applications/statuses`

**Method:** `GET`

**Description:** Returns all unique status values present in the authenticated user's applications. Useful for dynamically populating filter dropdowns in the UI.

**Authentication:** Required (JWT token in cookie)

**Request Headers:**

- `Cookie`: Contains JWT authentication token

**Query Parameters:** None

**Request Body:** None

**Response 200 OK:**

```json
{
  "statuses": [
    {
      "value": "APPLIED",
      "count": 1
    },
    {
      "value": "INTERVIEWING",
      "count": 2
    },
    {
      "value": "REJECTED",
      "count": 3
    },
    {
      "value": "NOT_APPLIED",
      "count": 4
    }
  ]
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```

---

## Implementation Notes for Backend

### Search Implementation

- Search should be **case-insensitive**
- Search should support **partial matching** (e.g., "Nic" matches "Niche")

### Filtering Implementation

- Support multiple status values separated by commas
- Validate status values against the enum before querying
- Return 400 error if invalid status value is provided


### Sorting Implementation

- Default sort: `dateApplied` descending (newest first)
- Validate `sortBy` field exists in the application schema
- Ensure consistent behavior for null/missing date fields (maybe we just make it mandatory)


### Pagination

- Always enforce maximum page size
- Return accurate pagination metadata for frontend to render page controls


### Security

- Always filter applications by the authenticated user's `userId` from the JWT token
- Never allow access to other users' application data
- Validate and sanitize all query parameters to prevent injection attacks

---

## Implementation Notes for Frontend

### Error Response Format

---

## Testing Checklist

### Backend Tests

* Returns only applications belonging to authenticated user
* Search matches company name (case-insensitive, partial)
* Filter by single status works correctly
* Filter by multiple statuses works correctly
* Invalid status value returns 400 error
* Sort by dateApplied ascending/descending works
* Pagination returns correct page of results
* Pagination metadata is accurate
* Combining search + filter + sort works correctly
* Unauthenticated requests return 401

### Frontend Tests
