# API Documentation - Feature #2: Log Job Applications

## Overview
This document outlines the API endpoints for the log job applications feature of CoApp. This feature allows users to log new job applications, retrieve their applications, delete existing applications, and edit application details. 

---

## Data Models

### Application DTO
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

**Note: The user can possibly select an existing company if it is already in the list. **

### Company DTO
```json
{
  "companyId": "string",
  "companyName": "string",
  "address": "string", 
  "city": "string", 
  "country": "string", 
  "website": "string", 
  "dateCreated": "datetime string", 
  "dateModified": "datetime string", 
  "notes": "string (optional)"
}
```

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

### 1. Create a new job application

**Path:** `/api/application`

**Method:** `POST`

**Description:** Takes a companyId, jobTitle, status, applicationDeadline. Optionally, can also take jobDescription, numPositions, sourceLink, dateApplied, and notes. User ID is extracted from authentication.

**Request Body:**

```json
{
  "companyId": "company",
  "jobTitle": "title",
  "status": "status", 
  "applicationDeadline": "deadline", 
  "jobDescription": "description (optional)", 
  "numPositions": "positions (optional)", 
  "sourceLink": "link (optional)", 
  "dateApplied": "applied (optional)",
  "notes": "notes (optional)",
  "interviewDate": "interview date (optional)"
}
```

**Response 201 CREATED:**

Response body:
```json
{
  "applicationId": "applicationId",
  "companyId": "companyId",
  "jobTitle": "jobTitle",
  "status": "status",
  "applicationDeadline": "applicationDeadline",
  "jobDescription": "jobDescription (optional)",
  "numPositions": "numPositions (optional)",
  "sourceLink": "sourceLink (optional)",
  "dateApplied": "dateApplied (optional)",
  "notes": "notes (optional)",
  "interviewDate": "interview date (optional)"
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "At least one of the required fields (companyId, jobTitle, status, applicationDeadline) is not present or invalid"
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing authentication"
}
```

**Response 404 Not Found:**

```json
{
  "error": "COMPANY_NOT_FOUND",
  "message": "Company not found"
}
```

**Response 409 Conflict:**

```json
{
  "error": "DUPLICATE_APPLICATION",
  "message": "Duplicate application detected"
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

### 2. Edit an existing job application

**Path:** `/api/application/{applicationId}`

**Method:** `PUT`

**Description:** Updates specific fields of an existing application (companyId, jobTitle, status, applicationDeadline, jobDescription, numPositions, sourceLink, dateApplied, notes). At least one field must be provided. User ID is extracted from authentication.

**Request Body:**

```json
{
  "companyId": "company (optional)",
  "jobTitle": "title (optional)",
  "status": "status (optional)", 
  "applicationDeadline": "deadline (optional)", 
  "jobDescription": "description (optional)", 
  "numPositions": "positions (optional)", 
  "sourceLink": "link (optional)", 
  "dateApplied": "applied (optional)",
  "notes": "notes (optional)",
  "interviewDate": "interview date (optional)"
}
```

**Response 200 OK:**

```json
{
  "applicationId": "applicationId",
  "companyId": "companyId",
  "jobTitle": "jobTitle",
  "status": "status",
  "applicationDeadline": "applicationDeadline",
  "jobDescription": "jobDescription (optional)",
  "numPositions": "numPositions (optional)",
  "sourceLink": "sourceLink (optional)",
  "dateApplied": "dateApplied (optional)",
  "notes": "notes (optional)",
  "interviewDate": "interview date (optional)"
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "No valid fields provided for update or invalid data"
}
```

**Response 403 Forbidden:**

```json
{
  "error": "UNAUTHORIZED_APPLICATION_ACCESS",
  "message": "Unauthorized access to application"
}
```

**Response 404 Not Found:**

```json
{
  "error": "APPLICATION_NOT_FOUND",
  "message": "Application not found"
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

### 3. Delete a job application

**Path:** `/api/application/{applicationId}`

**Method:** `DELETE`

**Description:** Deletes an existing application by ID. User ID is extracted from authentication.

**No Request Body.**

**Response 200 OK:**

```json
{
  "message": "Application successfully deleted."
}
```

**Response 403 Forbidden:**

```json
{
  "error": "UNAUTHORIZED_APPLICATION_ACCESS",
  "message": "Unauthorized access to application"
}
```

**Response 404 Not Found:**

```json
{
  "error": "APPLICATION_NOT_FOUND",
  "message": "Application not found"
}
```

**Response 500 Internal Server Error:**

```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred while processing your request."
}
```

---

### 4. Retrieve all job applications

**Path:** `/api/application`

**Method:** `GET`

**Description:** Retrieves all job applications for the currently authenticated user.

**No Request Body.**

**Response 200 OK:**

Array of applications:
```json
[
  {
    "applicationId": "applicationId",
    "companyId": "companyId",
    "jobTitle": "jobTitle",
    "status": "status",
    "applicationDeadline": "applicationDeadline",
    "jobDescription": "jobDescription (optional)",
    "numPositions": "numPositions (optional)",
    "sourceLink": "sourceLink (optional)",
    "dateApplied": "dateApplied (optional)",
    "notes": "notes (optional)",
    "interviewDate": "interview date (optional)"
  }
]
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