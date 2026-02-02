# API Documentation - Feature #4: Company Wiki ("Rate My Co-op")

## Overview

This document outlines the API endpoints for the common functionality, i.e. API endpoints utilized by multiple components, screens, features, etc.

---

## Data Models

N/A

## Endpoints

# Get Terms

**Path:** `/api/common/terms`

**Method:** `GET`

**Description:** Retrieves a list of all terms (Winter, Summer, Fall)

**Authentication:** Not Required

**Request Headers:** N/A

**Query Parameters:** N/A

## **Request Body:** None

**Response 200 OK:**

```json
["Winter", "Summer", "Fall"]
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

# Get Term Year Range

**Path:** `/api/common/termYearRange`

**Method:** `GET`

**Description:** Returns the earliest possible term year (1950, well before the invention of the field of Computer Science) and current year

**Authentication:** Not Required

**Request Headers:** N/A

**Query Parameters:** N/A

## **Request Body:** None

**Response 200 OK:**

```json
{
  "lowerBound": "1950",
  "upperBound": "<current-year>"
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

## Implementation Notes for Backend

### Security

- The common API should not be used to return sensitive data.
- In general, the paths within the common API should not require authentification.
- The common API should not be able to perform any mutations to data (editing, deletion, creation of new data)

---

## Testing Checklist

### Terms Tests

- [ ] Returns an array
- [ ] Array returned is non-empty
- [ ] Objects in array are Strings

### Term Years Tests

- [ ] Object returned contains "lowerBound" and "upperBound" fields
- [ ] "lowerBound" and "upperBound" values should be Strings that are parsable to long datatype
- [ ] "lowerBound" long value should be strictly less than "upperBound" long value
- [ ] "upperBound" long value should equal the current year
