# API Documentation - Feature 5: Get Interview Applications

## Overview
This document outlines the API endpoint for retrieving job applications that are currently in the interview stage.
Additionally, added interviewDate as a paramter for applications, also updated in API-feat2.md
---

## Data Models

### Application Response DTO
```json
{
  "applicationId": "string",
  "companyId": "string", 
  "jobTitle": "string",
  "status": "string",
  "applicationDeadline": "datetime string",
  "jobDescription": "string (optional)",
  "numPositions": "int (optional)", 
  "sourceLink": "string (optional)",
  "dateApplied": "datetime string (optional)",
  "notes": "string (optional)",
  "interviewDate": "datetime string (optional)"
}
```

---

## Endpoints

### 1. Retrieve interview applications

**Path:** `/api/application/interviews`

**Method:** `GET`

**Description:** Retrieves a list of job applications that for which an `interviewDate` exists for the currently authenticated user. Supports optional filtering by date range.

**Query Parameters:**

| Parameter | Type   | Description                                                                                             |
| :-------- | :----- | :------------------------------------------------------------------------------------------------------ |
| startDate | string | Optional. The start date of the range to filter interview dates (format: `yyyy-MM-dd`).                 |
| endDate   | string | Optional. The end date of the range to filter interview dates (format: `yyyy-MM-dd`).                   |

**Constraints:**
- If `startDate` is provided, `endDate` must also be provided.
- If `endDate` is provided, `startDate` must also be provided.
- `startDate` must be before or equal to `endDate`.

**Response 200 OK:**

Array of applications:
```json
[
  {
    "applicationId": "applicationId",
    "companyId": "companyId",
    "jobTitle": "jobTitle",
    "status": "INTERVIEWING",
    "applicationDeadline": "applicationDeadline",
    "interviewDateTime": "interviewDateTime",
    "jobDescription": "jobDescription (optional)",
    "numPositions": "numPositions (optional)",
    "sourceLink": "sourceLink (optional)",
    "dateApplied": "dateApplied (optional)",
    "notes": "notes (optional)"
  }
]
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid inputs of the request. If start date is provided, end date must be provided as well."
}
```
*Or:*
```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid inputs of the request. Start date must be before end date."
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing authentication"
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```
