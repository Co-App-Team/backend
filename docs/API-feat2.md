# API Documentation - Feature #2: Log Job Applications

## Overview
This document outlines the API endpoints for the log job applications feature of CoApp. This feature allows users to log new job applications, delete existing applications, and edit application details. 

---

## Data Models

### Application DTO
```json
{
  "applicationId": "string",
  "userId": "string",
  "companyId": "string", 
  "jobTitle": "string",
  "status": "string",
  "applicationDeadline": "datetime string",
  "dateCreated": "datetime string", 
  "dateModified": "datetime string", 
  "jobDescription": "string (optional)",
  "numPositions": "int (optional)", 
  "sourceLink": "string (optional)",
  "dateApplied": "datetime string (optional)",
  "notes": "string (optional)",
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

**Description:** Takes a userId, companyId, jobTitle, status, applicationDeadline. Optionally, can also take jobDescription, numPositions, sourceLink, and dateApplied.

**Request Body:** 

```json
{
  "userId": "user",
  "companyId": "company",
  "jobTitle": "title",
  "status": "status", 
  "applicationDeadline": "deadline", 
  "jobDescription": "description", 
  "numPositions": "positions", 
  "sourceLink": "link", 
  "dateApplied": "applied"
}
```

**Response 201 CREATED:** 

Response body: 
```json
{
  "applicationId": "applicationId",
  "dateCreated": "dateCreated",
  "dateModified": "dateModified"
}
```

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "At least one of the required fields (userId, companyId, jobTitle, status, applicationDeadline) is not present or invalid"
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid userId"
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

### 2. Edit an existing job application

**Path:** `/api/application`

**Method:** `PUT`

**Description:** Takes an applicationId, userId, and the field(s) (jobTitle, status, applicationDeadline, jobDescription, numPositions, sourceLink, and dateApplied) to be updated

**Request Body:** 

```json
{
  "applicationId": "application",
  "userId": "user",
  "jobTitle": "title (optional)",
  "status": "status (optional)", 
  "applicationDeadline": "deadline (optional)", 
  "jobDescription": "description (optional)", 
  "numPositions": "positions (optional)", 
  "sourceLink": "link (optional)", 
  "dateApplied": "applied (optional)"
}
```

**Response 200 OK:** 

```json
{
  "applicationId": "applicationId",
  "dateModified": "date"
}
```

**Response 400 Bad Request:** 

```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid applicationId and/or userId"
}
```

---

### 3. Delete a job application

**Path:** `/api/account`

**Method:** `DELETE`

**Description:** Takes an applicationId and userId 

**Request Body:** 

```json
{
  "applicationId": "application",
  "userId": "user"
}
```

**Response 200 OK:**

```json
{
  "applicationId": "application"
}
```


**Response 400 Bad Request:** 

```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid applicationId and/or userId"
}
```
