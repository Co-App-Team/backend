# Common response for endpoints

This document provides a comprehensive list of standard API responses that
may be returned by the backend. Clients should expect to receive any of
these responses for all API calls, unless otherwise specified.

## Access token failure.

This failure relates to `JWT` token for session management.

### Exception: `api/auth/*`. All API calls to `api/auth/*` are not required to have `JWT` token.

**Response 401 UNAUTHORIZED:**

Response body:
```json
{
  "error":"TOKEN_EXPIRE",
  "message":"Token is expired. Please log in again."
}
```

**Response 401 UNAUTHORIZED:**

Response body:
```json
{
  "error":"INVALID_TOKEN",
  "message":"Invalid token. Please log in again to obtain new token."
}
```

## Internal server failure.

### Exception: N/A. This applies to all endpoints.


For every API, we can potentially return this response (if something goes wrong) and the message indicating the internal failure

**Response 500 INTERNAL SERVER ERROR:**

Response body:
```json
{
  "error":"INTERNAL_ERROR",
  "message":"<this message will indicate a specific failure>"
}
```

## Bad request

### Exception: N/A

Bad request (request has null or empty field)

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"REQUEST_HAS_NULL_OR_EMPTY_FIELD",
  "message":"The error message will be customized based on the request."
}
```

Bad request (request has invalid data format). For example, we expect date in format of `yyyy-MM-dd`

**Response 400 BAD REQUEST:**

```json
{
    "error": "INVALID_FORMAT_FIELD",
    "message": "Invalid date format for field 'startDate'. Expected format: yyyy-MM-dd."
}
```