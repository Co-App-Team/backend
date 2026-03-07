# API Documentation - Feature #6: AI resume builder

## Overview
This document outlines the API endpoints for the AI resume builder feature of CoApp. This feature allows users interact with GenAI to improve their resumes.

## Endpoints

### 1. Prompt

**Path:** `api/resume-ai-advisor/`

**Method:** `POST`

**Description**: Prompt to chatbot 

**Request Body** 

```json
{
    "userPrompt": "question from user",
    "applicationId" : "applicationID"
}
```

*Note: `applicationId` is optional context.*

**Response 200 OK:**

Response body:
```json
{
  "response":"response from GenAI"
}
```

**Response 429 TOO MANY REQUESTS:**

Response body:
```json
{
  "error":"OVER_LIMIT_CHATBOT_REQUEST",
  "message":"You have reached your usage limit for Co-App chatbot. Please wait before making more requests."
}
```

**Response 400 BAD REQUESTS:**

Response body:
```json
{
  "error":"OVER_LIMIT_CHARACTER",
  "message":"Your prompt is too long. Please shorten it and try again..."
}
```


### 2. Update users' profiles for GenAI context

#### 2.1 View user information


This is an updated of the same endpoint from `API-feat1.md`

**Path:** `api/user/experience`

**Method:** `GET`

**Response 200 OK:**

Response body:
```json
{
    "experience":[
      {
        "experienceId": "experienceId1",
        "companyId": "companyId1",
        "roleTitle": "Software Engineer",
        "roleDescription": "Built and maintained microservices using Spring Boot",
        "startDate": "2023-01-01",
        "endDate": "2024-01-01"
      },
      {
       "experienceId": "experienceId2",
        "companyId": "companyId2",
        "roleTitle": "Software Engineer",
        "roleDescription": "Built and maintained microservices using Spring Boot",
        "startDate": "2023-01-01",
        "endDate": "2024-01-01"
      },
      ...
    ]
}
```

#### 2.2 Add/update/delete experience

**Note: `endDate` can be null to indicate current job.**

**Path:** `api/user/experience`

**Method:** `POST`

**Description**: Create a new experience for the user

**Request Body**

```json
{
  "companyId": "companyId",
  "roleTitle": "Software Engineer",
  "roleDescription": "Built and maintained microservices using Spring Boot",
  "startDate": "2023-01-01",
  "endDate": "2024-01-01"
}
```

**Response 200 OK:**

Response body:
```json
{
  "experienceId":"ID of the new experience"
}
```

**Response 400 BAD REQUEST:**

 *The backend will return 400 if any field is invalid. 
 This includes null/emptty fields or role title/description is over character limit (We will specify exact number of character limit in the response's message).*

**Response 404 NOT FOUND:**

Response body:
```json
{
  "error":"COMPANY_NOT_FOUND",
  "message":"Company with this companyId does not exist."
}
```
**Path:** `api/user/experience/{experienceId}`

**Method:** `PATCH`

**Description**: Update the existing experience for the user

**Request Body**

```json
{
   "companyId": "companyId",
   "roleTitle": "Software Engineer",
   "roleDescription": "Built and maintained microservices using Spring Boot",
   "startDate": "2023-01-01",
   "endDate": "2024-01-01"
}
```

**Response 200 OK:**

Response body:
```json
{
  "response":"Updated successfully."
}
```

**Response 400 BAD REQUEST:**

*The backend will return 400 if any field is invalid.
This includes null/emptty fields or role title/description is over character limit (We will specify exact number of character limit in the response's message).*


**Response 404 NOT FOUND:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_FOUND",
  "message":"The experience does not exist or not belong to the user."
}
```

**Response 403 FORBIDDEN:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_OWN",
  "message":"The experience doesn't belong to the user. Can NOT delete."
}
```

**Response 404 NOT FOUND:**

Response body:
```json
{
  "error":"COMPANY_NOT_FOUND",
  "message":"Company with this companyId does not exist."
}
```

**Path:** `api/user/experience/{experienceId}`

**Method:** `DELETE`

**Description**: Delete the existing experience for the user

**Request Body**

*N/A*

**Response 200 OK:**

Response body:
```json
{
  "response":"Deleted successfully."
}
```

**Response 404 NOT FOUND:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_FOUND",
  "message":"The experience does not exist or not belong to the user."
}
```

**Response 403 FORBIDDEN:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_OWN",
  "message":"The experience doesn't belong to the user. Can NOT delete."
}
```







