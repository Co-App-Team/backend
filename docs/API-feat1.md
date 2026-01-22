# API Documentation - Feature #3: Application Filtering/Search

## Overview
This document outlines the API endpoints for the user authentication feature of CoApp. This feature allows users to log in, create an account, change their password, and log out.

---

## Data Models

**Note:** This will also require a User DTO, which should be outlined in the documentation for Feature 1. There may also be a Job Application DTO outlined in feature 2, so this outlines the minimum requirements of that DTO for this feature.

### Application DTO
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

### 1. Log in

**Path:** `/api/login`

**Method:** `POST`

**Description:** Takes a username and password, and if correct, sets a JWT token in a cookie, authenticating the user.

**Request Body:** 

```json
{
  "username": "x",
  "password": "y"
}
```

**Response 200 OK:** 

Response header:
```
Set-Cookie: Authorization=Bearer <token>; HttpOnly; Secure; SameSite=Strict
```

Response body: N/A

**Response 400 Bad Request:**

```json
{
  "error": "BAD_REQUEST",
  "message": "Username or password not present"
}
```

**Response 401 Unauthorized:**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid username or password."
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

### 2. Log out

**Path:** `/api/login`

**Method:** `DELETE`

**Description:** Unsets the authentication cookie on the client, and invalidates their token on the backend.

**Authentication:** TODO

**Request Headers:**

- `Cookie`: Contains JWT authentication token TODO

**Query Parameters:**

**Request Body:** None

**Response 200 OK:** No body

**Response 401 Unauthorized:** TODO

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required. Please log in."
}
```


---

### 3. Create an account

**Path:** `/api/account`

**Method:** `POST`

**Description:** Creates an account for a provided username and password.


**Request Body:** 

```json
{
  "username": "abcd",
  "password": "wxyz"
}
```

**Response 200 OK:**

Response header:
```
Set-Cookie: Authorization=Bearer <token>; HttpOnly; Secure; SameSite=Strict
```

Response body: N/A

**Response 409 Conflict:**

Response body: 
```json
{
  "error": "ACCOUNT_ALREADY_EXISTS",
  "message": "An account with that username already exists"
}
```

---

### 4. Change password

TODO

## Implementation Notes for Backend

- TODO: JWT token format
- TODO: Make note regarding possible issues with setting `SameSite` and `Secure` Cookie flags
- TODO: Should log out require auth? If I want it to invalidate the token in the back end then yes, but if not it doesn't matter.

### Error Response Format

---

## Testing Checklist

### Backend Tests

* TODO

### Frontend Tests

